package com.qsmart.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queue_tokens", indexes = {
    @Index(name = "idx_token_date_dept", columnList = "token_date, department_id"),
    @Index(name = "idx_token_status", columnList = "status"),
    @Index(name = "idx_token_phone", columnList = "patient_phone"),
    @Index(name = "idx_token_number", columnList = "token_number")
})
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_number", nullable = false, length = 30)
    private String tokenNumber; // e.g. "OPD-001"

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user; // null for offline walk-ins without user account

    @Column(name = "patient_name", nullable = false, length = 100)
    private String patientName;

    @Column(name = "patient_phone", nullable = false, length = 20)
    private String patientPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_channel", nullable = false, length = 20)
    private AccessChannel accessChannel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TokenStatus status = TokenStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority_category", nullable = false, length = 30)
    private PriorityCategory priorityCategory = PriorityCategory.NORMAL;

    @Column(name = "priority_reason", length = 255)
    private String priorityReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_counter_id")
    private ServiceCounter assignedCounter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "served_by_user_id")
    private User servedBy;

    @Column(name = "called_at")
    private LocalDateTime calledAt;

    @Column(name = "served_at")
    private LocalDateTime servedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "skipped_at")
    private LocalDateTime skippedAt;

    @Column(name = "skip_reason", length = 255)
    private String skipReason;

    @Column(name = "token_date", nullable = false)
    private LocalDate tokenDate = LocalDate.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "verification_qr_hash", nullable = false, unique = true, length = 64)
    private String verificationQrHash = UUID.randomUUID().toString();

    @Column(length = 255)
    private String notes;

    public QueueToken() {}

    public QueueToken(String tokenNumber, int sequenceNumber, Department department, User user,
                      String patientName, String patientPhone, AccessChannel accessChannel,
                      PriorityCategory priorityCategory, String priorityReason) {
        this.tokenNumber = tokenNumber;
        this.sequenceNumber = sequenceNumber;
        this.department = department;
        this.user = user;
        this.patientName = patientName;
        this.patientPhone = patientPhone;
        this.accessChannel = accessChannel;
        this.priorityCategory = priorityCategory != null ? priorityCategory : PriorityCategory.NORMAL;
        this.priorityReason = priorityReason;
        this.status = TokenStatus.WAITING;
        this.tokenDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.verificationQrHash = UUID.randomUUID().toString();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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

    public ServiceCounter getAssignedCounter() { return assignedCounter; }
    public void setAssignedCounter(ServiceCounter assignedCounter) { this.assignedCounter = assignedCounter; }

    public User getServedBy() { return servedBy; }
    public void setServedBy(User servedBy) { this.servedBy = servedBy; }

    public LocalDateTime getCalledAt() { return calledAt; }
    public void setCalledAt(LocalDateTime calledAt) { this.calledAt = calledAt; }

    public LocalDateTime getServedAt() { return servedAt; }
    public void setServedAt(LocalDateTime servedAt) { this.servedAt = servedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public LocalDateTime getSkippedAt() { return skippedAt; }
    public void setSkippedAt(LocalDateTime skippedAt) { this.skippedAt = skippedAt; }

    public String getSkipReason() { return skipReason; }
    public void setSkipReason(String skipReason) { this.skipReason = skipReason; }

    public LocalDate getTokenDate() { return tokenDate; }
    public void setTokenDate(LocalDate tokenDate) { this.tokenDate = tokenDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getVerificationQrHash() { return verificationQrHash; }
    public void setVerificationQrHash(String verificationQrHash) { this.verificationQrHash = verificationQrHash; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
