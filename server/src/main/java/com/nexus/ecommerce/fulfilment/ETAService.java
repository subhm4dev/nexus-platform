package com.nexus.ecommerce.fulfilment.service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for calculating estimated delivery time (ETA)
 */
public interface ETAService {
    
    /**
     * Calculate ETA for a delivery
     */
    LocalDateTime calculateETA(UUID deliveryId, UUID tenantId);
    
    /**
     * Get ETA with time window
     */
    ETAWithWindow getETAWithWindow(UUID deliveryId, UUID tenantId);
    
    /**
     * Update ETA based on current location
     */
    LocalDateTime updateETA(UUID deliveryId, UUID tenantId);
    
    record ETAWithWindow(
        LocalDateTime estimatedArrival,
        LocalDateTime windowStart,
        LocalDateTime windowEnd,
        String timeWindow, // "2-4 PM"
        Integer minutesRemaining
    ) {}
}

