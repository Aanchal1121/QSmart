package com.qsmart.controller;

import com.qsmart.dto.ApiResponse;
import com.qsmart.dto.TokenBookingRequest;
import com.qsmart.dto.TokenStatusResponse;
import com.qsmart.model.Notification;
import com.qsmart.model.QueueToken;
import com.qsmart.model.TokenStatus;
import com.qsmart.model.User;
import com.qsmart.repository.QueueTokenRepository;
import com.qsmart.repository.UserRepository;
import com.qsmart.service.NotificationService;
import com.qsmart.service.QueueEngineService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final QueueEngineService queueEngineService;
    private final QueueTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public PatientController(QueueEngineService queueEngineService,
                             QueueTokenRepository tokenRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.queueEngineService = queueEngineService;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth == null) return null;
        return userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByPhone(auth.getName()))
                .orElse(null);
    }

    @PostMapping("/tokens/book")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> bookToken(@Valid @RequestBody TokenBookingRequest request,
                                                                      Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        try {
            QueueToken token = queueEngineService.bookToken(request, user);
            TokenStatusResponse response = queueEngineService.mapToResponse(token);
            return ResponseEntity.ok(ApiResponse.ok("Token booked successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/tokens/active")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> getActiveToken(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }

        List<TokenStatus> activeStatuses = Arrays.asList(TokenStatus.WAITING, TokenStatus.CALLED);
        List<QueueToken> activeTokens = tokenRepository.findActiveTokensByUserOrPhone(
                user.getId(), user.getPhone(), LocalDate.now(), activeStatuses
        );

        if (activeTokens.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok("No active tokens found", null));
        }

        TokenStatusResponse resp = queueEngineService.mapToResponse(activeTokens.get(0));
        return ResponseEntity.ok(ApiResponse.ok("Active token retrieved", resp));
    }

    @GetMapping("/tokens/history")
    public ResponseEntity<ApiResponse<List<TokenStatusResponse>>> getTokenHistory(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }

        List<QueueToken> tokens = tokenRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<TokenStatusResponse> resp = tokens.stream().map(queueEngineService::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Token history retrieved", resp));
    }

    @PostMapping("/tokens/{id}/cancel")
    public ResponseEntity<ApiResponse<TokenStatusResponse>> cancelToken(@PathVariable Long id,
                                                                        Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        try {
            QueueToken cancelled = queueEngineService.cancelToken(id, user, "Cancelled by patient via smartphone");
            return ResponseEntity.ok(ApiResponse.ok("Token cancelled successfully", queueEngineService.mapToResponse(cancelled)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotifications(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }

        List<Notification> list = notificationService.getUserNotifications(user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notifications list", list));
    }
}
