package com.qsmart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "departments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"service_center_id", "code"})
})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String code; // e.g. OPD, CARD, ORTHO, PED

    @Column(nullable = false, length = 100)
    private String name; // e.g. General OPD, Cardiology

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_center_id", nullable = false)
    private ServiceCenter serviceCenter;

    @Column(name = "avg_service_duration_minutes", nullable = false)
    private int avgServiceDurationMinutes = 10;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "queue_paused", nullable = false)
    private boolean queuePaused = false;

    @Column(name = "current_delay_minutes", nullable = false)
    private int currentDelayMinutes = 0;

    @Column(name = "delay_reason", length = 255)
    private String delayReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Department() {}

    public Department(String code, String name, ServiceCenter serviceCenter, int avgServiceDurationMinutes) {
        this.code = code;
        this.name = name;
        this.serviceCenter = serviceCenter;
        this.avgServiceDurationMinutes = avgServiceDurationMinutes;
        this.active = true;
        this.queuePaused = false;
        this.currentDelayMinutes = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ServiceCenter getServiceCenter() { return serviceCenter; }
    public void setServiceCenter(ServiceCenter serviceCenter) { this.serviceCenter = serviceCenter; }

    public int getAvgServiceDurationMinutes() { return avgServiceDurationMinutes; }
    public void setAvgServiceDurationMinutes(int avgServiceDurationMinutes) { this.avgServiceDurationMinutes = avgServiceDurationMinutes; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isQueuePaused() { return queuePaused; }
    public void setQueuePaused(boolean queuePaused) { this.queuePaused = queuePaused; }

    public int getCurrentDelayMinutes() { return currentDelayMinutes; }
    public void setCurrentDelayMinutes(int currentDelayMinutes) { this.currentDelayMinutes = currentDelayMinutes; }

    public String getDelayReason() { return delayReason; }
    public void setDelayReason(String delayReason) { this.delayReason = delayReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
