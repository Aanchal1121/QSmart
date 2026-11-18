package com.qsmart.service;

import com.qsmart.dto.QueueLiveStatusDto;
import com.qsmart.dto.TokenBookingRequest;
import com.qsmart.dto.TokenStatusResponse;
import com.qsmart.model.*;
import com.qsmart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QueueEngineService {

    private static final Logger log = LoggerFactory.getLogger(QueueEngineService.class);

    private final QueueTokenRepository tokenRepository;
    private final DepartmentRepository departmentRepository;
    private final ServiceCounterRepository counterRepository;
    private final TokenHistoryRepository historyRepository;
    private final WaitingTimeService waitingTimeService;
    private final NotificationService notificationService;
    private final SmsProvider smsProvider;
    private final AuditLogService auditLogService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public QueueEngineService(QueueTokenRepository tokenRepository,
                              DepartmentRepository departmentRepository,
                              ServiceCounterRepository counterRepository,
                              TokenHistoryRepository historyRepository,
                              WaitingTimeService waitingTimeService,
                              NotificationService notificationService,
                              SmsProvider smsProvider,
                              AuditLogService auditLogService,
                              SimpMessagingTemplate messagingTemplate) {
        this.tokenRepository = tokenRepository;
        this.departmentRepository = departmentRepository;
        this.counterRepository = counterRepository;
        this.historyRepository = historyRepository;
        this.waitingTimeService = waitingTimeService;
        this.notificationService = notificationService;
        this.smsProvider = smsProvider;
        this.auditLogService = auditLogService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Centralized Token Booking for Smartphone, SMS, and Walk-in channels.
     */
    @Transactional
    public synchronized QueueToken bookToken(TokenBookingRequest request, User currentUser) {
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + request.getDepartmentId()));

        if (!department.isActive()) {
            throw new IllegalStateException("Department is currently inactive");
        }
        if (department.isQueuePaused()) {
            throw new IllegalStateException("Queue is temporarily paused for " + department.getName());
        }

        String patientPhone = request.getPatientPhone();
        if (patientPhone == null || patientPhone.trim().isEmpty()) {
            if (currentUser != null && currentUser.getPhone() != null) {
                patientPhone = currentUser.getPhone();
            } else {
                throw new IllegalArgumentException("Patient phone number is required");
            }
        }
        patientPhone = patientPhone.replaceAll("[^0-9+]", "");

        String patientName = request.getPatientName();
        if (patientName == null || patientName.trim().isEmpty()) {
            patientName = (currentUser != null) ? currentUser.getName() : "Patient (" + patientPhone + ")";
        }

        LocalDate today = LocalDate.now();

        // Duplicate active booking prevention for the same department today
        List<TokenStatus> activeStatuses = Arrays.asList(TokenStatus.WAITING, TokenStatus.CALLED);
        List<QueueToken> existingActive = tokenRepository.findActiveTokensByPhoneAndDepartment(
                patientPhone, department.getId(), today, activeStatuses
        );

        if (!existingActive.isEmpty()) {
            QueueToken existing = existingActive.get(0);
            throw new IllegalStateException("You already have an active token (" + existing.getTokenNumber() +
                    ") in " + department.getName() + " today. Current Status: " + existing.getStatus());
        }

        // Atomic sequence number calculation for the department today
        int nextSequence = tokenRepository.findMaxSequenceNumber(department.getId(), today) + 1;
        String tokenNumber = String.format("%s-%03d", department.getCode().toUpperCase(), nextSequence);

        AccessChannel channel = request.getAccessChannel() != null ? request.getAccessChannel() : AccessChannel.SMARTPHONE;
        PriorityCategory priority = request.getPriorityCategory() != null ? request.getPriorityCategory() : PriorityCategory.NORMAL;

        QueueToken token = new QueueToken(
                tokenNumber,
                nextSequence,
                department,
                currentUser,
                patientName,
                patientPhone,
                channel,
                priority,
                request.getPriorityReason()
        );
        token.setNotes(request.getNotes());

        QueueToken savedToken = tokenRepository.save(token);

        // Record history
        historyRepository.save(new TokenHistory(
                savedToken,
                null,
                TokenStatus.WAITING,
                currentUser,
                "Token created via " + channel
        ));

        // Audit log
        auditLogService.log("BOOK_TOKEN",
                currentUser != null ? currentUser.getName() : "SMS/Walk-in Guest",
                "QueueToken",
                savedToken.getId().toString(),
                "Booked token " + tokenNumber + " for " + department.getName() + " via " + channel);

        // Notifications
        int waitEst = waitingTimeService.calculateWaitMinutes(savedToken);
        long peopleAhead = tokenRepository.countWaitingAhead(department.getId(), today, savedToken.getSequenceNumber());

        String confirmationMsg = String.format("QSmart: Token %s confirmed for %s (%s). People ahead: %d. Est. wait: ~%d mins.",
                tokenNumber, department.getName(), department.getServiceCenter().getName(), peopleAhead, waitEst);

        notificationService.notify(
                currentUser,
                patientPhone,
                "Token Booked: " + tokenNumber,
                confirmationMsg,
                "TOKEN_BOOKED",
                channel == AccessChannel.SMS ? "SMS" : "IN_APP"
        );

        if (channel == AccessChannel.SMS) {
            smsProvider.sendSms(patientPhone, confirmationMsg);
        }

        // Broadcast WebSocket update
        broadcastQueueUpdate(department.getId());

        log.info("Centralized Token created: {} for {} via channel {}", tokenNumber, patientName, channel);
        return savedToken;
    }

    /**
     * Call next token in queue for a counter
     */
    @Transactional
    public synchronized QueueToken callNext(Long departmentId, Long counterId, User staffOrDoctor, Long specificTokenId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        ServiceCounter counter = null;
        if (counterId != null) {
            counter = counterRepository.findById(counterId)
                    .orElseThrow(() -> new IllegalArgumentException("Counter not found with ID: " + counterId));
        }

        LocalDate today = LocalDate.now();
        QueueToken tokenToCall = null;

        if (specificTokenId != null) {
            tokenToCall = tokenRepository.findById(specificTokenId)
                    .orElseThrow(() -> new IllegalArgumentException("Token not found with ID: " + specificTokenId));
            if (tokenToCall.getStatus() != TokenStatus.WAITING) {
                throw new IllegalStateException("Token " + tokenToCall.getTokenNumber() + " is not in WAITING state.");
            }
        } else {
            // FIFO Order: lowest sequence number in WAITING state
            List<QueueToken> waitingTokens = tokenRepository.findByDepartmentIdAndTokenDateAndStatusOrderBySequenceNumberAsc(
                    departmentId, today, TokenStatus.WAITING
            );

            if (waitingTokens.isEmpty()) {
                throw new IllegalStateException("No waiting patients in queue for " + department.getName());
            }

            // Priority check: If any senior/pwd/emergency token is waiting and staff approved
            Optional<QueueToken> priorityToken = waitingTokens.stream()
                    .filter(t -> t.getPriorityCategory() != PriorityCategory.NORMAL)
                    .findFirst();

            tokenToCall = priorityToken.orElse(waitingTokens.get(0));
        }

        TokenStatus prevStatus = tokenToCall.getStatus();
        tokenToCall.setStatus(TokenStatus.CALLED);
        tokenToCall.setCalledAt(LocalDateTime.now());
        tokenToCall.setAssignedCounter(counter);
        tokenToCall.setServedBy(staffOrDoctor);

        QueueToken updatedToken = tokenRepository.save(tokenToCall);

        if (counter != null) {
            counter.setStatus(CounterStatus.BUSY);
            counter.setCurrentTokenId(updatedToken.getId());
            counterRepository.save(counter);
        }

        historyRepository.save(new TokenHistory(
                updatedToken,
                prevStatus,
                TokenStatus.CALLED,
                staffOrDoctor,
                "Called to " + (counter != null ? counter.getCounterNumber() : "Counter")
        ));

        auditLogService.log("CALL_NEXT",
                staffOrDoctor != null ? staffOrDoctor.getName() : "Staff",
                "QueueToken",
                updatedToken.getId().toString(),
                "Called token " + updatedToken.getTokenNumber() + " to " + (counter != null ? counter.getCounterNumber() : "Counter"));

        // Notify patient
        String counterInfo = (counter != null) ? counter.getCounterNumber() : "the designated room";
        String callMsg = String.format("ALERT: Token %s is now called at %s for %s! Please proceed immediately.",
                updatedToken.getTokenNumber(), counterInfo, department.getName());

        notificationService.notify(
                updatedToken.getUser(),
                updatedToken.getPatientPhone(),
                "Turn Arrived: " + updatedToken.getTokenNumber(),
                callMsg,
                "TOKEN_CALLED",
                updatedToken.getAccessChannel() == AccessChannel.SMS ? "SMS" : "IN_APP"
        );

        smsProvider.sendSms(updatedToken.getPatientPhone(), callMsg);

        broadcastQueueUpdate(departmentId);
        return updatedToken;
    }

    /**
     * Mark patient as served
     */
    @Transactional
    public QueueToken markServed(Long tokenId, User staffOrDoctor) {
        QueueToken token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found with ID: " + tokenId));

        TokenStatus prevStatus = token.getStatus();
        token.setStatus(TokenStatus.SERVED);
        token.setServedAt(LocalDateTime.now());
        token.setServedBy(staffOrDoctor);

        QueueToken savedToken = tokenRepository.save(token);

        // Free up assigned counter
        if (token.getAssignedCounter() != null) {
            ServiceCounter counter = token.getAssignedCounter();
            if (counter.getCurrentTokenId() != null && counter.getCurrentTokenId().equals(tokenId)) {
                counter.setStatus(CounterStatus.AVAILABLE);
                counter.setCurrentTokenId(null);
                counterRepository.save(counter);
            }
        }

        historyRepository.save(new TokenHistory(
                savedToken,
                prevStatus,
                TokenStatus.SERVED,
                staffOrDoctor,
                "Service completed"
        ));

        auditLogService.log("MARK_SERVED",
                staffOrDoctor != null ? staffOrDoctor.getName() : "Staff",
                "QueueToken",
                savedToken.getId().toString(),
                "Completed service for " + savedToken.getTokenNumber());

        broadcastQueueUpdate(token.getDepartment().getId());
        return savedToken;
    }

    /**
     * Skip absent patient with a mandatory reason
     */
    @Transactional
    public QueueToken skipToken(Long tokenId, String reason, User staffOrDoctor) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A valid reason is required to skip a token");
        }

        QueueToken token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found with ID: " + tokenId));

        TokenStatus prevStatus = token.getStatus();
        token.setStatus(TokenStatus.SKIPPED);
        token.setSkippedAt(LocalDateTime.now());
        token.setSkipReason(reason);

        QueueToken savedToken = tokenRepository.save(token);

        // Free counter
        if (token.getAssignedCounter() != null) {
            ServiceCounter counter = token.getAssignedCounter();
            if (counter.getCurrentTokenId() != null && counter.getCurrentTokenId().equals(tokenId)) {
                counter.setStatus(CounterStatus.AVAILABLE);
                counter.setCurrentTokenId(null);
                counterRepository.save(counter);
            }
        }

        historyRepository.save(new TokenHistory(
                savedToken,
                prevStatus,
                TokenStatus.SKIPPED,
                staffOrDoctor,
                "Skipped: " + reason
        ));

        auditLogService.log("SKIP_TOKEN",
                staffOrDoctor != null ? staffOrDoctor.getName() : "Staff",
                "QueueToken",
                savedToken.getId().toString(),
                "Skipped token " + savedToken.getTokenNumber() + ". Reason: " + reason);

        // Notify patient
        String skipMsg = String.format("QSmart Notice: Token %s was skipped because: '%s'. Please visit reception if you are present.",
                savedToken.getTokenNumber(), reason);

        notificationService.notify(
                savedToken.getUser(),
                savedToken.getPatientPhone(),
                "Token Skipped: " + savedToken.getTokenNumber(),
                skipMsg,
                "TOKEN_SKIPPED",
                savedToken.getAccessChannel() == AccessChannel.SMS ? "SMS" : "IN_APP"
        );

        smsProvider.sendSms(savedToken.getPatientPhone(), skipMsg);

        broadcastQueueUpdate(token.getDepartment().getId());
        return savedToken;
    }

    /**
     * Cancel token
     */
    @Transactional
    public QueueToken cancelToken(Long tokenId, User requester, String reason) {
        QueueToken token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found with ID: " + tokenId));

        if (token.getStatus() == TokenStatus.SERVED || token.getStatus() == TokenStatus.CANCELLED) {
            throw new IllegalStateException("Token " + token.getTokenNumber() + " cannot be cancelled (status: " + token.getStatus() + ")");
        }

        TokenStatus prevStatus = token.getStatus();
        token.setStatus(TokenStatus.CANCELLED);
        token.setCancelledAt(LocalDateTime.now());
        token.setSkipReason(reason != null ? reason : "Cancelled by user");

        QueueToken savedToken = tokenRepository.save(token);

        if (token.getAssignedCounter() != null) {
            ServiceCounter counter = token.getAssignedCounter();
            if (counter.getCurrentTokenId() != null && counter.getCurrentTokenId().equals(tokenId)) {
                counter.setStatus(CounterStatus.AVAILABLE);
                counter.setCurrentTokenId(null);
                counterRepository.save(counter);
            }
        }

        historyRepository.save(new TokenHistory(
                savedToken,
                prevStatus,
                TokenStatus.CANCELLED,
                requester,
                reason != null ? reason : "Cancelled by user"
        ));

        auditLogService.log("CANCEL_TOKEN",
                requester != null ? requester.getName() : "User/SMS",
                "QueueToken",
                savedToken.getId().toString(),
                "Cancelled token " + savedToken.getTokenNumber());

        broadcastQueueUpdate(token.getDepartment().getId());
        return savedToken;
    }

    /**
     * Get live queue status for a department
     */
    public QueueLiveStatusDto getLiveQueueStatus(Long departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + departmentId));

        LocalDate today = LocalDate.now();

        List<QueueToken> waiting = tokenRepository.findByDepartmentIdAndTokenDateAndStatusOrderBySequenceNumberAsc(
                departmentId, today, TokenStatus.WAITING
        );
        List<QueueToken> called = tokenRepository.findByDepartmentIdAndTokenDateAndStatusOrderBySequenceNumberAsc(
                departmentId, today, TokenStatus.CALLED
        );

        QueueLiveStatusDto dto = new QueueLiveStatusDto();
        dto.setDepartmentId(department.getId());
        dto.setDepartmentName(department.getName());
        dto.setDepartmentCode(department.getCode());
        dto.setHospitalName(department.getServiceCenter().getName());
        dto.setHospitalCode(department.getServiceCenter().getCode());
        dto.setQueuePaused(department.isQueuePaused());
        dto.setDelayMinutes(department.getCurrentDelayMinutes());
        dto.setDelayReason(department.getDelayReason());

        if (!called.isEmpty()) {
            QueueToken latestCalled = called.get(called.size() - 1);
            dto.setCurrentServingToken(latestCalled.getTokenNumber());
            dto.setCurrentServingPatient(latestCalled.getPatientName());
            if (latestCalled.getAssignedCounter() != null) {
                dto.setCurrentCounterNumber(latestCalled.getAssignedCounter().getCounterNumber());
            }
        } else {
            dto.setCurrentServingToken("None");
            dto.setCurrentServingPatient("-");
            dto.setCurrentCounterNumber("-");
        }

        dto.setWaitingTokens(waiting.stream().map(this::mapToResponse).collect(Collectors.toList()));
        dto.setRecentlyCalledTokens(called.stream().map(this::mapToResponse).collect(Collectors.toList()));

        dto.setTotalWaiting(waiting.size());
        dto.setTotalServedToday((int) tokenRepository.countByDepartmentIdAndTokenDateAndStatus(departmentId, today, TokenStatus.SERVED));
        dto.setTotalCancelledToday((int) tokenRepository.countByDepartmentIdAndTokenDateAndStatus(departmentId, today, TokenStatus.CANCELLED));
        dto.setTotalSkippedToday((int) tokenRepository.countByDepartmentIdAndTokenDateAndStatus(departmentId, today, TokenStatus.SKIPPED));

        int activeCounters = waitingTimeService.getActiveCounterCount(departmentId);
        dto.setActiveCountersCount(activeCounters);
        dto.setActiveDoctorsCount(activeCounters);

        // Average wait for first in line
        if (!waiting.isEmpty()) {
            dto.setAvgWaitMinutes(waitingTimeService.calculateWaitMinutes(waiting.get(0)));
        } else {
            dto.setAvgWaitMinutes(0);
        }

        return dto;
    }

    /**
     * Map QueueToken entity to TokenStatusResponse with live ETA
     */
    public TokenStatusResponse mapToResponse(QueueToken token) {
        TokenStatusResponse resp = new TokenStatusResponse();
        resp.setId(token.getId());
        resp.setTokenNumber(token.getTokenNumber());
        resp.setSequenceNumber(token.getSequenceNumber());
        resp.setDepartmentId(token.getDepartment().getId());
        resp.setDepartmentName(token.getDepartment().getName());
        resp.setHospitalName(token.getDepartment().getServiceCenter().getName());
        resp.setPatientName(token.getPatientName());
        resp.setPatientPhone(token.getPatientPhone());
        resp.setAccessChannel(token.getAccessChannel());
        resp.setStatus(token.getStatus());
        resp.setPriorityCategory(token.getPriorityCategory());
        resp.setPriorityReason(token.getPriorityReason());
        resp.setCreatedAt(token.getCreatedAt());
        resp.setCalledAt(token.getCalledAt());
        resp.setVerificationQrHash(token.getVerificationQrHash());
        resp.setSkipReason(token.getSkipReason());

        if (token.getAssignedCounter() != null) {
            resp.setAssignedCounterName(token.getAssignedCounter().getCounterNumber());
        }
        if (token.getServedBy() != null) {
            resp.setServedByName(token.getServedBy().getName());
        }

        LocalDate today = token.getTokenDate() != null ? token.getTokenDate() : LocalDate.now();
        long peopleAhead = tokenRepository.countWaitingAhead(token.getDepartment().getId(), today, token.getSequenceNumber());
        resp.setPeopleAhead((int) peopleAhead);

        int waitMinutes = waitingTimeService.calculateWaitMinutes(token);
        resp.setEstimatedWaitingMinutes(waitMinutes);
        resp.setEstimatedCallTime(LocalDateTime.now().plusMinutes(waitMinutes));

        return resp;
    }

    /**
     * Broadcast live queue status update over WebSocket
     */
    public void broadcastQueueUpdate(Long departmentId) {
        try {
            QueueLiveStatusDto liveStatus = getLiveQueueStatus(departmentId);
            messagingTemplate.convertAndSend("/topic/queue/" + departmentId, liveStatus);
            messagingTemplate.convertAndSend("/topic/public-display", liveStatus);
        } catch (Exception e) {
            log.warn("Could not broadcast WebSocket update for department {}: {}", departmentId, e.getMessage());
        }
    }
}
