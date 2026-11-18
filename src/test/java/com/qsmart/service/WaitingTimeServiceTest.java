package com.qsmart.service;

import com.qsmart.model.Department;
import com.qsmart.model.QueueToken;
import com.qsmart.model.ServiceCenter;
import com.qsmart.repository.DoctorAvailabilityRepository;
import com.qsmart.repository.QueueTokenRepository;
import com.qsmart.repository.ServiceCounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WaitingTimeServiceTest {

    @Mock
    private QueueTokenRepository tokenRepository;

    @Mock
    private ServiceCounterRepository counterRepository;

    @Mock
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    private WaitingTimeService waitingTimeService;

    @BeforeEach
    void setUp() {
        waitingTimeService = new WaitingTimeService(tokenRepository, counterRepository, doctorAvailabilityRepository);
    }

    @Test
    @DisplayName("Should calculate estimated waiting time accurately using statistical formula")
    void testCalculateWaitMinutes() {
        ServiceCenter hospital = new ServiceCenter("HOSPITAL01", "City Hospital", "HOSPITAL", "Delhi", "Delhi", "123", "9-5");
        Department dept = new Department("OPD", "General OPD", hospital, 10);
        dept.setId(1L);
        dept.setCurrentDelayMinutes(5);

        QueueToken token = new QueueToken();
        token.setId(10L);
        token.setDepartment(dept);
        token.setSequenceNumber(4);
        token.setTokenDate(LocalDate.now());

        // 3 people ahead
        when(tokenRepository.countWaitingAhead(eq(1L), any(LocalDate.class), eq(4))).thenReturn(3L);
        // 2 active counters
        when(counterRepository.countByDepartmentIdAndStatus(eq(1L), any())).thenReturn(1L);

        // Raw wait = (3 * 10) / 2 = 15 mins. Total wait = 15 + 5 (delay) = 20 mins.
        int waitTime = waitingTimeService.calculateWaitMinutes(token);

        assertEquals(20, waitTime);
    }

    @Test
    @DisplayName("Should safely handle zero active counters by defaulting to 1 without dividing by zero")
    void testCalculateWaitMinutesWithZeroCounters() {
        ServiceCenter hospital = new ServiceCenter("HOSPITAL01", "City Hospital", "HOSPITAL", "Delhi", "Delhi", "123", "9-5");
        Department dept = new Department("OPD", "General OPD", hospital, 10);
        dept.setId(1L);
        dept.setCurrentDelayMinutes(0);

        QueueToken token = new QueueToken();
        token.setId(10L);
        token.setDepartment(dept);
        token.setSequenceNumber(2);
        token.setTokenDate(LocalDate.now());

        // 1 person ahead
        when(tokenRepository.countWaitingAhead(eq(1L), any(LocalDate.class), eq(2))).thenReturn(1L);
        // 0 counters and 0 doctors
        when(counterRepository.countByDepartmentIdAndStatus(eq(1L), any())).thenReturn(0L);

        int waitTime = waitingTimeService.calculateWaitMinutes(token);

        // (1 * 10) / 1 = 10 mins
        assertEquals(10, waitTime);
    }
}
