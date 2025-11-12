package com.nexus.healthcare.appointment.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.appointment.model.request.TimeOffRequest;
import com.nexus.healthcare.appointment.model.response.TimeOffResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.healthcare.appointment.service.TimeOffService;
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
@RequestMapping("/api/v1/healthcare/time-offs")
@Tag(name = "Time-Off Management", description = "Doctor time-off periods management")
@RequiredArgsConstructor
@Slf4j
public class TimeOffController {
    
    private final TimeOffService timeOffService;
    
    @PostMapping
    @Operation(summary = "Create time-off", description = "Creates a time-off period for a doctor")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<TimeOffResponse> createTimeOff(
            @Valid @RequestBody TimeOffRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        TimeOffResponse response = timeOffService.createTimeOff(tenantId, domainId, request);
        return ApiResponse.success(response, "Time-off created successfully");
    }
    
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get time-offs by doctor", description = "Retrieves all time-offs for a doctor")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<TimeOffResponse>> getTimeOffsByDoctor(
            @PathVariable UUID doctorId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        List<TimeOffResponse> timeOffs = timeOffService.getTimeOffsByDoctor(doctorId, tenantId, domainId);
        return ApiResponse.success(timeOffs);
    }
    
    @PutMapping("/{timeOffId}")
    @Operation(summary = "Update time-off", description = "Updates time-off")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<TimeOffResponse> updateTimeOff(
            @PathVariable UUID timeOffId,
            @Valid @RequestBody TimeOffRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        TimeOffResponse response = timeOffService.updateTimeOff(timeOffId, tenantId, domainId, request);
        return ApiResponse.success(response, "Time-off updated successfully");
    }
    
    @DeleteMapping("/{timeOffId}")
    @Operation(summary = "Delete time-off", description = "Deletes time-off")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deleteTimeOff(
            @PathVariable UUID timeOffId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        timeOffService.deleteTimeOff(timeOffId, tenantId, domainId);
        return ApiResponse.success(null, "Time-off deleted successfully");
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

