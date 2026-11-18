package com.qsmart.service;

import com.qsmart.dto.DelayReportRequest;
import com.qsmart.dto.DoctorStatusUpdateRequest;
import com.qsmart.model.*;
import com.qsmart.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AvailabilityService {

    private final DepartmentRepository departmentRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final ServiceCounterRepository counterRepository;
    private final QueueTokenRepository tokenRepository;
    private final NotificationService notificationService;
    private final SmsProvider smsProvider;
    private final AuditLogService auditLogService;
    private final QueueEngineService queueEngineService;

    @Autowired
    public AvailabilityService(DepartmentRepository departmentRepository,
                               DoctorAvailabilityRepository doctorAvailabilityRepository,
                               ServiceCounterRepository counterRepository,
                               QueueTokenRepository tokenRepository,
                               NotificationService notificationService,
                               SmsProvider smsProvider,
                               AuditLogService auditLogService,
                               QueueEngineService queueEngineService) {
        this.departmentRepository = departmentRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.counterRepository = counterRepository;
        this.tokenRepository = tokenRepository;
        this.notificationService = notificationService;
        this.smsProvider = smsProvider;
        this.auditLogService = auditLogService;
        this.queueEngineService = queueEngineService;
    }

    /**
     * Pause or resume a department's virtual queue
     */
    @Transactional
    public Department toggleQueuePause(Long departmentId, boolean pause, User user) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        dept.setQueuePaused(pause);
        Department saved = departmentRepository.save(dept);

        String action = pause ? "PAUSE_QUEUE" : "RESUME_QUEUE";
        auditLogService.log(action,
                user != null ? user.getName() : "Staff/Admin",
                "Department",
                dept.getId().toString(),
                (pause ? "Paused" : "Resumed") + " queue for " + dept.getName());

        queueEngineService.broadcastQueueUpdate(departmentId);
        return saved;
    }

    /**
     * Report an unexpected service delay in department
     */
    @Transactional
    public Department reportServiceDelay(DelayReportRequest request, User user) {
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + request.getDepartmentId()));

        dept.setCurrentDelayMinutes(request.getDelayMinutes());
        dept.setDelayReason(request.getReason());
        Department saved = departmentRepository.save(dept);

        auditLogService.log("REPORT_DELAY",
                user != null ? user.getName() : "Doctor/Staff",
                "Department",
                dept.getId().toString(),
                "Reported delay: " + request.getDelayMinutes() + " mins. Reason: " + request.getReason());

        // Notify all active waiting patients in this department
        List<QueueToken> waiting = tokenRepository.findByDepartmentIdAndTokenDateAndStatusOrderBySequenceNumberAsc(
                dept.getId(), LocalDate.now(), TokenStatus.WAITING
        );

        String delayNotice = String.format("QSmart Notice: Service in %s is experiencing an estimated delay of ~%d minutes due to '%s'. Your updated waiting time has been recalculated.",
                dept.getName(), request.getDelayMinutes(), request.getReason());

        for (QueueToken token : waiting) {
            notificationService.notify(
                    token.getUser(),
                    token.getPatientPhone(),
                    "Service Delay in " + dept.getName(),
                    delayNotice,
                    "QUEUE_DELAY",
                    token.getAccessChannel() == AccessChannel.SMS ? "SMS" : "IN_APP"
            );
            if (token.getAccessChannel() == AccessChannel.SMS) {
                smsProvider.sendSms(token.getPatientPhone(), delayNotice);
            }
        }

        queueEngineService.broadcastQueueUpdate(dept.getId());
        return saved;
    }

    /**
     * Update doctor availability
     */
    @Transactional
    public DoctorAvailability updateDoctorStatus(Long doctorId, DoctorStatusUpdateRequest request, User user) {
        DoctorAvailability availability = doctorAvailabilityRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor availability record not found for doctor ID: " + doctorId));

        availability.setStatus(request.getStatus());
        availability.setReportedDelayMinutes(request.getReportedDelayMinutes());
        availability.setDelayReason(request.getDelayReason());
        availability.setLastUpdated(LocalDateTime.now());

        DoctorAvailability saved = doctorAvailabilityRepository.save(availability);

        auditLogService.log("DOCTOR_STATUS_UPDATE",
                user != null ? user.getName() : "Doctor",
                "DoctorAvailability",
                saved.getId().toString(),
                "Doctor " + availability.getDoctor().getName() + " set status to " + request.getStatus());

        if (availability.getDepartment() != null) {
            Department dept = availability.getDepartment();
            if (request.getStatus() == DoctorStatus.EMERGENCY_DELAY && request.getReportedDelayMinutes() > 0) {
                dept.setCurrentDelayMinutes(request.getReportedDelayMinutes());
                dept.setDelayReason(request.getDelayReason() != null ? request.getDelayReason() : "Doctor attending emergency case");
                departmentRepository.save(dept);
            } else if (request.getStatus() == DoctorStatus.AVAILABLE) {
                dept.setCurrentDelayMinutes(0);
                dept.setDelayReason(null);
                departmentRepository.save(dept);
            }
            queueEngineService.broadcastQueueUpdate(dept.getId());
        }

        return saved;
    }

    /**
     * Update counter status
     */
    @Transactional
    public ServiceCounter updateCounterStatus(Long counterId, CounterStatus status, User user) {
        ServiceCounter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new IllegalArgumentException("Counter not found with ID: " + counterId));

        counter.setStatus(status);
        ServiceCounter saved = counterRepository.save(counter);

        auditLogService.log("COUNTER_STATUS_UPDATE",
                user != null ? user.getName() : "Staff",
                "ServiceCounter",
                saved.getId().toString(),
                "Counter " + counter.getCounterNumber() + " status set to " + status);

        if (counter.getDepartment() != null) {
            queueEngineService.broadcastQueueUpdate(counter.getDepartment().getId());
        }

        return saved;
    }
}
