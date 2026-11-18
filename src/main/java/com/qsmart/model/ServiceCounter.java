package com.qsmart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_counters")
public class ServiceCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "counter_number", nullable = false, length = 50)
    private String counterNumber; // e.g. "Counter 1", "OPD Room 102"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_user_id")
    private User staff;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_user_id")
    private User doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CounterStatus status = CounterStatus.AVAILABLE;

    @Column(name = "current_token_id")
    private Long currentTokenId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ServiceCounter() {}

    public ServiceCounter(String counterNumber, Department department, User staff, User doctor) {
        this.counterNumber = counterNumber;
        this.department = department;
        this.staff = staff;
        this.doctor = doctor;
        this.status = CounterStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCounterNumber() { return counterNumber; }
    public void setCounterNumber(String counterNumber) { this.counterNumber = counterNumber; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }

    public User getDoctor() { return doctor; }
    public void setDoctor(User doctor) { this.doctor = doctor; }

    public CounterStatus getStatus() { return status; }
    public void setStatus(CounterStatus status) { this.status = status; }

    public Long getCurrentTokenId() { return currentTokenId; }
    public void setCurrentTokenId(Long currentTokenId) { this.currentTokenId = currentTokenId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
