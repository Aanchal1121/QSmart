package com.qsmart.service;

import com.qsmart.dto.SmsInboundRequest;
import com.qsmart.dto.SmsInboundResponse;
import com.qsmart.dto.TokenBookingRequest;
import com.qsmart.model.*;
import com.qsmart.repository.DepartmentRepository;
import com.qsmart.repository.QueueTokenRepository;
import com.qsmart.repository.SmsMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final QueueEngineService queueEngineService;
    private final DepartmentRepository departmentRepository;
    private final QueueTokenRepository tokenRepository;
    private final SmsMessageRepository smsMessageRepository;
    private final WaitingTimeService waitingTimeService;
    private final SmsProvider smsProvider;

    @Autowired
    public SmsService(QueueEngineService queueEngineService,
                      DepartmentRepository departmentRepository,
                      QueueTokenRepository tokenRepository,
                      SmsMessageRepository smsMessageRepository,
                      WaitingTimeService waitingTimeService,
                      SmsProvider smsProvider) {
        this.queueEngineService = queueEngineService;
        this.departmentRepository = departmentRepository;
        this.tokenRepository = tokenRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.waitingTimeService = waitingTimeService;
        this.smsProvider = smsProvider;
    }

    /**
     * Process incoming SMS command from keypad phone users
     */
    @Transactional
    public SmsInboundResponse processInboundSms(SmsInboundRequest request) {
        String rawPhone = request.getFrom() != null ? request.getFrom().replaceAll("[^0-9+]", "") : "";
        String text = request.getText() != null ? request.getText().trim() : "";

        log.info("Processing Inbound SMS from: {} | Message: '{}'", rawPhone, text);

        if (rawPhone.isEmpty() || text.isEmpty()) {
            return new SmsInboundResponse(false, "Invalid SMS payload. Sender and text are required.", null, "ERROR");
        }

        String[] parts = text.split("\\s+");
        String command = parts[0].toUpperCase();
        String reply;
        String generatedToken = null;
        boolean success = true;

        try {
            switch (command) {
                case "BOOK":
                    if (parts.length < 3) {
                        reply = "Error: Use syntax: BOOK <HOSPITAL_CODE> <DEPT_CODE> (e.g. BOOK HOSPITAL01 OPD). Text HELP for codes.";
                        success = false;
                    } else {
                        String hospitalCode = parts[1].trim();
                        String deptCode = parts[2].trim();

                        Optional<Department> deptOpt = departmentRepository
                                .findByServiceCenterCodeIgnoreCaseAndCodeIgnoreCase(hospitalCode, deptCode);

                        if (deptOpt.isEmpty()) {
                            // Try finding by department code directly if hospital code matches
                            deptOpt = departmentRepository.findByCodeIgnoreCase(deptCode);
                        }

                        if (deptOpt.isEmpty()) {
                            reply = "Hospital or Department code not found. Available: HOSPITAL01 (OPD, CARD, ORTHO, PED). Text HELP for info.";
                            success = false;
                        } else {
                            Department dept = deptOpt.get();
                            TokenBookingRequest bookingReq = new TokenBookingRequest();
                            bookingReq.setDepartmentId(dept.getId());
                            bookingReq.setPatientPhone(rawPhone);
                            bookingReq.setPatientName("SMS User (" + rawPhone.substring(Math.max(0, rawPhone.length() - 4)) + ")");
                            bookingReq.setAccessChannel(AccessChannel.SMS);
                            bookingReq.setPriorityCategory(PriorityCategory.NORMAL);

                            QueueToken token = queueEngineService.bookToken(bookingReq, null);
                            generatedToken = token.getTokenNumber();

                            int waitEst = waitingTimeService.calculateWaitMinutes(token);
                            long ahead = tokenRepository.countWaitingAhead(dept.getId(), LocalDate.now(), token.getSequenceNumber());

                            reply = String.format("QSmart: Token %s confirmed for %s (%s). Ahead: %d. Est wait: ~%d min. Text STATUS %s for updates.",
                                    token.getTokenNumber(), dept.getName(), dept.getServiceCenter().getName(), ahead, waitEst, token.getTokenNumber());
                        }
                    }
                    break;

                case "STATUS":
                    if (parts.length < 2) {
                        reply = "Error: Use syntax: STATUS <TOKEN_NUMBER> (e.g. STATUS OPD-001 or STATUS H-025).";
                        success = false;
                    } else {
                        String tokenNumber = parts[1].trim();
                        Optional<QueueToken> tokenOpt = tokenRepository.findByTokenNumberIgnoreCase(tokenNumber);

                        if (tokenOpt.isEmpty()) {
                            reply = "Token " + tokenNumber + " not found. Please verify your token number.";
                            success = false;
                        } else {
                            QueueToken token = tokenOpt.get();
                            generatedToken = token.getTokenNumber();
                            if (token.getStatus() == TokenStatus.WAITING) {
                                int waitEst = waitingTimeService.calculateWaitMinutes(token);
                                long ahead = tokenRepository.countWaitingAhead(token.getDepartment().getId(), token.getTokenDate(), token.getSequenceNumber());
                                reply = String.format("Token %s: WAITING. People ahead: %d. Est wait: ~%d min. Dept: %s.",
                                        token.getTokenNumber(), ahead, waitEst, token.getDepartment().getName());
                            } else if (token.getStatus() == TokenStatus.CALLED) {
                                String counterName = token.getAssignedCounter() != null ? token.getAssignedCounter().getCounterNumber() : "Room";
                                reply = String.format("ALERT: Token %s is now CALLED at %s (%s)! Please proceed immediately.",
                                        token.getTokenNumber(), counterName, token.getDepartment().getName());
                            } else {
                                reply = String.format("Token %s status: %s (%s).",
                                        token.getTokenNumber(), token.getStatus(), token.getDepartment().getName());
                            }
                        }
                    }
                    break;

                case "CANCEL":
                    if (parts.length < 2) {
                        reply = "Error: Use syntax: CANCEL <TOKEN_NUMBER> (e.g. CANCEL OPD-001).";
                        success = false;
                    } else {
                        String tokenNumber = parts[1].trim();
                        Optional<QueueToken> tokenOpt = tokenRepository.findByTokenNumberIgnoreCase(tokenNumber);

                        if (tokenOpt.isEmpty()) {
                            reply = "Token " + tokenNumber + " not found to cancel.";
                            success = false;
                        } else {
                            QueueToken token = tokenOpt.get();
                            queueEngineService.cancelToken(token.getId(), null, "Cancelled via SMS command");
                            generatedToken = token.getTokenNumber();
                            reply = "QSmart: Token " + token.getTokenNumber() + " has been cancelled successfully.";
                        }
                    }
                    break;

                case "HELP":
                    reply = "QSmart SMS Commands:\n- BOOK <HOSPITAL> <DEPT> (e.g. BOOK HOSPITAL01 OPD)\n- STATUS <TOKEN> (e.g. STATUS OPD-001)\n- CANCEL <TOKEN>\n- HELP";
                    break;

                default:
                    reply = "Unknown command '" + command + "'. Valid commands: BOOK <HOSPITAL> <DEPT>, STATUS <TOKEN>, CANCEL <TOKEN>, HELP.";
                    success = false;
                    break;
            }
        } catch (Exception e) {
            log.error("Error executing SMS command: {}", e.getMessage());
            reply = "QSmart Error: " + e.getMessage();
            success = false;
        }

        // Record inbound SMS and reply in database
        SmsMessage smsRecord = new SmsMessage(rawPhone, "INBOUND", text, command, success ? "PROCESSED" : "FAILED", reply);
        smsMessageRepository.save(smsRecord);

        // Send outbound response through SmsProvider
        smsProvider.sendSms(rawPhone, reply);

        return new SmsInboundResponse(success, reply, generatedToken, command);
    }
}
