package com.qsmart.dto;

import jakarta.validation.constraints.NotNull;

public class DelayReportRequest {
    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private int delayMinutes;
    private String reason;

    public DelayReportRequest() {}

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
