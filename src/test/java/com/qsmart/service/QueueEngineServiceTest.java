package com.qsmart.service;

import com.qsmart.dto.TokenBookingRequest;
import com.qsmart.model.*;
import com.qsmart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueueEngineServiceTest {

    @Mock
    private QueueTokenRepository tokenRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ServiceCounterRepository counterRepository;

    @Mock
    private TokenHistoryRepository historyRepository;

    @Mock
    private WaitingTimeService waitingTimeService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SmsProvider smsProvider;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private QueueEngineService queueEngineService;

    @BeforeEach
    void setUp() {
        queueEngineService = new QueueEngineService(
                tokenRepository,
                departmentRepository,
                counterRepository,
                historyRepository,
                waitingTimeService,
                notificationService,
                smsProvider,
                auditLogService,
                messagingTemplate
        );
    }

    @Test
    @DisplayName("Should prevent duplicate active booking in the same department on the same day")
    void testPreventDuplicateActiveBooking() {
        ServiceCenter hospital = new ServiceCenter("HOSPITAL01", "City Hospital", "HOSPITAL", "Delhi", "Delhi", "123", "9-5");
        Department dept = new Department("OPD", "General OPD", hospital, 10);
        dept.setId(1L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));

        QueueToken existingToken = new QueueToken();
        existingToken.setTokenNumber("OPD-001");
        existingToken.setStatus(TokenStatus.WAITING);

        when(tokenRepository.findActiveTokensByPhoneAndDepartment(eq("9876543210"), eq(1L), any(LocalDate.class), anyCollection()))
                .thenReturn(List.of(existingToken));

        TokenBookingRequest req = new TokenBookingRequest();
        req.setDepartmentId(1L);
        req.setPatientPhone("9876543210");
        req.setPatientName("Test Patient");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            queueEngineService.bookToken(req, null);
        });

        assertTrue(ex.getMessage().contains("already have an active token"));
    }

    @Test
    @DisplayName("Should generate sequential token number in FIFO order")
    void testBookTokenGeneratesSequentialNumber() {
        ServiceCenter hospital = new ServiceCenter("HOSPITAL01", "City Hospital", "HOSPITAL", "Delhi", "Delhi", "123", "9-5");
        Department dept = new Department("OPD", "General OPD", hospital, 10);
        dept.setId(1L);

        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(tokenRepository.findActiveTokensByPhoneAndDepartment(anyString(), eq(1L), any(LocalDate.class), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(tokenRepository.findMaxSequenceNumber(eq(1L), any(LocalDate.class))).thenReturn(4);

        when(tokenRepository.save(any(QueueToken.class))).thenAnswer(invocation -> {
            QueueToken t = invocation.getArgument(0);
            t.setId(105L);
            return t;
        });

        TokenBookingRequest req = new TokenBookingRequest();
        req.setDepartmentId(1L);
        req.setPatientPhone("9876543210");
        req.setPatientName("New Patient");

        QueueToken booked = queueEngineService.bookToken(req, null);

        assertNotNull(booked);
        assertEquals("OPD-005", booked.getTokenNumber());
        assertEquals(5, booked.getSequenceNumber());
        assertEquals(TokenStatus.WAITING, booked.getStatus());
        verify(historyRepository).save(any(TokenHistory.class));
    }

    @Test
    @DisplayName("Should skip absent token with mandatory reason and free the counter")
    void testSkipTokenRequiresReason() {
        QueueToken token = new QueueToken();
        token.setId(20L);
        token.setTokenNumber("OPD-002");
        token.setStatus(TokenStatus.CALLED);

        ServiceCenter hospital = new ServiceCenter("HOSPITAL01", "City Hospital", "HOSPITAL", "Delhi", "Delhi", "123", "9-5");
        Department dept = new Department("OPD", "General OPD", hospital, 10);
        dept.setId(1L);
        token.setDepartment(dept);

        ServiceCounter counter = new ServiceCounter("Counter 1", dept, null, null);
        counter.setCurrentTokenId(20L);
        counter.setStatus(CounterStatus.BUSY);
        token.setAssignedCounter(counter);

        when(tokenRepository.findById(20L)).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(QueueToken.class))).thenAnswer(i -> i.getArgument(0));

        // Skip token
        QueueToken skipped = queueEngineService.skipToken(20L, "Patient absent after 3 calls", null);

        assertEquals(TokenStatus.SKIPPED, skipped.getStatus());
        assertEquals("Patient absent after 3 calls", skipped.getSkipReason());
        assertEquals(CounterStatus.AVAILABLE, counter.getStatus());
        assertNull(counter.getCurrentTokenId());
        verify(historyRepository).save(any(TokenHistory.class));
    }
}
