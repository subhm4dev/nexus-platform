package com.nexus.healthcare.patient.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.patient.model.request.PatientRequest;
import com.nexus.healthcare.patient.model.response.PatientResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.healthcare.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthcare/patients")
@Tag(name = "Patient Management", description = "Patient records and medical information")
@RequiredArgsConstructor
@Slf4j
public class PatientController {
    
    private final PatientService patientService;
    
    @PostMapping
    @Operation(summary = "Create patient", description = "Creates a new patient profile")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PatientResponse> createPatient(
            @Valid @RequestBody PatientRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        PatientResponse response = patientService.createPatient(userId, tenantId, domainId, request);
        return ApiResponse.success(response, "Patient created successfully");
    }
    
    @GetMapping("/{patientId}")
    @Operation(summary = "Get patient by ID", description = "Retrieves patient profile by ID")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PatientResponse> getPatientById(
            @PathVariable UUID patientId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        PatientResponse response = patientService.getPatientById(patientId, tenantId, domainId);
        return ApiResponse.success(response);
    }
    
    @GetMapping
    @Operation(summary = "Search patients", description = "Search patients with filters")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Page<PatientResponse>> searchPatients(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        Page<PatientResponse> response = patientService.searchPatients(tenantId, domainId, query, page, size);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/{patientId}")
    @Operation(summary = "Update patient", description = "Updates patient profile")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PatientResponse> updatePatient(
            @PathVariable UUID patientId,
            @Valid @RequestBody PatientRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        PatientResponse response = patientService.updatePatient(patientId, tenantId, domainId, request);
        return ApiResponse.success(response, "Patient updated successfully");
    }
    
    @DeleteMapping("/{patientId}")
    @Operation(summary = "Delete patient", description = "Soft deletes patient profile")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ApiResponse<Void> deletePatient(
            @PathVariable UUID patientId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        patientService.deletePatient(patientId, tenantId, domainId);
        return ApiResponse.success(null, "Patient deleted successfully");
    }
    
    @PostMapping("/{patientId}/transfer")
    @Operation(summary = "Transfer patient to another branch", description = "Transfers patient to another branch. Updates tenant_id and transfers all medical history, allergies, medications, insurance, and emergency contacts. Requires ADMIN or RECEPTIONIST role.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ApiResponse<PatientResponse> transferPatient(
            @PathVariable UUID patientId,
            @RequestParam UUID targetTenantId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID currentTenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        String jwtToken = getJwtTokenFromAuthentication(authentication);
        
        PatientResponse response = patientService.transferPatient(patientId, currentTenantId, targetTenantId, domainId, jwtToken);
        return ApiResponse.success(response, "Patient transferred successfully");
    }
    
    @PostMapping("/{patientId}/add-to-branch")
    @Operation(summary = "Add patient to another branch", description = "Adds patient to another branch while keeping at current branch. Useful for patients visiting multiple branches. Requires ADMIN or RECEPTIONIST role.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ApiResponse<PatientResponse> addPatientToBranch(
            @PathVariable UUID patientId,
            @RequestParam UUID targetTenantId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID currentTenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        String jwtToken = getJwtTokenFromAuthentication(authentication);
        
        PatientResponse response = patientService.addPatientToBranch(patientId, currentTenantId, targetTenantId, domainId, jwtToken);
        return ApiResponse.success(response, "Patient added to branch successfully");
    }
    
    private UUID getUserIdFromAuthentication(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User ID is required");
        }
        return UUID.fromString(((JwtAuthenticationToken) authentication).getUserId());
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
    
    private String getJwtTokenFromAuthentication(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "JWT token is required");
        }
        return ((JwtAuthenticationToken) authentication).getToken();
    }
}

