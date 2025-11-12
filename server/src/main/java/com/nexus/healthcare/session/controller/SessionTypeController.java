package com.nexus.healthcare.session.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.session.model.request.SessionTypeRequest;
import com.nexus.healthcare.session.model.response.SessionTypeResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.healthcare.session.service.SessionTypeService;
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
@RequestMapping("/api/v1/healthcare/sessions")
@Tag(name = "Session Management", description = "Session types and offerings")
@RequiredArgsConstructor
@Slf4j
public class SessionTypeController {
    
    private final SessionTypeService sessionTypeService;
    
    @PostMapping("/types")
    @Operation(summary = "Create session type", description = "Creates a new session type")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<SessionTypeResponse> createSessionType(
            @Valid @RequestBody SessionTypeRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        SessionTypeResponse response = sessionTypeService.createSessionType(tenantId, domainId, request);
        return ApiResponse.success(response, "Session type created successfully");
    }
    
    @GetMapping("/types")
    @Operation(summary = "Get active session types", description = "Retrieves all active session types")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<SessionTypeResponse>> getActiveSessionTypes(
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        List<SessionTypeResponse> response = sessionTypeService.getActiveSessionTypes(tenantId, domainId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/types/{id}")
    @Operation(summary = "Get session type by ID", description = "Retrieves session type by ID")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<SessionTypeResponse> getSessionTypeById(
            @PathVariable UUID id,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        SessionTypeResponse response = sessionTypeService.getSessionTypeById(id, tenantId, domainId);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/types/{id}")
    @Operation(summary = "Update session type", description = "Updates session type")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<SessionTypeResponse> updateSessionType(
            @PathVariable UUID id,
            @Valid @RequestBody SessionTypeRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        SessionTypeResponse response = sessionTypeService.updateSessionType(id, tenantId, domainId, request);
        return ApiResponse.success(response, "Session type updated successfully");
    }
    
    @DeleteMapping("/types/{id}")
    @Operation(summary = "Delete session type", description = "Deactivates session type")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deleteSessionType(
            @PathVariable UUID id,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        sessionTypeService.deleteSessionType(id, tenantId, domainId);
        return ApiResponse.success(null, "Session type deleted successfully");
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

