package com.nexus.healthcare.notification.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.notification.model.request.NotificationRequest;
import com.nexus.healthcare.notification.model.response.NotificationResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.healthcare.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthcare/notifications")
@Tag(name = "Notification Management", description = "Healthcare-specific notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping
    @Operation(summary = "Send notification", description = "Sends a notification")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        NotificationResponse response = notificationService.sendNotification(tenantId, domainId, request);
        return ApiResponse.success(response, "Notification sent successfully");
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications by user", description = "Retrieves all notifications for a user")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<NotificationResponse>> getNotificationsByUser(
            @PathVariable UUID userId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        List<NotificationResponse> notifications = notificationService.getNotificationsByUser(userId, tenantId, domainId);
        return ApiResponse.success(notifications);
    }
    
    private UUID getTenantIdFromAuthentication(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Tenant ID is required");
        }
        return UUID.fromString(((JwtAuthenticationToken) authentication).getTenantId());
    }
    
    private UUID getDomainIdFromAuthentication(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Domain ID is required");
        }
        String domainIdStr = ((JwtAuthenticationToken) authentication).getDomainId();
        if (domainIdStr == null || domainIdStr.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Domain ID is missing in JWT");
        }
        try {
            return UUID.fromString(domainIdStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid domain ID format: " + domainIdStr);
        }
    }
}

