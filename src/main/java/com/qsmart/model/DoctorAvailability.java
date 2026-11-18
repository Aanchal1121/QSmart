package com.qsmart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_availabilities")
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DoctorStatus status = DoctorStatus.AVAILABLE;

    @Column(name = "reported_delay_minutes", nullable = false)
    private int reportedDelayMinutes = 0;

    @Column(name = "delay_reason", length = 255)
    private String delayReason;

    @Column(name = "room_number", length = 50)
    private String roomNumber;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public DoctorAvailability() {}

    public DoctorAvailability(User doctor, Department department, DoctorStatus status, String roomNumber) {
        this.doctor = doctor;
        this.department = department;
        this.status = status;
        this.roomNumber = roomNumber;
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDoctor() { return doctor; }
    public void setDoctor(User doctor) { this.doctor = doctor; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public DoctorStatus getStatus() { return status; }
    public void setStatus(DoctorStatus status) { this.status = status; }

    public int getReportedDelayMinutes() { return reportedDelayMinutes; }
    public void setReportedDelayMinutes(int reportedDelayMinutes) { this.reportedDelayMinutes = reportedDelayMinutes; }

    public String getDelayReason() { return delayReason; }
    public void setDelayReason(String delayReason) { this.delayReason = delayReason; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
