package com.qsmart.controller;

import com.qsmart.dto.*;
import com.qsmart.model.Department;
import com.qsmart.model.DoctorAvailability;
import com.qsmart.model.QueueToken;
import com.qsmart.model.User;
import com.qsmart.repository.DoctorAvailabilityRepository;
import com.qsmart.repository.UserRepository;
import com.qsmart.service.AvailabilityService;
import com.qsmart.service.QueueEngineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final QueueEngineService queueEngineService;
    private final AvailabilityService availabilityService;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final UserRepository userRepository;

    @Autowired
    public DoctorController(QueueEngineService queueEngineService,
                            AvailabilityService availabilityService,
                            DoctorAvailabilityRepository doctorAvailabilityRepository,
                            UserRepository userRepository) {
        this.queueEngineService = queueEngineService;
        this.availabilityService = availabilityService;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null) return null;
        return userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByPhone(auth.getName()))
                .orElse(null);
    }

    @GetMapping("/my-status")
    public ResponseEntity<ApiResponse<DoctorAvailability>> getMyStatus(Authentication authentication) {
        User doctor = getAuthenticatedUser(authentication);
        if (doctor == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }

        DoctorAvailability record = doctorAvailabilityRepository.findByDoctorId(doctor.getId())
                .orElse(null);

        return ResponseEntity.ok(ApiResponse.ok("Doctor availability retrieved", record));
    }

    @PostMapping("/status")
    public ResponseEntity<ApiResponse<DoctorAvailability>> updateStatus(@Valid @RequestBody DoctorStatusUpdateRequest request,
                                                                        Authentication authentication) {
        User doctor = getAuthenticatedUser(authentication);
        if (doctor == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Doctor profile not found"));
        }

        try {
            DoctorAvailability updated = availabilityService.updateDoctorStatus(doctor.getId(), request, doctor);
            return ResponseEntity.ok(ApiResponse.ok("Status updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<QueueLiveStatusDto>> getDoctorQueue(@RequestParam(required = false) Long departmentId,
                                                                         Authentication authentication) {
        User doctor = getAuthenticatedUser(authentication);
        if (departmentId == null && doctor != null) {
            DoctorAvailability da = doctorAvailabilityRepository.findByDoctorId(doctor.getId()).orElse(null);
            if (da != null && da.getDepartment() != null) {
                departmentId = da.getDepartment().getId();
            }
        }

        if (departmentId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Department ID could not be determined"));
        }

        try {
            QueueLiveStatusDto status = queueEngineService.getLiveQueueStatus(departmentId);
            return ResponseEntity.ok(ApiResponse.ok("Doctor queue retrieved", status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/call-next")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> callNext(@RequestParam Long departmentId,
                                                                     @RequestParam(required = false) Long counterId,
                                                                     @RequestParam(required = false) Long specificTokenId,
                                                                     Authentication authentication) {
        User doctor = getAuthenticatedUser(authentication);
        try {
            QueueToken token = queueEngineService.callNext(departmentId, counterId, doctor, specificTokenId);
            return ResponseEntity.ok(ApiResponse.ok("Patient called to consultation room", queueEngineService.mapToResponse(token)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/mark-served")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> markServed(@RequestParam Long tokenId,
                                                                       Authentication authentication) {
        User doctor = getAuthenticatedUser(authentication);
        try {
            QueueToken token = queueEngineService.markServed(tokenId, doctor);
            return ResponseEntity.ok(ApiResponse.ok("Consultation marked completed", queueEngineService.mapToResponse(token)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
