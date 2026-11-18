package com.qsmart.service;

import com.qsmart.model.CounterStatus;
import com.qsmart.model.Department;
import com.qsmart.model.DoctorStatus;
import com.qsmart.model.QueueToken;
import com.qsmart.repository.DoctorAvailabilityRepository;
import com.qsmart.repository.QueueTokenRepository;
import com.qsmart.repository.ServiceCounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class WaitingTimeService {

    private final QueueTokenRepository tokenRepository;
    private final ServiceCounterRepository counterRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Autowired
    public WaitingTimeService(QueueTokenRepository tokenRepository,
                              ServiceCounterRepository counterRepository,
                              DoctorAvailabilityRepository doctorAvailabilityRepository) {
        this.tokenRepository = tokenRepository;
        this.counterRepository = counterRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
    }

    /**
     * Calculates the estimated waiting time in minutes for a specific token in queue.
     * Formula:
     * WaitTime = max(0, round((PeopleAhead * AvgDuration) / ActiveCounters) + DepartmentDelay)
     */
    public int calculateWaitMinutes(QueueToken token) {
        if (token == null || token.getDepartment() == null) {
            return 0;
        }

        Department department = token.getDepartment();
        LocalDate today = token.getTokenDate() != null ? token.getTokenDate() : LocalDate.now();

        // 1. Count waiting tokens ahead with a lower sequence number
        long peopleAhead = tokenRepository.countWaitingAhead(department.getId(), today, token.getSequenceNumber());

        // 2. Count active service counters and active doctors
        int activeCounters = getActiveCounterCount(department.getId());

        // 3. Department average service duration
        int avgDuration = Math.max(1, department.getAvgServiceDurationMinutes());

        // 4. Department reported delay
        int delayMinutes = Math.max(0, department.getCurrentDelayMinutes());

        // 5. Calculation
        double rawWait = ((double) peopleAhead * avgDuration) / (double) Math.max(1, activeCounters);
        int totalWait = (int) Math.round(rawWait) + delayMinutes;

        return Math.max(0, totalWait);
    }

    /**
     * Calculates estimated call time
     */
    public LocalDateTime estimateCallTime(QueueToken token) {
        int waitMinutes = calculateWaitMinutes(token);
        return LocalDateTime.now().plusMinutes(waitMinutes);
    }

    /**
     * Get count of active service counters or doctors for department
     */
    public int getActiveCounterCount(Long departmentId) {
        long availableCounters = counterRepository.countByDepartmentIdAndStatus(departmentId, CounterStatus.AVAILABLE);
        long busyCounters = counterRepository.countByDepartmentIdAndStatus(departmentId, CounterStatus.BUSY);
        long activeCounters = availableCounters + busyCounters;

        if (activeCounters > 0) {
            return (int) activeCounters;
        }

        // Check active doctors as fallback
        long activeDoctors = doctorAvailabilityRepository.findByDepartmentIdAndStatus(departmentId, DoctorStatus.AVAILABLE).size()
                           + doctorAvailabilityRepository.findByDepartmentIdAndStatus(departmentId, DoctorStatus.IN_CONSULTATION).size();

        return Math.max(1, (int) activeDoctors);
    }
}
