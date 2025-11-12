package com.nexus.healthcare.consultation.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.consultation.model.request.ConsultationRequest;
import com.nexus.healthcare.consultation.model.response.ConsultationResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.healthcare.consultation.service.ConsultationService;
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
@RequestMapping("/api/v1/healthcare/consultations")
@Tag(name = "Consultation Management", description = "Consultation records, prescriptions, and notes")
@RequiredArgsConstructor
@Slf4j
public class ConsultationController {
    
    private final ConsultationService consultationService;
    
    @PostMapping
    @Operation(summary = "Create consultation", description = "Creates a new consultation record")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ConsultationResponse> createConsultation(
            @Valid @RequestBody ConsultationRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        ConsultationResponse response = consultationService.createConsultation(userId, tenantId, domainId, request);
        return ApiResponse.success(response, "Consultation created successfully");
    }
    
    @GetMapping("/{consultationId}")
    @Operation(summary = "Get consultation by ID", description = "Retrieves consultation by ID")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ConsultationResponse> getConsultationById(
            @PathVariable UUID consultationId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        ConsultationResponse response = consultationService.getConsultationById(consultationId, tenantId, domainId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Get consultation by appointment ID", description = "Retrieves consultation for an appointment")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ConsultationResponse> getConsultationByAppointmentId(
            @PathVariable UUID appointmentId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        ConsultationResponse response = consultationService.getConsultationByAppointmentId(appointmentId, tenantId, domainId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get consultations by patient", description = "Retrieves all consultations for a patient")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<ConsultationResponse>> getConsultationsByPatient(
            @PathVariable UUID patientId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        List<ConsultationResponse> consultations = consultationService.getConsultationsByPatient(patientId, tenantId, domainId);
        return ApiResponse.success(consultations);
    }
    
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get consultations by doctor", description = "Retrieves all consultations for a doctor")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<ConsultationResponse>> getConsultationsByDoctor(
            @PathVariable UUID doctorId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        List<ConsultationResponse> consultations = consultationService.getConsultationsByDoctor(doctorId, tenantId, domainId);
        return ApiResponse.success(consultations);
    }
    
    @PutMapping("/{consultationId}")
    @Operation(summary = "Update consultation", description = "Updates consultation details")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ConsultationResponse> updateConsultation(
            @PathVariable UUID consultationId,
            @Valid @RequestBody ConsultationRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        ConsultationResponse response = consultationService.updateConsultation(consultationId, tenantId, domainId, request);
        return ApiResponse.success(response, "Consultation updated successfully");
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
}

