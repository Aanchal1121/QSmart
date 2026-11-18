package com.qsmart.dto;

import com.qsmart.model.AccessChannel;
import com.qsmart.model.PriorityCategory;
import jakarta.validation.constraints.NotNull;

public class TokenBookingRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private String patientName;
    private String patientPhone;
    private AccessChannel accessChannel = AccessChannel.SMARTPHONE;
    private PriorityCategory priorityCategory = PriorityCategory.NORMAL;
    private String priorityReason;
    private String notes;

    public TokenBookingRequest() {}

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public AccessChannel getAccessChannel() { return accessChannel; }
    public void setAccessChannel(AccessChannel accessChannel) { this.accessChannel = accessChannel; }

    public PriorityCategory getPriorityCategory() { return priorityCategory; }
    public void setPriorityCategory(PriorityCategory priorityCategory) { this.priorityCategory = priorityCategory; }

    public String getPriorityReason() { return priorityReason; }
    public void setPriorityReason(String priorityReason) { this.priorityReason = priorityReason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
