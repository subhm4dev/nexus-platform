package com.nexus.ecommerce.fulfilment.controller;

import com.nexus.ecommerce.fulfilment.model.request.ConfirmDeliveryRequest;
import com.nexus.ecommerce.fulfilment.model.response.DeliveryConfirmationResponse;
import com.nexus.ecommerce.fulfilment.service.DeliveryConfirmationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for dual-confirmation delivery system
 */
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryConfirmationController {
    
    private final DeliveryConfirmationService confirmationService;
    
    /**
     * Agent confirms delivery (requires proximity)
     */
    @PostMapping("/{deliveryId}/confirm")
    public ResponseEntity<DeliveryConfirmationResponse> agentConfirmDelivery(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-User-Id") UUID agentUserId,
        @RequestHeader("X-Tenant-Id") UUID tenantId,
        @Valid @RequestBody ConfirmDeliveryRequest request
    ) {
        log.info("Agent confirming delivery: deliveryId={}, agentUserId={}", deliveryId, agentUserId);
        DeliveryConfirmationResponse response = confirmationService.agentConfirmDelivery(
            deliveryId, agentUserId, tenantId, request
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Customer confirms delivery (requires proximity)
     */
    @PostMapping("/{deliveryId}/customer-confirm")
    public ResponseEntity<DeliveryConfirmationResponse> customerConfirmDelivery(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-User-Id") UUID customerUserId,
        @RequestHeader("X-Tenant-Id") UUID tenantId,
        @Valid @RequestBody ConfirmDeliveryRequest request
    ) {
        log.info("Customer confirming delivery: deliveryId={}, customerUserId={}", deliveryId, customerUserId);
        DeliveryConfirmationResponse response = confirmationService.customerConfirmDelivery(
            deliveryId, customerUserId, tenantId, request
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Agent marks customer as unavailable
     */
    @PostMapping("/{deliveryId}/mark-unavailable/agent")
    public ResponseEntity<DeliveryConfirmationResponse> agentMarkUnavailable(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-User-Id") UUID agentUserId,
        @RequestHeader("X-Tenant-Id") UUID tenantId,
        @Valid @RequestBody ConfirmDeliveryRequest request,
        @RequestParam(required = false) String reason
    ) {
        DeliveryConfirmationResponse response = confirmationService.agentMarkUnavailable(
            deliveryId, agentUserId, tenantId, request, reason
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Customer marks agent as unavailable
     */
    @PostMapping("/{deliveryId}/mark-unavailable/customer")
    public ResponseEntity<DeliveryConfirmationResponse> customerMarkUnavailable(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-User-Id") UUID customerUserId,
        @RequestHeader("X-Tenant-Id") UUID tenantId,
        @Valid @RequestBody ConfirmDeliveryRequest request,
        @RequestParam(required = false) String reason
    ) {
        DeliveryConfirmationResponse response = confirmationService.customerMarkUnavailable(
            deliveryId, customerUserId, tenantId, request, reason
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get confirmation status
     */
    @GetMapping("/{deliveryId}/confirmation-status")
    public ResponseEntity<DeliveryConfirmationResponse> getConfirmationStatus(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-User-Id") UUID userId,
        @RequestHeader("X-Tenant-Id") UUID tenantId,
        @RequestParam(defaultValue = "false") boolean isAgent
    ) {
        DeliveryConfirmationResponse response = confirmationService.getConfirmationStatus(
            deliveryId, userId, tenantId, isAgent
        );
        return ResponseEntity.ok(response);
    }
}

