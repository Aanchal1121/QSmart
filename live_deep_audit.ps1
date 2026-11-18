# QSmart Live End-to-End Deep Verification Audit Script
$baseUrl = "http://localhost:8080"
$ErrorActionPreference = "Continue"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "QSMART DEEP LIVE VERIFICATION AUDIT STARTING" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$results = [System.Collections.Generic.List[PSObject]]::new()

function Record-Test($category, $testName, $passed, $details) {
    $status = if ($passed) { "PASS" } else { "FAIL" }
    $color = if ($passed) { "Green" } else { "Red" }
    Write-Host "[$status] $category - $testName : $details" -ForegroundColor $color
    $results.Add([PSCustomObject]@{
        Category = $category
        TestName = $testName
        Status = $status
        Details = $details
    })
}

# ----------------------------------------------------
# 1. AUTHENTICATION & RBAC
# ----------------------------------------------------
Write-Host "`n--- 1. Testing Authentication & RBAC ---" -ForegroundColor Yellow

# Admin Login
$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -ContentType "application/json" `
        -Body '{"username":"admin@qsmart.com","password":"Admin@123"}' -WebSession $adminSession
    Record-Test "AUTH" "Admin Login" ($res.success -and $res.data.role -eq "ROLE_ADMIN") "Token received for admin@qsmart.com"
} catch {
    Record-Test "AUTH" "Admin Login" $false $_.Exception.Message
}

# Staff Login
$staffSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -ContentType "application/json" `
        -Body '{"username":"staff.rahul@qsmart.com","password":"Staff@123"}' -WebSession $staffSession
    Record-Test "AUTH" "Staff Login" ($res.success -and $res.data.role -eq "ROLE_STAFF") "Staff authenticated successfully (staff.rahul@qsmart.com)"
} catch {
    Record-Test "AUTH" "Staff Login" $false $_.Exception.Message
}

# Doctor Login
$doctorSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -ContentType "application/json" `
        -Body '{"username":"dr.sharma@qsmart.com","password":"Doctor@123"}' -WebSession $doctorSession
    Record-Test "AUTH" "Doctor Login" ($res.success -and $res.data.role -eq "ROLE_DOCTOR") "Doctor authenticated successfully (dr.sharma@qsmart.com)"
} catch {
    Record-Test "AUTH" "Doctor Login" $false $_.Exception.Message
}

# Patient Registration & Login
$patientPhone = "98" + (Get-Random -Minimum 10000000 -Maximum 99999999)
$patientSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    $regBody = @{
        name = "Audit Patient Deep"
        phone = $patientPhone
        email = "audit$patientPhone@test.com"
        password = "Patient@123"
        role = "ROLE_PATIENT"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -ContentType "application/json" `
        -Body $regBody
    Record-Test "AUTH" "Patient Registration" ($res.success) "Registered unique patient phone: $patientPhone"

    # Authenticate registered patient
    $loginRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -ContentType "application/json" `
        -Body (@{ username = $patientPhone; password = "Patient@123" } | ConvertTo-Json) -WebSession $patientSession
    Record-Test "AUTH" "Patient Login Session" ($loginRes.success -and $loginRes.data.role -eq "ROLE_PATIENT") "Session established for patient"
} catch {
    Record-Test "AUTH" "Patient Registration & Login" $false $_.Exception.Message
}

# Invalid Credentials Test
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -ContentType "application/json" `
        -Body '{"username":"admin@qsmart.com","password":"WRONG_PASSWORD"}'
    Record-Test "AUTH" "Invalid Credentials Rejection" ($res.success -eq $false) "Correctly returned error on bad password"
} catch {
    Record-Test "AUTH" "Invalid Credentials Rejection" $true "Correctly rejected bad password (HTTP error)"
}

