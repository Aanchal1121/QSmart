package com.qsmart.controller;

import com.qsmart.dto.ApiResponse;
import com.qsmart.dto.SmsInboundRequest;
import com.qsmart.dto.SmsInboundResponse;
import com.qsmart.model.SmsMessage;
import com.qsmart.repository.SmsMessageRepository;
import com.qsmart.service.SmsProvider;
import com.qsmart.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
public class SmsWebhookController {

    private final SmsService smsService;
    private final SmsMessageRepository smsMessageRepository;
    private final SmsProvider smsProvider;

    @Autowired
    public SmsWebhookController(SmsService smsService,
                                SmsMessageRepository smsMessageRepository,
                                SmsProvider smsProvider) {
        this.smsService = smsService;
        this.smsMessageRepository = smsMessageRepository;
        this.smsProvider = smsProvider;
    }

    /**
     * Inbound SMS webhook endpoint
     * Accepts webhook from telecom gateway / aggregators (e.g., Gupshup, Textlocal, Twilio)
     */
    @PostMapping("/webhook")
    public ResponseEntity<SmsInboundResponse> handleWebhook(@RequestBody SmsInboundRequest request) {
        SmsInboundResponse response = smsService.processInboundSms(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Test / Simulator endpoint to trigger SMS commands from web UI
     */
    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<SmsInboundResponse>> simulateSms(@RequestBody SmsInboundRequest request) {
        SmsInboundResponse response = smsService.processInboundSms(request);
        return ResponseEntity.ok(ApiResponse.ok("SMS processed", response));
    }

    /**
     * Recent SMS traffic for verification
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SmsMessage>>> getSmsHistory() {
        return ResponseEntity.ok(ApiResponse.ok("Recent SMS records", smsMessageRepository.findTop50ByOrderByTimestampDesc()));
    }

    /**
     * SMS Gateway status and Indian telecom gateway documentation
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSmsGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("providerName", smsProvider.getProviderName());
        info.put("isMockMode", smsProvider.isMock());
        info.put("supportedCommands", List.of(
                "BOOK <HOSPITAL_CODE> <DEPT_CODE>  (e.g., BOOK HOSPITAL01 OPD)",
                "STATUS <TOKEN_NUMBER>             (e.g., STATUS OPD-001 or STATUS H-025)",
                "CANCEL <TOKEN_NUMBER>             (e.g., CANCEL OPD-001)",
                "HELP                              (lists all available commands)"
        ));
        info.put("indianTelecomRequirements", List.of(
                "1. TRAI / DLT (Distributed Ledger Technology) Entity Registration (e.g. Jio / Airtel / Vi / BSNL DLT portal).",
                "2. Approved Sender ID / Header (e.g. 'QSMRT', 'QSMART', 6-character alpha).",
                "3. Registered Content Templates with exact variable tokens for OTP and transactional SMS.",
                "4. 10-digit Dedicated Long Code or Short Code (e.g. 56767) for 2-way incoming SMS webhook.",
                "5. Webhook URL HTTPS SSL endpoint mapped to telecom aggregator API (Gupshup / Textlocal / Exotel / Kaleyra)."
        ));

        return ResponseEntity.ok(ApiResponse.ok("SMS Gateway Information", info));
    }
}
