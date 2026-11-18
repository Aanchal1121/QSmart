package com.qsmart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "token_histories")
public class TokenHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_token_id", nullable = false)
    private QueueToken queueToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private TokenStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private TokenStatus newStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

    @Column(length = 255)
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public TokenHistory() {}

    public TokenHistory(QueueToken queueToken, TokenStatus previousStatus, TokenStatus newStatus, User changedBy, String remarks) {
        this.queueToken = queueToken;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.remarks = remarks;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public QueueToken getQueueToken() { return queueToken; }
    public void setQueueToken(QueueToken queueToken) { this.queueToken = queueToken; }

    public TokenStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(TokenStatus previousStatus) { this.previousStatus = previousStatus; }

    public TokenStatus getNewStatus() { return newStatus; }
    public void setNewStatus(TokenStatus newStatus) { this.newStatus = newStatus; }

    public User getChangedBy() { return changedBy; }
    public void setChangedBy(User changedBy) { this.changedBy = changedBy; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
