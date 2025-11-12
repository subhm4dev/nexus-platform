package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.model.request.UpdateDeliveryPreferenceRequest;
import com.nexus.ecommerce.fulfilment.model.response.DeliveryPreferenceResponse;

import java.util.UUID;

/**
 * Service for delivery preferences management
 */
public interface DeliveryPreferenceService {
    
    /**
     * Update delivery preferences
     */
    DeliveryPreferenceResponse updatePreferences(
        UUID fulfillmentId,
        UUID customerUserId,
        UUID tenantId,
        UpdateDeliveryPreferenceRequest request
    );
    
    /**
     * Get preferences for a fulfillment
     */
    DeliveryPreferenceResponse getPreferences(UUID fulfillmentId, UUID tenantId);
}

