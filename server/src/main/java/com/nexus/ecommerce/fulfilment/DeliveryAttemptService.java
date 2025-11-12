package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.model.request.RecordDeliveryAttemptRequest;
import com.nexus.ecommerce.fulfilment.model.response.DeliveryAttemptResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service for tracking delivery attempts
 */
public interface DeliveryAttemptService {
    
    /**
     * Record a delivery attempt
     */
    DeliveryAttemptResponse recordAttempt(
        UUID deliveryId,
        UUID driverId,
        UUID tenantId,
        RecordDeliveryAttemptRequest request
    );
    
    /**
     * Get all attempts for a delivery
     */
    List<DeliveryAttemptResponse> getAttemptsByDeliveryId(UUID deliveryId, UUID tenantId);
    
    /**
     * Get attempt count for a delivery
     */
    Integer getAttemptCount(UUID deliveryId);
    
    /**
     * Schedule next attempt
     */
    DeliveryAttemptResponse scheduleNextAttempt(
        UUID deliveryId,
        UUID tenantId,
        java.time.LocalDateTime nextAttemptAt
    );
}

