package com.nexus.healthcare.doctor.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.doctor.model.request.DoctorRequest;
import com.nexus.healthcare.doctor.model.response.DoctorResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.healthcare.doctor.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthcare/doctors")
@Tag(name = "Doctor Management", description = "Doctor profiles, specializations, qualifications, and awards")
@RequiredArgsConstructor
@Slf4j
public class DoctorController {
    
    private final DoctorService doctorService;
    
    @PostMapping
    @Operation(summary = "Create doctor profile", description = "Creates a new doctor profile")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<DoctorResponse> createDoctor(
            @Valid @RequestBody DoctorRequest request,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        DoctorResponse response = doctorService.createDoctor(userId, tenantId, domainId, request);
        return ApiResponse.success(response, "Doctor created successfully");
    }
    
    @GetMapping("/{doctorId}")
    @Operation(summary = "Get doctor by ID", description = "Retrieves doctor profile by ID")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<DoctorResponse> getDoctorById(
            @PathVariable UUID doctorId,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        DoctorResponse response = doctorService.getDoctorById(doctorId, tenantId, domainId);
        return ApiResponse.success(response);
    }
    
    @GetMapping
    @Operation(summary = "Search doctors", description = "Search doctors with filters")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Page<DoctorResponse>> searchDoctors(
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        Page<DoctorResponse> response = doctorService.searchDoctors(tenantId, domainId, verificationStatus, query, page, size);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/{doctorId}")
    @Operation(summary = "Update doctor", description = "Updates doctor profile")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<DoctorResponse> updateDoctor(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorRequest request,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        DoctorResponse response = doctorService.updateDoctor(doctorId, tenantId, domainId, request);
        return ApiResponse.success(response, "Doctor updated successfully");
    }
    
    @DeleteMapping("/{doctorId}")
    @Operation(summary = "Delete doctor", description = "Soft deletes doctor profile")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deleteDoctor(
            @PathVariable UUID doctorId,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        doctorService.deleteDoctor(doctorId, tenantId, domainId);
        return ApiResponse.success(null, "Doctor deleted successfully");
    }
    
    @PutMapping("/{doctorId}/verify")
    @Operation(summary = "Verify doctor", description = "Updates doctor verification status")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DoctorResponse> verifyDoctor(
            @PathVariable UUID doctorId,
            @RequestParam String verificationStatus,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        DoctorResponse response = doctorService.verifyDoctor(doctorId, tenantId, domainId, verificationStatus);
        return ApiResponse.success(response, "Doctor verification status updated");
    }
    
    @PostMapping("/{doctorId}/transfer")
    @Operation(summary = "Transfer doctor to another branch", description = "Transfers doctor to another branch. Soft deletes at current branch, creates new at target branch. Requires ADMIN or RECEPTIONIST role.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ApiResponse<DoctorResponse> transferDoctor(
            @PathVariable UUID doctorId,
            @RequestParam UUID targetTenantId,
            Authentication authentication) {
        
        UUID currentTenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        String jwtToken = getJwtTokenFromAuthentication(authentication);
        
        DoctorResponse response = doctorService.transferDoctor(doctorId, currentTenantId, targetTenantId, domainId, jwtToken);
        return ApiResponse.success(response, "Doctor transferred successfully");
    }
    
    @PostMapping("/{doctorId}/add-to-branch")
    @Operation(summary = "Add doctor to another branch", description = "Adds doctor to another branch while keeping at current branch. Useful for doctors working at multiple branches. Requires ADMIN or RECEPTIONIST role.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ApiResponse<DoctorResponse> addDoctorToBranch(
            @PathVariable UUID doctorId,
            @RequestParam UUID targetTenantId,
            Authentication authentication) {
        
        UUID currentTenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        String jwtToken = getJwtTokenFromAuthentication(authentication);
        
        DoctorResponse response = doctorService.addDoctorToBranch(doctorId, currentTenantId, targetTenantId, domainId, jwtToken);
        return ApiResponse.success(response, "Doctor added to branch successfully");
    }
    
    @GetMapping("/qualifications/names")
    @Operation(summary = "Get distinct qualification names", description = "Returns a list of distinct qualification names from existing qualifications. Used for populating dropdowns.")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<String>> getQualificationNames() {
        List<String> names = doctorService.getDistinctQualificationNames();
        return ApiResponse.success(names);
    }
    
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User ID is required");
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        return UUID.fromString(jwtAuth.getUserId());
    }
    
    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Tenant ID is required");
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        return UUID.fromString(jwtAuth.getTenantId());
    }
    
    private UUID getDomainIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Domain ID is required");
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        String domainIdStr = jwtAuth.getDomainId();
        if (domainIdStr == null || domainIdStr.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Domain ID is missing in JWT");
        }
        try {
            return UUID.fromString(domainIdStr);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid domain ID format: " + domainIdStr);
        }
    }
    
    private String getJwtTokenFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "JWT token is required");
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        return jwtAuth.getToken();
    }
}

