package com.qsmart.service;

import com.qsmart.dto.SmsInboundRequest;
import com.qsmart.dto.SmsInboundResponse;
import com.qsmart.dto.TokenBookingRequest;
import com.qsmart.model.*;
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
public class SmsEngineDeepTest {

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
    private Department testDept;
    private ServiceCenter testHospital;

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

        testHospital = new ServiceCenter("HOSPITAL01", "City Hospital", "HOSPITAL", "Delhi", "Delhi", "123", "8-8");
        testDept = new Department("OPD", "General OPD", testHospital, 10);
        testDept.setId(1L);
    }

    @Test
    @DisplayName("SMS BOOK: Case-insensitive command parsing books token and replies with confirmation")
    void testBookCommandCaseInsensitive() {
        when(departmentRepository.findByServiceCenterCodeIgnoreCaseAndCodeIgnoreCase(anyString(), anyString()))
                .thenReturn(Optional.of(testDept));

        QueueToken booked = new QueueToken("OPD-007", 7, testDept, null, "SMS User", "9876543210", AccessChannel.SMS, PriorityCategory.NORMAL, null);
        when(queueEngineService.bookToken(any(TokenBookingRequest.class), isNull())).thenReturn(booked);
        when(waitingTimeService.calculateWaitMinutes(any())).thenReturn(15);
        when(tokenRepository.countWaitingAhead(any(), any(), anyInt())).thenReturn(2L);

        // Test with extra whitespaces and lowercase
        SmsInboundRequest req = new SmsInboundRequest("9876543210", "  book   hospital01   opd  ");
        SmsInboundResponse resp = smsService.processInboundSms(req);

        assertTrue(resp.isSuccess(), "Expected success but got reply: " + resp.getReply());
        assertEquals("BOOK", resp.getCommand());
        assertEquals("OPD-007", resp.getTokenNumber());
        assertTrue(resp.getReply().contains("OPD-007"));
        assertTrue(resp.getReply().contains("Ahead: 2"));
        assertTrue(resp.getReply().contains("~15 min"));
    }

    @Test
    @DisplayName("SMS CANCEL: Valid token cancels queue booking via SMS")
    void testCancelCommand() {
        QueueToken token = new QueueToken("OPD-005", 5, testDept, null, "SMS User", "9876543210", AccessChannel.SMS, PriorityCategory.NORMAL, null);
        token.setId(55L);
        token.setStatus(TokenStatus.WAITING);

        when(tokenRepository.findByTokenNumberIgnoreCase("OPD-005")).thenReturn(Optional.of(token));

        QueueToken cancelled = new QueueToken("OPD-005", 5, testDept, null, "SMS User", "9876543210", AccessChannel.SMS, PriorityCategory.NORMAL, null);
        cancelled.setStatus(TokenStatus.CANCELLED);
        when(queueEngineService.cancelToken(eq(55L), isNull(), contains("via SMS"))).thenReturn(cancelled);

        SmsInboundRequest req = new SmsInboundRequest("9876543210", "CANCEL OPD-005");
        SmsInboundResponse resp = smsService.processInboundSms(req);

        assertTrue(resp.isSuccess());
        assertEquals("CANCEL", resp.getCommand());
        assertTrue(resp.getReply().contains("cancelled successfully"));
    }

    @Test
    @DisplayName("SMS STATUS: Non-existent token returns clear friendly error message")
    void testStatusNotFound() {
        when(tokenRepository.findByTokenNumberIgnoreCase("NONEXISTENT-999")).thenReturn(Optional.empty());

        SmsInboundRequest req = new SmsInboundRequest("9876543210", "STATUS NONEXISTENT-999");
        SmsInboundResponse resp = smsService.processInboundSms(req);

        assertFalse(resp.isSuccess());
        assertTrue(resp.getReply().contains("not found"));
    }

    @Test
    @DisplayName("SMS UNKNOWN: Gibberish text responds with HELP commands list")
    void testUnknownCommandReturnsHelp() {
        SmsInboundRequest req = new SmsInboundRequest("9876543210", "HELLO ANYONE THERE");
        SmsInboundResponse resp = smsService.processInboundSms(req);

        assertFalse(resp.isSuccess());
        assertEquals("HELLO", resp.getCommand());
        assertTrue(resp.getReply().contains("Unknown command"));
        assertTrue(resp.getReply().contains("BOOK <HOSPITAL> <DEPT>"));
    }
}
