package com.nexus.healthcare.report.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.healthcare.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthcare/reports")
@Tag(name = "Report Management", description = "Appointment reports, analytics, and Excel exports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    
    private final ReportService reportService;
    
    @GetMapping("/appointments/excel")
    @Operation(summary = "Generate appointment report Excel", description = "Generates Excel report with appointment and payment data")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Resource> generateAppointmentReportExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) UUID doctorId,
            @RequestParam(required = false) UUID patientId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        Resource resource = reportService.generateAppointmentReportExcel(
            tenantId, domainId, startDate, endDate, status, paymentStatus, doctorId, patientId);
        
        String filename = "appointment-report-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }
    
    @GetMapping("/payments/reconciliation")
    @Operation(summary = "Generate payment reconciliation report", description = "Generates payment reconciliation report")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Resource> generatePaymentReconciliationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        Resource resource = reportService.generatePaymentReconciliationReport(tenantId, domainId, date);
        
        String filename = "payment-reconciliation-" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
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

