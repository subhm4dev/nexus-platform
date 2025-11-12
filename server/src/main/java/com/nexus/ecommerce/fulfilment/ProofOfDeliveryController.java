package com.nexus.ecommerce.fulfilment.controller;

import com.nexus.ecommerce.fulfilment.model.request.UploadPODRequest;
import com.nexus.ecommerce.fulfilment.model.response.ProofOfDeliveryResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.fulfilment.service.ProofOfDeliveryService;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@Tag(name = "Proof of Delivery", description = "POD management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class ProofOfDeliveryController {
    
    private final ProofOfDeliveryService podService;
    
    @PostMapping("/{deliveryId}/proof")
    @Operation(summary = "Upload proof of delivery", description = "Uploads POD (photo, signature, OTP)")
    public ResponseEntity<ApiResponse<ProofOfDeliveryResponse>> uploadPOD(
        @PathVariable UUID deliveryId,
        @Valid @RequestBody UploadPODRequest request,
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        UUID driverId = getUserId(authentication);
        
        ProofOfDeliveryResponse response = podService.uploadPOD(
            deliveryId, driverId, tenantId, request
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "POD uploaded"));
    }
    
    @GetMapping("/{deliveryId}/proof")
    @Operation(summary = "Get proof of delivery", description = "Gets POD for a delivery")
    public ResponseEntity<ApiResponse<ProofOfDeliveryResponse>> getPOD(
        @PathVariable UUID deliveryId,
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        ProofOfDeliveryResponse response = podService.getPODByDeliveryId(deliveryId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{deliveryId}/proof/verify-otp")
    @Operation(summary = "Verify OTP for POD", description = "Verifies OTP for proof of delivery")
    public ResponseEntity<ApiResponse<ProofOfDeliveryResponse>> verifyOTP(
        @PathVariable UUID deliveryId,
        @RequestParam String otpCode,
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        ProofOfDeliveryResponse response = podService.verifyOTP(deliveryId, otpCode, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response, "OTP verified"));
    }
    
    private UUID getTenantId(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken token) {
            return UUID.fromString(token.getTenantId());
        }
        throw new IllegalStateException("Invalid authentication");
    }
    
    private UUID getUserId(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken token) {
            return UUID.fromString(token.getUserId());
        }
        throw new IllegalStateException("Invalid authentication");
    }
}

