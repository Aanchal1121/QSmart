package com.qsmart.service;

import com.qsmart.dto.TokenBookingRequest;
import com.qsmart.model.*;
import com.qsmart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QueueEngineDeepVerificationTest {

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
    private Department testDept;
    private ServiceCenter testHospital;

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

        testHospital = new ServiceCenter("HOSPITAL01", "Apex City Hospital", "HOSPITAL", "New Delhi", "New Delhi", "+91-11-23456789", "08:00 AM - 08:00 PM");
        testDept = new Department("OPD", "General OPD", testHospital, 12);
        testDept.setId(1L);
        testDept.setActive(true);
        testDept.setQueuePaused(false);
    }

    @Test
    @DisplayName("Centralized FIFO: Smartphone, SMS, and Walk-in all join the SAME queue in strict sequence")
    void testCentralizedFifoAcrossChannels() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDept));
        when(tokenRepository.findActiveTokensByPhoneAndDepartment(anyString(), eq(1L), any(LocalDate.class), anyCollection()))
                .thenReturn(Collections.emptyList());

        // Token 1: SMARTPHONE
        when(tokenRepository.findMaxSequenceNumber(eq(1L), any(LocalDate.class))).thenReturn(0);
        when(tokenRepository.save(any(QueueToken.class))).thenAnswer(i -> {
            QueueToken t = i.getArgument(0);
            if (t.getId() == null) {
                t.setId(101L);
            }
            return t;
        });

        TokenBookingRequest req1 = new TokenBookingRequest();
        req1.setDepartmentId(1L);
        req1.setPatientName("Smartphone Patient");
        req1.setPatientPhone("9876543211");
        req1.setAccessChannel(AccessChannel.SMARTPHONE);

        QueueToken t1 = queueEngineService.bookToken(req1, null);
        assertEquals("OPD-001", t1.getTokenNumber());
        assertEquals(1, t1.getSequenceNumber());
        assertEquals(AccessChannel.SMARTPHONE, t1.getAccessChannel());

        // Token 2: SMS Keypad Phone
        when(tokenRepository.findMaxSequenceNumber(eq(1L), any(LocalDate.class))).thenReturn(1);
        TokenBookingRequest req2 = new TokenBookingRequest();
        req2.setDepartmentId(1L);
        req2.setPatientName("SMS Patient");
        req2.setPatientPhone("9876543212");
        req2.setAccessChannel(AccessChannel.SMS);

        QueueToken t2 = queueEngineService.bookToken(req2, null);
        assertEquals("OPD-002", t2.getTokenNumber());
        assertEquals(2, t2.getSequenceNumber());
        assertEquals(AccessChannel.SMS, t2.getAccessChannel());

        // Token 3: Walk-in counter
        when(tokenRepository.findMaxSequenceNumber(eq(1L), any(LocalDate.class))).thenReturn(2);
        TokenBookingRequest req3 = new TokenBookingRequest();
        req3.setDepartmentId(1L);
        req3.setPatientName("Walk-in Patient");
        req3.setPatientPhone("9876543213");
        req3.setAccessChannel(AccessChannel.WALK_IN);

        QueueToken t3 = queueEngineService.bookToken(req3, null);
        assertEquals("OPD-003", t3.getTokenNumber());
        assertEquals(3, t3.getSequenceNumber());
        assertEquals(AccessChannel.WALK_IN, t3.getAccessChannel());

        // All 3 tokens belong to the exact same department and queue sequence
        assertEquals(t1.getDepartment().getId(), t2.getDepartment().getId());
        assertEquals(t2.getDepartment().getId(), t3.getDepartment().getId());
    }

    @Test
    @DisplayName("Duplicate Prevention: Cannot book second active token in same department today")
    void testDuplicateActiveTokenPrevention() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDept));

        QueueToken existing = new QueueToken();
        existing.setTokenNumber("OPD-001");
        existing.setStatus(TokenStatus.WAITING);

        when(tokenRepository.findActiveTokensByPhoneAndDepartment(eq("9876543210"), eq(1L), any(LocalDate.class), anyCollection()))
                .thenReturn(List.of(existing));

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
    @DisplayName("FIFO Fairness: Call next picks the first waiting token when no priority is flagged")
    void testCallNextStrictFifo() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDept));

        QueueToken t1 = new QueueToken("OPD-001", 1, testDept, null, "P1", "111", AccessChannel.SMARTPHONE, PriorityCategory.NORMAL, null);
        t1.setId(1L);
        t1.setStatus(TokenStatus.WAITING);

        QueueToken t2 = new QueueToken("OPD-002", 2, testDept, null, "P2", "222", AccessChannel.SMS, PriorityCategory.NORMAL, null);
        t2.setId(2L);
        t2.setStatus(TokenStatus.WAITING);

        when(tokenRepository.findByDepartmentIdAndTokenDateAndStatusOrderBySequenceNumberAsc(eq(1L), any(LocalDate.class), eq(TokenStatus.WAITING)))
                .thenReturn(List.of(t1, t2));
        when(tokenRepository.save(any(QueueToken.class))).thenAnswer(i -> {
            QueueToken t = i.getArgument(0);
            if (t.getId() == null) {
                t.setId(101L);
            }
            return t;
        });

        ServiceCounter counter = new ServiceCounter("Counter 1", testDept, null, null);
        counter.setId(10L);
        when(counterRepository.findById(10L)).thenReturn(Optional.of(counter));

        User staff = new User();
        staff.setName("Staff Member");

        QueueToken called = queueEngineService.callNext(1L, 10L, staff, null);

        assertEquals("OPD-001", called.getTokenNumber());
        assertEquals(TokenStatus.CALLED, called.getStatus());
        assertEquals(CounterStatus.BUSY, counter.getStatus());
        assertEquals(1L, counter.getCurrentTokenId());
        verify(auditLogService).log(eq("CALL_NEXT"), anyString(), eq("QueueToken"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Priority Exception: Authorized priority tokens take precedence with transparent audit logging")
    void testPriorityExceptionAudited() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDept));

        QueueToken standard = new QueueToken("OPD-001", 1, testDept, null, "Standard P1", "111", AccessChannel.SMARTPHONE, PriorityCategory.NORMAL, null);
        standard.setId(1L);
        standard.setStatus(TokenStatus.WAITING);

        QueueToken senior = new QueueToken("OPD-002", 2, testDept, null, "Senior Citizen P2", "222", AccessChannel.WALK_IN, PriorityCategory.SENIOR_CITIZEN, "Age 78 verified at desk");
        senior.setId(2L);
        senior.setStatus(TokenStatus.WAITING);

        when(tokenRepository.findByDepartmentIdAndTokenDateAndStatusOrderBySequenceNumberAsc(eq(1L), any(LocalDate.class), eq(TokenStatus.WAITING)))
                .thenReturn(List.of(standard, senior));
        when(tokenRepository.save(any(QueueToken.class))).thenAnswer(i -> {
            QueueToken t = i.getArgument(0);
            if (t.getId() == null) {
                t.setId(101L);
            }
            return t;
        });

        QueueToken called = queueEngineService.callNext(1L, null, null, null);

        // Senior Citizen priority token called first
        assertEquals("OPD-002", called.getTokenNumber());
        assertEquals(PriorityCategory.SENIOR_CITIZEN, called.getPriorityCategory());
        assertEquals(TokenStatus.CALLED, called.getStatus());
        verify(auditLogService).log(eq("CALL_NEXT"), anyString(), eq("QueueToken"), eq("2"), anyString());
    }

    @Test
    @DisplayName("Mandatory Skip Reason: Skipping without a reason is strictly rejected")
    void testMandatorySkipReason() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> {
            queueEngineService.skipToken(1L, null, null);
        });
        assertTrue(ex1.getMessage().contains("reason is required"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> {
            queueEngineService.skipToken(1L, "   ", null);
        });
        assertTrue(ex2.getMessage().contains("reason is required"));
    }

    @Test
    @DisplayName("Token State Transition: Marking served transitions state and frees counter")
    void testMarkServedTransition() {
        QueueToken token = new QueueToken("OPD-001", 1, testDept, null, "Patient", "123", AccessChannel.SMARTPHONE, PriorityCategory.NORMAL, null);
        token.setId(15L);
        token.setStatus(TokenStatus.CALLED);

        ServiceCounter counter = new ServiceCounter("Counter 2", testDept, null, null);
        counter.setCurrentTokenId(15L);
        counter.setStatus(CounterStatus.BUSY);
        token.setAssignedCounter(counter);

        when(tokenRepository.findById(15L)).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(QueueToken.class))).thenAnswer(i -> {
            QueueToken t = i.getArgument(0);
            if (t.getId() == null) {
                t.setId(101L);
            }
            return t;
        });

        QueueToken served = queueEngineService.markServed(15L, null);

        assertEquals(TokenStatus.SERVED, served.getStatus());
        assertNotNull(served.getServedAt());
        assertEquals(CounterStatus.AVAILABLE, counter.getStatus());
        assertNull(counter.getCurrentTokenId());
        verify(historyRepository).save(any(TokenHistory.class));
        verify(auditLogService).log(eq("MARK_SERVED"), anyString(), eq("QueueToken"), eq("15"), anyString());
    }

    @Test
    @DisplayName("Invalid Cancellation: Cannot cancel an already SERVED or CANCELLED token")
    void testInvalidTokenCancellation() {
        QueueToken servedToken = new QueueToken();
        servedToken.setId(25L);
        servedToken.setTokenNumber("OPD-025");
        servedToken.setStatus(TokenStatus.SERVED);

        when(tokenRepository.findById(25L)).thenReturn(Optional.of(servedToken));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            queueEngineService.cancelToken(25L, null, "Too late");
        });

        assertTrue(ex.getMessage().contains("cannot be cancelled"));
    }

    @Test
    @DisplayName("Privacy & Security: QR Hash is an opaque UUID and does not leak patient PII")
    void testQrHashOpaqueAndNonPii() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDept));
        when(tokenRepository.findActiveTokensByPhoneAndDepartment(anyString(), eq(1L), any(LocalDate.class), anyCollection()))
                .thenReturn(Collections.emptyList());
        when(tokenRepository.findMaxSequenceNumber(eq(1L), any(LocalDate.class))).thenReturn(0);
        when(tokenRepository.save(any(QueueToken.class))).thenAnswer(i -> {
            QueueToken t = i.getArgument(0);
            if (t.getId() == null) {
                t.setId(101L);
            }
            return t;
        });

        TokenBookingRequest req = new TokenBookingRequest();
        req.setDepartmentId(1L);
        req.setPatientName("Ramesh Kumar Confidential");
        req.setPatientPhone("+919876543210");

        QueueToken token = queueEngineService.bookToken(req, null);

        String qrHash = token.getVerificationQrHash();
        assertNotNull(qrHash);
        // Verify it is standard 36-char UUID format (opaque)
        assertEquals(36, qrHash.length());
        assertFalse(qrHash.contains("Ramesh"));
        assertFalse(qrHash.contains("9876543210"));
    }
}
