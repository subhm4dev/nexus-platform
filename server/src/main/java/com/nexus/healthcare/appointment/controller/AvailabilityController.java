package com.nexus.healthcare.appointment.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.appointment.model.request.AvailabilityRequest;
import com.nexus.healthcare.appointment.model.response.AvailabilityResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.healthcare.appointment.service.AvailabilityService;
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
@RequestMapping("/api/v1/healthcare/availabilities")
@Tag(name = "Availability Management", description = "Doctor availability and time-off management")
@RequiredArgsConstructor
@Slf4j
public class AvailabilityController {
    
    private final AvailabilityService availabilityService;
    
    @PostMapping
    @Operation(summary = "Create availability", description = "Creates doctor availability schedule")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AvailabilityResponse> createAvailability(
            @Valid @RequestBody AvailabilityRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        AvailabilityResponse response = availabilityService.createAvailability(tenantId, domainId, request);
        return ApiResponse.success(response, "Availability created successfully");
    }
    
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get availabilities by doctor", description = "Retrieves all availabilities for a doctor")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AvailabilityResponse>> getAvailabilitiesByDoctor(
            @PathVariable UUID doctorId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        List<AvailabilityResponse> availabilities = availabilityService.getAvailabilitiesByDoctor(doctorId, tenantId, domainId);
        return ApiResponse.success(availabilities);
    }
    
    @PutMapping("/{availabilityId}")
    @Operation(summary = "Update availability", description = "Updates availability")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AvailabilityResponse> updateAvailability(
            @PathVariable UUID availabilityId,
            @Valid @RequestBody AvailabilityRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        AvailabilityResponse response = availabilityService.updateAvailability(availabilityId, tenantId, domainId, request);
        return ApiResponse.success(response, "Availability updated successfully");
    }
    
    @DeleteMapping("/{availabilityId}")
    @Operation(summary = "Delete availability", description = "Deactivates availability")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deleteAvailability(
            @PathVariable UUID availabilityId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        availabilityService.deleteAvailability(availabilityId, tenantId, domainId);
        return ApiResponse.success(null, "Availability deleted successfully");
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

