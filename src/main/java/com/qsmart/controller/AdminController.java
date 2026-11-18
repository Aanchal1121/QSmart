package com.qsmart.controller;

import com.qsmart.dto.ApiResponse;
import com.qsmart.model.*;
import com.qsmart.repository.*;
import com.qsmart.service.AnalyticsService;
import com.qsmart.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ServiceCenterRepository centerRepository;
    private final DepartmentRepository departmentRepository;
    private final ServiceCounterRepository counterRepository;
    private final PriorityRequestRepository priorityRequestRepository;
    private final AuditLogService auditLogService;
    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @Autowired
    public AdminController(ServiceCenterRepository centerRepository,
                           DepartmentRepository departmentRepository,
                           ServiceCounterRepository counterRepository,
                           PriorityRequestRepository priorityRequestRepository,
                           AuditLogService auditLogService,
                           AnalyticsService analyticsService,
                           UserRepository userRepository) {
        this.centerRepository = centerRepository;
        this.departmentRepository = departmentRepository;
        this.counterRepository = counterRepository;
        this.priorityRequestRepository = priorityRequestRepository;
        this.auditLogService = auditLogService;
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null) return null;
        return userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByPhone(auth.getName()))
                .orElse(null);
    }

    @GetMapping("/hospitals")
    public ResponseEntity<ApiResponse<List<ServiceCenter>>> getAllHospitals() {
        return ResponseEntity.ok(ApiResponse.ok("Hospitals list", centerRepository.findAll()));
    }

    @PostMapping("/hospitals")
    public ResponseEntity<ApiResponse<ServiceCenter>> createHospital(@RequestBody ServiceCenter hospital,
                                                                     Authentication authentication) {
        User admin = getAuthenticatedUser(authentication);
        ServiceCenter saved = centerRepository.save(hospital);
        auditLogService.log("CREATE_HOSPITAL",
                admin != null ? admin.getName() : "Admin",
                "ServiceCenter",
                saved.getId().toString(),
                "Created hospital " + saved.getName() + " (" + saved.getCode() + ")");
        return ResponseEntity.ok(ApiResponse.ok("Hospital created", saved));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.ok("Departments list", departmentRepository.findAll()));
    }

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<Department>> createDepartment(@RequestBody Department department,
                                                                    Authentication authentication) {
        User admin = getAuthenticatedUser(authentication);
        if (department.getServiceCenter() != null && department.getServiceCenter().getId() != null) {
            ServiceCenter sc = centerRepository.findById(department.getServiceCenter().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Hospital not found with ID: " + department.getServiceCenter().getId()));
            department.setServiceCenter(sc);
        }
        Department saved = departmentRepository.save(department);
        auditLogService.log("CREATE_DEPARTMENT",
                admin != null ? admin.getName() : "Admin",
                "Department",
                saved.getId().toString(),
                "Created department " + saved.getName());
        return ResponseEntity.ok(ApiResponse.ok("Department created", saved));
    }

    @GetMapping("/counters")
    public ResponseEntity<ApiResponse<List<ServiceCounter>>> getAllCounters() {
        return ResponseEntity.ok(ApiResponse.ok("Counters list", counterRepository.findAll()));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.ok("Audit logs retrieved", auditLogService.getRecentLogs()));
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.ok("System analytics", analyticsService.getOverallAnalytics()));
    }

    @GetMapping("/priority-requests")
    public ResponseEntity<ApiResponse<List<PriorityRequest>>> getPriorityRequests() {
        return ResponseEntity.ok(ApiResponse.ok("Priority requests list", priorityRequestRepository.findTop50ByOrderByCreatedAtDesc()));
    }
}
