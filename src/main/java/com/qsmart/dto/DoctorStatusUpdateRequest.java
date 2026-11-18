package com.qsmart.dto;

import com.qsmart.model.DoctorStatus;
import jakarta.validation.constraints.NotNull;

public class DoctorStatusUpdateRequest {
    @NotNull(message = "Doctor status is required")
    private DoctorStatus status;

    private int reportedDelayMinutes = 0;
    private String delayReason;

    public DoctorStatusUpdateRequest() {}

    public DoctorStatus getStatus() { return status; }
    public void setStatus(DoctorStatus status) { this.status = status; }

    public int getReportedDelayMinutes() { return reportedDelayMinutes; }
    public void setReportedDelayMinutes(int reportedDelayMinutes) { this.reportedDelayMinutes = reportedDelayMinutes; }

    public String getDelayReason() { return delayReason; }
    public void setDelayReason(String delayReason) { this.delayReason = delayReason; }
}
