package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.model.request.ShareDeliveryLinkRequest;
import com.nexus.ecommerce.fulfilment.model.response.AlternateRecipientResponse;
import com.nexus.ecommerce.fulfilment.model.response.ShareLinkResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing alternate recipients who can receive orders on behalf of customers
 */
public interface AlternateRecipientService {
    
    /**
     * Share delivery link with alternate recipient(s)
     * Customer can share with multiple alternate phone numbers/users
     * 
     * @param deliveryId Delivery ID
     * @param customerUserId Customer user ID who is sharing
     * @param tenantId Tenant ID
     * @param request Share request with alternate recipient details
     * @return Share link response with shareable links
     */
    ShareLinkResponse shareDeliveryLink(
        UUID deliveryId,
        UUID customerUserId,
        UUID tenantId,
        ShareDeliveryLinkRequest request
    );
    
    /**
     * Get alternate recipient by share token (for link access)
     * 
     * @param shareToken Share token from link
     * @return Alternate recipient details
     */
    AlternateRecipientResponse getByShareToken(String shareToken);
    
    /**
     * Get all alternate recipients for a delivery
     * 
     * @param deliveryId Delivery ID
     * @param tenantId Tenant ID
     * @return List of alternate recipients
     */
    List<AlternateRecipientResponse> getByDeliveryId(UUID deliveryId, UUID tenantId);
    
    /**
     * Revoke share link
     * 
     * @param recipientId Alternate recipient ID
     * @param userId User ID revoking (customer or admin)
     * @param tenantId Tenant ID
     * @param reason Revoke reason
     */
    void revokeShareLink(UUID recipientId, UUID userId, UUID tenantId, String reason);
    
    /**
     * Get active alternate recipients for proximity check
     * 
     * @param deliveryId Delivery ID
     * @return List of active alternate recipients with location data
     */
    List<AlternateRecipientLocation> getActiveRecipientsForProximity(UUID deliveryId);
    
    /**
     * Record alternate recipient confirmation
     * Called when alternate recipient confirms delivery
     * 
     * @param recipientId Alternate recipient ID
     * @param latitude Confirmation latitude
     * @param longitude Confirmation longitude
     * @param locationAccuracy Location accuracy
     * @return Updated alternate recipient
     */
    AlternateRecipientResponse recordConfirmation(
        UUID recipientId,
        double latitude,
        double longitude,
        double locationAccuracy
    );
    
    /**
     * Process expired links (scheduled job)
     */
    void processExpiredLinks();
    
    /**
     * Alternate recipient location data for proximity checks
     */
    record AlternateRecipientLocation(
        UUID recipientId,
        java.math.BigDecimal latitude,
        java.math.BigDecimal longitude,
        java.math.BigDecimal locationAccuracy,
        String name,
        String phoneNumber
    ) {}
}

