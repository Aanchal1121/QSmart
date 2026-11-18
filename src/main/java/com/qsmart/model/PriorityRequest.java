package com.qsmart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "priority_requests")
public class PriorityRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "token_id", nullable = false)
    private QueueToken token;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_category", nullable = false, length = 30)
    private PriorityCategory requestedCategory;

    @Column(nullable = false, length = 255)
    private String reason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "action_notes", length = 255)
    private String actionNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PriorityRequest() {}

    public PriorityRequest(QueueToken token, PriorityCategory requestedCategory, String reason, User requestedBy) {
        this.token = token;
        this.requestedCategory = requestedCategory;
        this.reason = reason;
        this.requestedBy = requestedBy;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QueueToken getToken() { return token; }
    public void setToken(QueueToken token) { this.token = token; }

    public PriorityCategory getRequestedCategory() { return requestedCategory; }
    public void setRequestedCategory(PriorityCategory requestedCategory) { this.requestedCategory = requestedCategory; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }

    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getActionNotes() { return actionNotes; }
    public void setActionNotes(String actionNotes) { this.actionNotes = actionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
