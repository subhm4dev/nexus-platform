package com.nexus.healthcare.appointment.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.appointment.model.request.PosPaymentRequest;
import com.nexus.healthcare.appointment.model.response.PaymentResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.healthcare.appointment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthcare/payments")
@Tag(name = "Healthcare Payment Management", description = "POS payments, receipts, and cash register management")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/pos")
    @Operation(summary = "Process POS payment", description = "Processes payment at Point of Sale")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PaymentResponse> processPosPayment(
            @Valid @RequestBody PosPaymentRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        PaymentResponse response = paymentService.processPosPayment(userId, tenantId, domainId, request);
        return ApiResponse.success(response, "Payment processed successfully");
    }
    
    @PostMapping("/manual")
    @Operation(summary = "Manual payment entry", description = "Manual payment entry by admin")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PaymentResponse> processManualPayment(
            @Valid @RequestBody PosPaymentRequest request,
            org.springframework.security.core.Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        PaymentResponse response = paymentService.processManualPayment(userId, tenantId, domainId, request);
        return ApiResponse.success(response, "Payment recorded successfully");
    }
    
    @GetMapping("/{paymentId}/receipt")
    @Operation(summary = "Generate receipt", description = "Generates payment receipt (PDF)")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<String> generateReceipt(
            @PathVariable UUID paymentId,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        // TODO: Implement receipt generation
        String receiptUrl = paymentService.generateReceipt(paymentId, tenantId, domainId);
        return ApiResponse.success(receiptUrl, "Receipt generated successfully");
    }
    
    @PostMapping("/cash-register/open")
    @Operation(summary = "Open cash register", description = "Opens cash register for the day")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> openCashRegister(
            @RequestParam(defaultValue = "0.00") java.math.BigDecimal openingBalance,
            @RequestParam(required = false) String notes,
            org.springframework.security.core.Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        paymentService.openCashRegister(userId, tenantId, domainId, openingBalance, notes);
        return ApiResponse.success(null, "Cash register opened successfully");
    }
    
    @PostMapping("/cash-register/close")
    @Operation(summary = "Close cash register", description = "Closes cash register for the day")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> closeCashRegister(
            @RequestParam java.math.BigDecimal closingBalance,
            @RequestParam(required = false) String notes,
            org.springframework.security.core.Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        paymentService.closeCashRegister(userId, tenantId, domainId, closingBalance, notes);
        return ApiResponse.success(null, "Cash register closed successfully");
    }
    
    @GetMapping("/cash-register/balance")
    @Operation(summary = "Get cash register balance", description = "Gets current cash register balance")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<CashRegisterBalanceResponse> getCashRegisterBalance(
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        CashRegisterBalanceResponse response = paymentService.getCashRegisterBalance(tenantId, domainId);
        return ApiResponse.success(response);
    }
    
    @GetMapping("/reconciliation")
    @Operation(summary = "Payment reconciliation", description = "Gets payment reconciliation report")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<PaymentReconciliationResponse> getPaymentReconciliation(
            @RequestParam(required = false) java.time.LocalDate date,
            org.springframework.security.core.Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        PaymentReconciliationResponse response = paymentService.getPaymentReconciliation(tenantId, domainId, date);
        return ApiResponse.success(response);
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
    
    // Inner classes for responses
    public static class CashRegisterBalanceResponse {
        public java.math.BigDecimal openingBalance;
        public java.math.BigDecimal cashReceived;
        public java.math.BigDecimal cashDisbursed;
        public java.math.BigDecimal closingBalance;
    }
    
    public static class PaymentReconciliationResponse {
        public java.math.BigDecimal totalCollected;
        public java.util.Map<String, java.math.BigDecimal> byMethod;
        public java.util.Map<String, java.math.BigDecimal> bySource;
    }
}

