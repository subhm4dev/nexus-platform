package com.nexus.ecommerce.fulfilment.controller;

import com.nexus.ecommerce.fulfilment.model.request.RecordDeliveryAttemptRequest;
import com.nexus.ecommerce.fulfilment.model.response.DeliveryAttemptResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.fulfilment.service.DeliveryAttemptService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery")
@Tag(name = "Delivery Attempts", description = "Delivery attempt tracking endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class DeliveryAttemptController {
    
    private final DeliveryAttemptService attemptService;
    
    @PostMapping("/{deliveryId}/attempt")
    @Operation(summary = "Record delivery attempt", description = "Records a delivery attempt by driver")
    public ResponseEntity<ApiResponse<DeliveryAttemptResponse>> recordAttempt(
        @PathVariable UUID deliveryId,
        @Valid @RequestBody RecordDeliveryAttemptRequest request,
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        UUID driverId = getUserId(authentication);
        
        DeliveryAttemptResponse response = attemptService.recordAttempt(
            deliveryId, driverId, tenantId, request
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "Attempt recorded"));
    }
    
    @GetMapping("/{deliveryId}/attempts")
    @Operation(summary = "Get delivery attempts", description = "Gets all attempts for a delivery")
    public ResponseEntity<ApiResponse<List<DeliveryAttemptResponse>>> getAttempts(
        @PathVariable UUID deliveryId,
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        List<DeliveryAttemptResponse> response = attemptService.getAttemptsByDeliveryId(deliveryId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
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

