package com.qsmart.service;

import com.qsmart.dto.DelayReportRequest;
import com.qsmart.dto.DoctorStatusUpdateRequest;
import com.qsmart.model.*;
import com.qsmart.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AvailabilityAndDelayTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Mock
    private ServiceCounterRepository counterRepository;

    @Mock
    private QueueTokenRepository tokenRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SmsProvider smsProvider;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private QueueEngineService queueEngineService;

    private AvailabilityService availabilityService;
    private Department testDept;
    private ServiceCenter testHospital;
    private User testDoctor;

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService(
                departmentRepository,
                doctorAvailabilityRepository,
                counterRepository,
                tokenRepository,
                notificationService,
                smsProvider,
                auditLogService,
                queueEngineService
        );

        testHospital = new ServiceCenter("HOSPITAL01", "City Hospital", "HOSPITAL", "Delhi", "Delhi", "123", "8-8");
        testDept = new Department("OPD", "General OPD", testHospital, 10);
        testDept.setId(1L);

        testDoctor = new User();
        testDoctor.setId(5L);
        testDoctor.setName("Dr. A. K. Sharma");
    }

    @Test
    @DisplayName("Queue Pause/Resume: Toggles department queue paused state with audit logging")
    void testToggleQueuePause() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDept));
        when(departmentRepository.save(any(Department.class))).thenAnswer(i -> i.getArgument(0));

        Department paused = availabilityService.toggleQueuePause(1L, true, testDoctor);
        assertTrue(paused.isQueuePaused());
        verify(auditLogService).log(eq("PAUSE_QUEUE"), eq("Dr. A. K. Sharma"), eq("Department"), eq("1"), anyString());
        verify(queueEngineService).broadcastQueueUpdate(1L);

        Department resumed = availabilityService.toggleQueuePause(1L, false, testDoctor);
        assertFalse(resumed.isQueuePaused());
        verify(auditLogService).log(eq("RESUME_QUEUE"), eq("Dr. A. K. Sharma"), eq("Department"), eq("1"), anyString());
    }

    @Test
    @DisplayName("Emergency Delay: Doctor reported delay cascades to department delayMinutes and broadcasts alert")
    void testDoctorEmergencyDelayCascadesToDepartment() {
        DoctorAvailability docAvail = new DoctorAvailability(testDoctor, testDept, DoctorStatus.AVAILABLE, "Room 102");
        docAvail.setId(10L);

        when(doctorAvailabilityRepository.findByDoctorId(5L)).thenReturn(Optional.of(docAvail));
        when(doctorAvailabilityRepository.save(any(DoctorAvailability.class))).thenAnswer(i -> i.getArgument(0));

        DoctorStatusUpdateRequest req = new DoctorStatusUpdateRequest();
        req.setStatus(DoctorStatus.EMERGENCY_DELAY);
        req.setReportedDelayMinutes(25);
        req.setDelayReason("Attending cardiac emergency in ICU");

        DoctorAvailability updated = availabilityService.updateDoctorStatus(5L, req, testDoctor);

        assertEquals(DoctorStatus.EMERGENCY_DELAY, updated.getStatus());
        assertEquals(25, updated.getReportedDelayMinutes());
        // Verify department current delay minutes updated
        assertEquals(25, testDept.getCurrentDelayMinutes());
        assertEquals("Attending cardiac emergency in ICU", testDept.getDelayReason());
        verify(departmentRepository).save(testDept);
        verify(queueEngineService).broadcastQueueUpdate(1L);
    }

    @Test
    @DisplayName("Delay Notification: Reporting department delay notifies all waiting patients")
    void testReportDelayNotifiesWaitingPatients() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDept));
        when(departmentRepository.save(any(Department.class))).thenAnswer(i -> i.getArgument(0));

        QueueToken waitingPatient = new QueueToken("OPD-001", 1, testDept, null, "John Doe", "+919876543210", AccessChannel.SMS, PriorityCategory.NORMAL, null);
        when(tokenRepository.findByDepartmentIdAndTokenDateAndStatusOrderBySequenceNumberAsc(eq(1L), any(LocalDate.class), eq(TokenStatus.WAITING)))
                .thenReturn(List.of(waitingPatient));

        DelayReportRequest req = new DelayReportRequest();
        req.setDepartmentId(1L);
        req.setDelayMinutes(15);
        req.setReason("Power backup switchover");
        availabilityService.reportServiceDelay(req, testDoctor);

        assertEquals(15, testDept.getCurrentDelayMinutes());
        assertEquals("Power backup switchover", testDept.getDelayReason());
        verify(notificationService).notify(any(), eq("+919876543210"), anyString(), contains("15 minutes"), eq("QUEUE_DELAY"), eq("SMS"));
        verify(smsProvider).sendSms(eq("+919876543210"), contains("15 minutes"));
    }
}
