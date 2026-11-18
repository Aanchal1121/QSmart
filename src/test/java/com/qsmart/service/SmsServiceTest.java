package com.qsmart.service;

import com.qsmart.dto.SmsInboundRequest;
import com.qsmart.dto.SmsInboundResponse;
import com.qsmart.model.Department;
import com.qsmart.model.QueueToken;
import com.qsmart.model.ServiceCenter;
import com.qsmart.model.TokenStatus;
import com.qsmart.repository.DepartmentRepository;
import com.qsmart.repository.QueueTokenRepository;
import com.qsmart.repository.SmsMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SmsServiceTest {

    @Mock
    private QueueEngineService queueEngineService;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private QueueTokenRepository tokenRepository;

    @Mock
    private SmsMessageRepository smsMessageRepository;

    @Mock
    private WaitingTimeService waitingTimeService;

    @Mock
    private SmsProvider smsProvider;

    private SmsService smsService;

    @BeforeEach
    void setUp() {
        smsService = new SmsService(
                queueEngineService,
                departmentRepository,
                tokenRepository,
                smsMessageRepository,
                waitingTimeService,
                smsProvider
        );
    }

    @Test
    @DisplayName("Should process HELP command and return clear instructions")
    void testHelpCommand() {
        SmsInboundRequest req = new SmsInboundRequest("+919876543210", "HELP");
        SmsInboundResponse resp = smsService.processInboundSms(req);

        assertTrue(resp.isSuccess());
        assertEquals("HELP", resp.getCommand());
        assertTrue(resp.getReply().contains("BOOK <HOSPITAL> <DEPT>"));
        verify(smsProvider).sendSms(eq("+919876543210"), anyString());
    }

    @Test
    @DisplayName("Should process STATUS command for an existing waiting token")
    void testStatusCommand() {
        ServiceCenter hospital = new ServiceCenter("HOSPITAL01", "City Hospital", "HOSPITAL", "Delhi", "Delhi", "123", "9-5");
        Department dept = new Department("OPD", "General OPD", hospital, 10);
        dept.setId(1L);

        QueueToken token = new QueueToken();
        token.setTokenNumber("OPD-001");
        token.setStatus(TokenStatus.WAITING);
        token.setDepartment(dept);
        token.setSequenceNumber(1);
        token.setTokenDate(LocalDate.now());

        when(tokenRepository.findByTokenNumberIgnoreCase("OPD-001")).thenReturn(Optional.of(token));
        when(waitingTimeService.calculateWaitMinutes(token)).thenReturn(10);
        when(tokenRepository.countWaitingAhead(any(), any(), anyInt())).thenReturn(0L);

        SmsInboundRequest req = new SmsInboundRequest("+919876543210", "STATUS OPD-001");
        SmsInboundResponse resp = smsService.processInboundSms(req);

        assertTrue(resp.isSuccess());
        assertEquals("OPD-001", resp.getTokenNumber());
        assertTrue(resp.getReply().contains("WAITING"));
    }

    @Test
    @DisplayName("Should reject invalid or unknown commands gracefully")
    void testInvalidCommand() {
        SmsInboundRequest req = new SmsInboundRequest("+919876543210", "UNKNOWN_CMD");
        SmsInboundResponse resp = smsService.processInboundSms(req);

        assertFalse(resp.isSuccess());
        assertTrue(resp.getReply().contains("Unknown command"));
    }
}
