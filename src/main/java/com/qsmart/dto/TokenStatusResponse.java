package com.qsmart.dto;

import com.qsmart.model.AccessChannel;
import com.qsmart.model.PriorityCategory;
import com.qsmart.model.TokenStatus;

import java.time.LocalDateTime;

public class TokenStatusResponse {
    private Long id;
    private String tokenNumber;
    private int sequenceNumber;
    private Long departmentId;
    private String departmentName;
    private String hospitalName;
    private String patientName;
    private String patientPhone;
    private AccessChannel accessChannel;
    private TokenStatus status;
    private PriorityCategory priorityCategory;
    private String priorityReason;
    private String assignedCounterName;
    private String servedByName;
    private int peopleAhead;
    private int estimatedWaitingMinutes;
    private LocalDateTime estimatedCallTime;
    private String currentServingToken;
    private LocalDateTime createdAt;
    private LocalDateTime calledAt;
    private String verificationQrHash;
    private String skipReason;

    public TokenStatusResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public AccessChannel getAccessChannel() { return accessChannel; }
    public void setAccessChannel(AccessChannel accessChannel) { this.accessChannel = accessChannel; }

    public TokenStatus getStatus() { return status; }
    public void setStatus(TokenStatus status) { this.status = status; }

    public PriorityCategory getPriorityCategory() { return priorityCategory; }
    public void setPriorityCategory(PriorityCategory priorityCategory) { this.priorityCategory = priorityCategory; }

    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }

    public String getAssignedCounterName() { return assignedCounterName; }
    public void setAssignedCounterName(String assignedCounterName) { this.assignedCounterName = assignedCounterName; }

    public String getServedByName() { return servedByName; }
    public void setServedByName(String servedByName) { this.servedByName = servedByName; }

    public int getPeopleAhead() { return peopleAhead; }
    public void setPeopleAhead(int peopleAhead) { this.peopleAhead = peopleAhead; }

    public int getEstimatedWaitingMinutes() { return estimatedWaitingMinutes; }
    public void setEstimatedWaitingMinutes(int estimatedWaitingMinutes) { this.estimatedWaitingMinutes = estimatedWaitingMinutes; }

    public LocalDateTime getEstimatedCallTime() { return estimatedCallTime; }
    public void setEstimatedCallTime(LocalDateTime estimatedCallTime) { this.estimatedCallTime = estimatedCallTime; }

    public String getCurrentServingToken() { return currentServingToken; }
    public void setCurrentServingToken(String currentServingToken) { this.currentServingToken = currentServingToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCalledAt() { return calledAt; }
    public void setCalledAt(LocalDateTime calledAt) { this.calledAt = calledAt; }

    public String getVerificationQrHash() { return verificationQrHash; }
    public void setVerificationQrHash(String verificationQrHash) { this.verificationQrHash = verificationQrHash; }

    public String getSkipReason() { return skipReason; }
    public void setSkipReason(String skipReason) { this.skipReason = skipReason; }
}
