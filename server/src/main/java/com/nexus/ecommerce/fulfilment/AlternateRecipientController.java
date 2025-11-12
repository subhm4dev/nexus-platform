package com.nexus.ecommerce.fulfilment.controller;

import com.nexus.ecommerce.fulfilment.model.request.ConfirmDeliveryRequest;
import com.nexus.ecommerce.fulfilment.model.request.ShareDeliveryLinkRequest;
import com.nexus.ecommerce.fulfilment.model.response.AlternateRecipientResponse;
import com.nexus.ecommerce.fulfilment.model.response.ShareLinkResponse;
import com.nexus.ecommerce.fulfilment.service.AlternateRecipientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for alternate recipient management
 */
@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Slf4j
public class AlternateRecipientController {
    
    private final AlternateRecipientService recipientService;
    
    /**
     * Share delivery link with alternate recipient(s)
     * Customer can share with unlimited alternate phone numbers/users
     */
    @PostMapping("/{deliveryId}/share-link")
    public ResponseEntity<ShareLinkResponse> shareDeliveryLink(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-User-Id") UUID customerUserId,
        @RequestHeader("X-Tenant-Id") UUID tenantId,
        @Valid @RequestBody ShareDeliveryLinkRequest request
    ) {
        log.info("Sharing delivery link: deliveryId={}, customerUserId={}", deliveryId, customerUserId);
        ShareLinkResponse response = recipientService.shareDeliveryLink(
            deliveryId, customerUserId, tenantId, request
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get alternate recipients for a delivery
     */
    @GetMapping("/{deliveryId}/alternate-recipients")
    public ResponseEntity<List<AlternateRecipientResponse>> getAlternateRecipients(
        @PathVariable UUID deliveryId,
        @RequestHeader("X-Tenant-Id") UUID tenantId
    ) {
        List<AlternateRecipientResponse> recipients = recipientService.getByDeliveryId(deliveryId, tenantId);
        return ResponseEntity.ok(recipients);
    }
    
    /**
     * Revoke share link
     */
    @DeleteMapping("/{deliveryId}/share-link/{recipientId}")
    public ResponseEntity<Void> revokeShareLink(
        @PathVariable UUID deliveryId,
        @PathVariable UUID recipientId,
        @RequestHeader("X-User-Id") UUID userId,
        @RequestHeader("X-Tenant-Id") UUID tenantId,
        @RequestParam(required = false) String reason
    ) {
        recipientService.revokeShareLink(recipientId, userId, tenantId, reason);
        return ResponseEntity.noContent().build();
    }
}

/**
 * Public controller for alternate recipients (no auth required)
 */
@RestController
@RequestMapping("/api/v1/public/delivery")
@RequiredArgsConstructor
@Slf4j
class PublicAlternateRecipientController {
    
    private final AlternateRecipientService recipientService;
    
    /**
     * Get share link details (public, no auth)
     */
    @GetMapping("/share/{shareToken}")
    public ResponseEntity<AlternateRecipientResponse> getShareLinkDetails(
        @PathVariable String shareToken
    ) {
        AlternateRecipientResponse response = recipientService.getByShareToken(shareToken);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Alternate recipient confirms delivery (public, no auth)
     * Same dual confirmation criteria applies - agent proximity must match
     */
    @PostMapping("/share/{shareToken}/confirm")
    public ResponseEntity<AlternateRecipientResponse> alternateRecipientConfirm(
        @PathVariable String shareToken,
        @Valid @RequestBody ConfirmDeliveryRequest request
    ) {
        log.info("Alternate recipient confirming delivery: shareToken={}", shareToken);
        
        // Get recipient by token
        AlternateRecipientResponse recipient = recipientService.getByShareToken(shareToken);
        
        // Record confirmation with location
        AlternateRecipientResponse confirmed = recipientService.recordConfirmation(
            recipient.id(),
            request.latitude().doubleValue(),
            request.longitude().doubleValue(),
            request.locationAccuracy().doubleValue()
        );
        
        // TODO: Integrate with DeliveryConfirmationService to check proximity with agent
        // and complete dual confirmation
        
        return ResponseEntity.ok(confirmed);
    }
}

