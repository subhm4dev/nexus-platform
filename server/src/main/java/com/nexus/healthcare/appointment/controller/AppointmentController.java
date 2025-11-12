package com.nexus.healthcare.appointment.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.appointment.model.request.AppointmentRequest;
import com.nexus.healthcare.appointment.model.response.AppointmentResponse;
import com.nexus.healthcare.appointment.model.response.AvailableSlot;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.healthcare.appointment.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthcare/appointments")
@Tag(name = "Appointment Management", description = "Appointment booking, scheduling, and slot management")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @PostMapping
    @Operation(summary = "Create appointment", description = "Creates a new appointment with slot validation")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AppointmentResponse> createAppointment(
            @Valid @RequestBody AppointmentRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        AppointmentResponse response = appointmentService.createAppointment(userId, tenantId, domainId, request);
        return ApiResponse.success(response, "Appointment created successfully");
    }
    
    @GetMapping("/{appointmentId}")
    @Operation(summary = "Get appointment by ID", description = "Retrieves appointment by ID")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AppointmentResponse> getAppointmentById(
            @PathVariable UUID appointmentId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        AppointmentResponse response = appointmentService.getAppointmentById(appointmentId, tenantId, domainId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/available-slots")
    @Operation(summary = "Get available slots", description = "Calculates available slots for a doctor on a specific date")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AvailableSlot>> getAvailableSlots(
            @RequestParam UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam UUID sessionTypeId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        List<AvailableSlot> slots = appointmentService.getAvailableSlots(doctorId, date, sessionTypeId, tenantId, domainId);
        return ApiResponse.success(slots);
    }
    
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get appointments by patient", description = "Retrieves all appointments for a patient")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AppointmentResponse>> getAppointmentsByPatient(
            @PathVariable UUID patientId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByPatient(patientId, tenantId, domainId);
        return ApiResponse.success(appointments);
    }
    
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get appointments by doctor", description = "Retrieves appointments for a doctor on a specific date")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<List<AppointmentResponse>> getAppointmentsByDoctor(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        List<AppointmentResponse> appointments = appointmentService.getAppointmentsByDoctor(doctorId, tenantId, domainId, date);
        return ApiResponse.success(appointments);
    }
    
    @PutMapping("/{appointmentId}")
    @Operation(summary = "Update appointment", description = "Updates appointment details")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AppointmentResponse> updateAppointment(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody AppointmentRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        AppointmentResponse response = appointmentService.updateAppointment(appointmentId, tenantId, domainId, request);
        return ApiResponse.success(response, "Appointment updated successfully");
    }
    
    @PostMapping("/{appointmentId}/cancel")
    @Operation(summary = "Cancel appointment", description = "Cancels an appointment and triggers refund if applicable")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AppointmentResponse> cancelAppointment(
            @PathVariable UUID appointmentId,
            @RequestParam(required = false) String reason,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId, tenantId, domainId, reason);
        return ApiResponse.success(response, "Appointment cancelled successfully");
    }
    
    @PostMapping("/{appointmentId}/complete")
    @Operation(summary = "Complete appointment", description = "Marks appointment as completed")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AppointmentResponse> completeAppointment(
            @PathVariable UUID appointmentId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        AppointmentResponse response = appointmentService.completeAppointment(appointmentId, tenantId, domainId);
        return ApiResponse.success(response, "Appointment completed successfully");
    }
    
    @PostMapping("/admin-booking")
    @Operation(summary = "Admin booking", description = "Admin creates appointment on behalf of patient and optionally sends payment link")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<AppointmentResponse> createAdminBooking(
            @Valid @RequestBody AppointmentRequest request,
            @RequestParam(defaultValue = "false") boolean sendPaymentLink,
            org.springframework.security.core.Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        AppointmentResponse response = appointmentService.createAdminBooking(userId, tenantId, domainId, request, sendPaymentLink);
        return ApiResponse.success(response, "Appointment created successfully");
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