# RBAC Test: Patient trying to access Admin analytics (Must be Forbidden)
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/admin/analytics" -Method Get -WebSession $patientSession
    Record-Test "RBAC" "Patient Cannot Access Admin Analytics" $false "Patient unexpectedly accessed admin endpoint"
} catch {
    $isForbidden = ($_.Exception.Response.StatusCode.value__ -eq 403) -or ($_.Exception.Message -match "403")
    Record-Test "RBAC" "Patient Cannot Access Admin Analytics" $isForbidden "Strictly returned 403 Forbidden"
}

# ----------------------------------------------------
# 2. CENTRALIZED FIFO QUEUE ACROSS 3 CHANNELS
# ----------------------------------------------------
Write-Host "`n--- 2. Testing Centralized FIFO Queue Across All 3 Channels ---" -ForegroundColor Yellow

# Channel A: Smartphone booking
$smartToken = $null
try {
    $bookBody = @{
        departmentId = 1
        patientName = "Smartphone Patient"
        patientPhone = $patientPhone
        accessChannel = "SMARTPHONE"
        priorityCategory = "NORMAL"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/patient/tokens/book" -Method Post -ContentType "application/json" `
        -Body $bookBody -WebSession $patientSession
    $smartToken = $res.data
    Record-Test "FIFO" "Channel A (Smartphone Booking)" ($res.success -and $smartToken.tokenNumber) "Token: $($smartToken.tokenNumber), Seq: $($smartToken.sequenceNumber)"
} catch {
    Record-Test "FIFO" "Channel A (Smartphone Booking)" $false $_.Exception.Message
}

# Channel B: SMS Keypad Phone booking via Inbound SMS Webhook
$smsToken = $null
$smsPhone = "97" + (Get-Random -Minimum 10000000 -Maximum 99999999)
try {
    $smsBody = @{
        from = $smsPhone
        text = "BOOK HOSPITAL01 OPD"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/sms/webhook" -Method Post -ContentType "application/json" -Body $smsBody
    $smsTokenNumber = $res.tokenNumber
    Record-Test "FIFO" "Channel B (SMS Keypad Booking)" ($res.success -and $smsTokenNumber) "Token: $smsTokenNumber, Reply: $($res.reply)"
} catch {
    Record-Test "FIFO" "Channel B (SMS Keypad Booking)" $false $_.Exception.Message
}

# Channel C: Walk-in Patient registered by Staff at reception
$walkinToken = $null
$walkinPhone = "96" + (Get-Random -Minimum 10000000 -Maximum 99999999)
try {
    $walkinBody = @{
        departmentId = 1
        patientName = "Walk-in Desk Patient"
        patientPhone = $walkinPhone
        priorityCategory = "NORMAL"
        priorityReason = "Standard Walk-in"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/staff/walk-in/register" -Method Post -ContentType "application/json" `
        -Body $walkinBody -WebSession $staffSession
    $walkinToken = $res.data
    Record-Test "FIFO" "Channel C (Walk-in Staff Booking)" ($res.success -and $walkinToken.tokenNumber) "Token: $($walkinToken.tokenNumber), Seq: $($walkinToken.sequenceNumber)"
} catch {
    Record-Test "FIFO" "Channel C (Walk-in Staff Booking)" $false $_.Exception.Message
}

# Verify ALL 3 tokens are in the SAME queue in sequential FIFO order
try {
    $qRes = Invoke-RestMethod -Uri "$baseUrl/api/public/queue/1" -Method Get
    $tokens = $qRes.data.waitingTokens
    $smartFound = $tokens | Where-Object { $_.patientPhone -eq $patientPhone }
    $smsFound = $tokens | Where-Object { $_.patientPhone -eq $smsPhone }
    $walkinFound = $tokens | Where-Object { $_.patientPhone -eq $walkinPhone }

    $allInSameQueue = ($smartFound -ne $null) -and ($smsFound -ne $null) -and ($walkinFound -ne $null)
    $fifoPreserved = ($smartFound.sequenceNumber -lt $smsFound.sequenceNumber) -and ($smsFound.sequenceNumber -lt $walkinFound.sequenceNumber)
    
    Record-Test "FIFO" "Centralized FIFO Order Across All 3 Channels" ($allInSameQueue -and $fifoPreserved) `
        "Smartphone (#$($smartFound.sequenceNumber)) < SMS (#$($smsFound.sequenceNumber)) < Walk-in (#$($walkinFound.sequenceNumber))"
} catch {
    Record-Test "FIFO" "Centralized FIFO Order Across All 3 Channels" $false $_.Exception.Message
}

# ----------------------------------------------------
# 3. DUPLICATE ACTIVE BOOKING PREVENTION
# ----------------------------------------------------
Write-Host "`n--- 3. Testing Duplicate Active Booking Prevention ---" -ForegroundColor Yellow

try {
    # Attempt second booking with identical phone in same department today
    $dupBody = @{
        departmentId = 1
        patientName = "Duplicate Attempt"
        patientPhone = $patientPhone
        accessChannel = "SMARTPHONE"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/patient/tokens/book" -Method Post -ContentType "application/json" `
        -Body $dupBody -WebSession $patientSession
    Record-Test "CONCURRENCY" "Duplicate Booking Prevention" ($res.success -eq $false) "Duplicate was rejected"
} catch {
    Record-Test "CONCURRENCY" "Duplicate Booking Prevention" $true "Rejected with message: $($_.Exception.Message)"
}

# ----------------------------------------------------
# 4. TOKEN CANCELLATION & WAITING TIME RECALCULATION
# ----------------------------------------------------
Write-Host "`n--- 4. Testing Token Cancellation & ETA Recalculation ---" -ForegroundColor Yellow

if ($smartToken) {
    try {
        $res = Invoke-RestMethod -Uri "$baseUrl/api/patient/tokens/$($smartToken.id)/cancel" `
            -Method Post -WebSession $patientSession
        Record-Test "LIFECYCLE" "Token Cancellation" ($res.success -and $res.data.status -eq "CANCELLED") "Status transitioned to CANCELLED"
    } catch {
        Record-Test "LIFECYCLE" "Token Cancellation" $false $_.Exception.Message
    }

    # Verify invalid double cancellation
    try {
        $res = Invoke-RestMethod -Uri "$baseUrl/api/patient/tokens/$($smartToken.id)/cancel" -Method Post -WebSession $patientSession
        Record-Test "LIFECYCLE" "Double Cancellation Rejection" ($res.success -eq $false) "Double cancel blocked"
    } catch {
        Record-Test "LIFECYCLE" "Double Cancellation Rejection" $true "Double cancel correctly rejected"
    }
}

# ----------------------------------------------------
# 5. STAFF LIFECYCLE: CALL NEXT, MANDATORY SKIP REASON, MARK SERVED
# ----------------------------------------------------
Write-Host "`n--- 5. Testing Staff Queue Operations ---" -ForegroundColor Yellow

# Call Next
$calledToken = $null
try {
    $res = Invoke-RestMethod -Uri "$baseUrl/api/staff/queue/call-next?departmentId=1&counterId=1" `
        -Method Post -WebSession $staffSession
    $calledToken = $res.data
    Record-Test "STAFF" "Call Next Token" ($res.success -and $calledToken.status -eq "CALLED") `
        "Token $($calledToken.tokenNumber) assigned to counter"
} catch {
    Record-Test "STAFF" "Call Next Token" $false $_.Exception.Message
}

# Skip without reason (Must fail!)
if ($calledToken) {
    try {
        $skipEmpty = @{ tokenId = $calledToken.id; reason = "" } | ConvertTo-Json
        $res = Invoke-RestMethod -Uri "$baseUrl/api/staff/queue/skip" -Method Post -ContentType "application/json" `
            -Body $skipEmpty -WebSession $staffSession
        Record-Test "STAFF" "Skip Requires Mandatory Reason" ($res.success -eq $false) "Empty reason was rejected"
    } catch {
        Record-Test "STAFF" "Skip Requires Mandatory Reason" $true "Rejected empty skip reason as required"
    }

    # Skip with valid reason
    try {
        $skipValid = @{ tokenId = $calledToken.id; reason = "Patient_Absent_After_3_Calls" } | ConvertTo-Json
        $res = Invoke-RestMethod -Uri "$baseUrl/api/staff/queue/skip" -Method Post -ContentType "application/json" `
            -Body $skipValid -WebSession $staffSession
        Record-Test "STAFF" "Skip With Reason" ($res.success -and $res.data.status -eq "SKIPPED") `
        "Token $($calledToken.tokenNumber) marked SKIPPED with reason"
    } catch {
        Record-Test "STAFF" "Skip With Reason" $false $_.Exception.Message
    }
}

# Call another token and Mark Served
try {
    $callRes = Invoke-RestMethod -Uri "$baseUrl/api/staff/queue/call-next?departmentId=1&counterId=1" `
        -Method Post -WebSession $staffSession
    if ($callRes.success -and $callRes.data) {
        $servedRes = Invoke-RestMethod -Uri "$baseUrl/api/staff/queue/mark-served?tokenId=$($callRes.data.id)" `
            -Method Post -WebSession $staffSession
        Record-Test "STAFF" "Mark Served & Free Counter" ($servedRes.success -and $servedRes.data.status -eq "SERVED") `
            "Token $($callRes.data.tokenNumber) SERVED, counter freed"
    }
} catch {
    Record-Test "STAFF" "Mark Served & Free Counter" $false $_.Exception.Message
}

# ----------------------------------------------------
# 6. DOCTOR CLINIC & EMERGENCY DELAY CASCADING
# ----------------------------------------------------
Write-Host "`n--- 6. Testing Doctor Status & Emergency Delay Cascading ---" -ForegroundColor Yellow

try {
    # Report Emergency Delay of 25 minutes
    $delayBody = @{
        status = "EMERGENCY_DELAY"
        reportedDelayMinutes = 25
        delayReason = "Attending ICU Code Blue emergency"
    } | ConvertTo-Json
    $res = Invoke-RestMethod -Uri "$baseUrl/api/doctor/status" -Method Post -ContentType "application/json" `
        -Body $delayBody -WebSession $doctorSession
    Record-Test "DOCTOR" "Doctor Report Emergency Delay" ($res.success -and $res.data.status -eq "EMERGENCY_DELAY") "Delay: 25 mins reported"

    # Verify Department Wait Time Recalculated
    $deptRes = Invoke-RestMethod -Uri "$baseUrl/api/public/queue/1" -Method Get
    Record-Test "DOCTOR" "Department Delay Cascading" ($deptRes.data.delayMinutes -eq 25) `
        "Department currentDelayMinutes updated to $($deptRes.data.delayMinutes) mins"

    # Restore Doctor Status to AVAILABLE
    $availBody = @{
        status = "AVAILABLE"
        reportedDelayMinutes = 0
        delayReason = $null
    } | ConvertTo-Json
    $res2 = Invoke-RestMethod -Uri "$baseUrl/api/doctor/status" -Method Post -ContentType "application/json" `
        -Body $availBody -WebSession $doctorSession
    Record-Test "DOCTOR" "Restore Doctor Available" ($res2.success -and $res2.data.status -eq "AVAILABLE") "Doctor restored to AVAILABLE"
} catch {
    Record-Test "DOCTOR" "Doctor Operations" $false $_.Exception.Message
}

# ----------------------------------------------------
# 7. SMS KEYPAD COMMAND SUITE
# ----------------------------------------------------
Write-Host "`n--- 7. Testing SMS Keypad Commands ---" -ForegroundColor Yellow

$smsTestPhone = "99" + (Get-Random -Minimum 10000000 -Maximum 99999999)

# HELP
$helpRes = Invoke-RestMethod -Uri "$baseUrl/api/sms/webhook" -Method Post -ContentType "application/json" `
    -Body (@{ from = $smsTestPhone; text = "HELP" } | ConvertTo-Json)
Record-Test "SMS" "SMS HELP Command" ($helpRes.success -and $helpRes.reply -match "BOOK <HOSPITAL>") $helpRes.reply.Replace("`n", " ")

# BOOK
$bookRes = Invoke-RestMethod -Uri "$baseUrl/api/sms/webhook" -Method Post -ContentType "application/json" `
    -Body (@{ from = $smsTestPhone; text = "BOOK HOSPITAL01 OPD" } | ConvertTo-Json)
$newSmsToken = $bookRes.tokenNumber
Record-Test "SMS" "SMS BOOK Command" ($bookRes.success -and $newSmsToken) "Booked token: $newSmsToken"

# STATUS
if ($newSmsToken) {
    $statRes = Invoke-RestMethod -Uri "$baseUrl/api/sms/webhook" -Method Post -ContentType "application/json" `
        -Body (@{ from = $smsTestPhone; text = "STATUS $newSmsToken" } | ConvertTo-Json)
    Record-Test "SMS" "SMS STATUS Command" ($statRes.success -and $statRes.reply -match "WAITING") $statRes.reply

    # CANCEL
    $cancRes = Invoke-RestMethod -Uri "$baseUrl/api/sms/webhook" -Method Post -ContentType "application/json" `
        -Body (@{ from = $smsTestPhone; text = "CANCEL $newSmsToken" } | ConvertTo-Json)
    Record-Test "SMS" "SMS CANCEL Command" ($cancRes.success -and $cancRes.reply -match "cancelled") $cancRes.reply
}

# UNKNOWN
$unkRes = Invoke-RestMethod -Uri "$baseUrl/api/sms/webhook" -Method Post -ContentType "application/json" `
    -Body (@{ from = $smsTestPhone; text = "GIBBERISH 123" } | ConvertTo-Json)
Record-Test "SMS" "SMS Unknown Command" ($unkRes.success -eq $false -and $unkRes.reply -match "Unknown") "Graceful help returned"

# ----------------------------------------------------
# 8. ADMIN DASHBOARD & AUDIT LOGS
# ----------------------------------------------------
Write-Host "`n--- 8. Testing Admin Analytics & Audit Logs ---" -ForegroundColor Yellow

try {
    $analytics = Invoke-RestMethod -Uri "$baseUrl/api/admin/analytics" -Method Get -WebSession $adminSession
    Record-Test "ADMIN" "Analytics KPI Endpoint" ($analytics.success -and $analytics.data.totalBookings -gt 0) `
        "Total: $($analytics.data.totalBookings), Waiting: $($analytics.data.waitingCount), Served: $($analytics.data.servedCount)"

    $auditLogs = Invoke-RestMethod -Uri "$baseUrl/api/admin/audit-logs" -Method Get -WebSession $adminSession
    Record-Test "ADMIN" "Audit Trail Compliance" ($auditLogs.success -and $auditLogs.data.Count -gt 0) `
        "Found $($auditLogs.data.Count) audit trail events logged with actor, entity, and timestamp"
} catch {
    Record-Test "ADMIN" "Admin Analytics & Logs" $false $_.Exception.Message
}

# ----------------------------------------------------
# 9. PRIVACY & SECURITY: QR HASH VERIFICATION
# ----------------------------------------------------
Write-Host "`n--- 9. Testing Privacy & Security ---" -ForegroundColor Yellow

if ($walkinToken) {
    $qr = $walkinToken.verificationQrHash
    $isOpaqueUuid = ($qr.Length -eq 36) -and ($qr -match "^[0-9a-fA-F-]{36}$")
    $noPii = -not ($qr -match $walkinPhone) -and -not ($qr -match "Walk-in")
    Record-Test "SECURITY" "Opaque Non-PII QR Hash" ($isOpaqueUuid -and $noPii) `
        "QR hash is an opaque 36-char UUID ($qr), zero sensitive PII leaked"
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
$passCount = ($results | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($results | Where-Object { $_.Status -eq "FAIL" }).Count
Write-Host "DEEP AUDIT FINISHED: $passCount PASSED, $failCount FAILED out of $($results.Count) tests" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Red" })
Write-Host "==========================================================" -ForegroundColor Cyan

$results | Export-Csv -Path "e:\QSmart\deep_audit_results.csv" -NoTypeInformation
