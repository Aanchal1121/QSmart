package com.qsmart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_messages", indexes = {
    @Index(name = "idx_sms_phone", columnList = "phone")
})
public class SmsMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, length = 20)
    private String direction; // INBOUND, OUTBOUND

    @Column(name = "raw_message", nullable = false, length = 500)
    private String rawMessage;

    @Column(name = "processed_command", length = 50)
    private String processedCommand;

    @Column(length = 30)
    private String status; // RECEIVED, PROCESSED, FAILED, SENT

    @Column(name = "reply_message", length = 500)
    private String replyMessage;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public SmsMessage() {}

    public SmsMessage(String phone, String direction, String rawMessage, String processedCommand, String status, String replyMessage) {
        this.phone = phone;
        this.direction = direction;
        this.rawMessage = rawMessage;
        this.processedCommand = processedCommand;
        this.status = status;
        this.replyMessage = replyMessage;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getRawMessage() { return rawMessage; }
    public void setRawMessage(String rawMessage) { this.rawMessage = rawMessage; }

    public String getProcessedCommand() { return processedCommand; }
    public void setProcessedCommand(String processedCommand) { this.processedCommand = processedCommand; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReplyMessage() { return replyMessage; }
    public void setReplyMessage(String replyMessage) { this.replyMessage = replyMessage; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
