package com.qsmart.controller;

import com.qsmart.dto.ApiResponse;
import com.qsmart.dto.QueueLiveStatusDto;
import com.qsmart.dto.TokenStatusResponse;
import com.qsmart.model.Department;
import com.qsmart.model.QueueToken;
import com.qsmart.model.ServiceCenter;
import com.qsmart.repository.DepartmentRepository;
import com.qsmart.repository.QueueTokenRepository;
import com.qsmart.repository.ServiceCenterRepository;
import com.qsmart.service.QueueEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicApiController {

    private final ServiceCenterRepository centerRepository;
    private final DepartmentRepository departmentRepository;
    private final QueueTokenRepository tokenRepository;
    private final QueueEngineService queueEngineService;

    @Autowired
    public PublicApiController(ServiceCenterRepository centerRepository,
                               DepartmentRepository departmentRepository,
                               QueueTokenRepository tokenRepository,
                               QueueEngineService queueEngineService) {
        this.centerRepository = centerRepository;
        this.departmentRepository = departmentRepository;
        this.tokenRepository = tokenRepository;
        this.queueEngineService = queueEngineService;
    }

    @GetMapping("/hospitals")
    public ResponseEntity<ApiResponse<List<ServiceCenter>>> getHospitals() {
        return ResponseEntity.ok(ApiResponse.ok("Hospitals list", centerRepository.findByActiveTrue()));
    }

    @GetMapping("/hospitals/{id}/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Departments list", departmentRepository.findByServiceCenterIdAndActiveTrue(id)));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.ok("All active departments", departmentRepository.findByActiveTrue()));
    }

    @GetMapping("/queue/{departmentId}")
    public ResponseEntity<ApiResponse<QueueLiveStatusDto>> getLiveQueue(@PathVariable Long departmentId) {
        try {
            QueueLiveStatusDto liveStatus = queueEngineService.getLiveQueueStatus(departmentId);
            return ResponseEntity.ok(ApiResponse.ok("Live queue status", liveStatus));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/verify-token/{qrHash}")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> verifyToken(@PathVariable String qrHash) {
        return tokenRepository.findByVerificationQrHash(qrHash)
                .map(token -> ResponseEntity.ok(ApiResponse.ok("Token verified successfully", queueEngineService.mapToResponse(token))))
                .orElse(ResponseEntity.badRequest().body(ApiResponse.error("Invalid or expired QR verification code")));
    }
}
