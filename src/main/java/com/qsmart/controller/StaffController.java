package com.qsmart.controller;

import com.qsmart.dto.*;
import com.qsmart.model.AccessChannel;
import com.qsmart.model.Department;
import com.qsmart.model.QueueToken;
import com.qsmart.model.ServiceCounter;
import com.qsmart.model.User;
import com.qsmart.repository.DepartmentRepository;
import com.qsmart.repository.ServiceCounterRepository;
import com.qsmart.repository.UserRepository;
import com.qsmart.service.AvailabilityService;
import com.qsmart.service.QueueEngineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final QueueEngineService queueEngineService;
    private final AvailabilityService availabilityService;
    private final UserRepository userRepository;
    private final ServiceCounterRepository counterRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    public StaffController(QueueEngineService queueEngineService,
                           AvailabilityService availabilityService,
                           UserRepository userRepository,
                           ServiceCounterRepository counterRepository,
                           DepartmentRepository departmentRepository) {
        this.queueEngineService = queueEngineService;
        this.availabilityService = availabilityService;
        this.userRepository = userRepository;
        this.counterRepository = counterRepository;
        this.departmentRepository = departmentRepository;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null) return null;
        return userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByPhone(auth.getName()))
                .orElse(null);
    }

    @PostMapping("/walk-in/register")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> registerWalkIn(@Valid @RequestBody TokenBookingRequest request,
                                                                           Authentication authentication) {
        User staff = getAuthenticatedUser(authentication);
        try {
            request.setAccessChannel(AccessChannel.WALK_IN);
            QueueToken token = queueEngineService.bookToken(request, staff);
            TokenStatusResponse response = queueEngineService.mapToResponse(token);
            return ResponseEntity.ok(ApiResponse.ok("Walk-in patient registered. Token slip generated.", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/queue/{departmentId}")
    public ResponseEntity<ApiResponse<QueueLiveStatusDto>> getDepartmentQueue(@PathVariable Long departmentId) {
        try {
            QueueLiveStatusDto liveStatus = queueEngineService.getLiveQueueStatus(departmentId);
            return ResponseEntity.ok(ApiResponse.ok("Department queue retrieved", liveStatus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/queue/call-next")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> callNext(@RequestParam Long departmentId,
                                                                     @RequestParam(required = false) Long counterId,
                                                                     @RequestParam(required = false) Long specificTokenId,
                                                                     Authentication authentication) {
        User staff = getAuthenticatedUser(authentication);
        try {
            QueueToken token = queueEngineService.callNext(departmentId, counterId, staff, specificTokenId);
            return ResponseEntity.ok(ApiResponse.ok("Next patient called successfully", queueEngineService.mapToResponse(token)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/queue/mark-served")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> markServed(@RequestParam Long tokenId,
                                                                       Authentication authentication) {
        User staff = getAuthenticatedUser(authentication);
        try {
            QueueToken token = queueEngineService.markServed(tokenId, staff);
            return ResponseEntity.ok(ApiResponse.ok("Patient marked as served", queueEngineService.mapToResponse(token)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/queue/skip")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> skipToken(@Valid @RequestBody SkipTokenRequest request,
                                                                      Authentication authentication) {
        User staff = getAuthenticatedUser(authentication);
        try {
            QueueToken token = queueEngineService.skipToken(request.getTokenId(), request.getReason(), staff);
            return ResponseEntity.ok(ApiResponse.ok("Token skipped successfully", queueEngineService.mapToResponse(token)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/queue/report-delay")
    public ResponseEntity<ApiResponse<Department>> reportDelay(@Valid @RequestBody DelayReportRequest request,
                                                               Authentication authentication) {
        User staff = getAuthenticatedUser(authentication);
        try {
            Department dept = availabilityService.reportServiceDelay(request, staff);
            return ResponseEntity.ok(ApiResponse.ok("Service delay reported and patients alerted", dept));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/queue/toggle-pause")
    public ResponseEntity<ApiResponse<Department>> togglePause(@RequestParam Long departmentId,
                                                               @RequestParam boolean pause,
                                                               Authentication authentication) {
        User staff = getAuthenticatedUser(authentication);
        try {
            Department dept = availabilityService.toggleQueuePause(departmentId, pause, staff);
            return ResponseEntity.ok(ApiResponse.ok("Queue pause status toggled", dept));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/counters")
    public ResponseEntity<ApiResponse<List<ServiceCounter>>> getCounters(@RequestParam(required = false) Long departmentId) {
        List<ServiceCounter> counters;
        if (departmentId != null) {
            counters = counterRepository.findByDepartmentId(departmentId);
        } else {
            counters = counterRepository.findAll();
        }
        return ResponseEntity.ok(ApiResponse.ok("Counters retrieved", counters));
    }
}
