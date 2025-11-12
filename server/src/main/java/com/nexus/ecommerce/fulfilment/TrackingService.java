package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.model.response.TrackingHistoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Tracking Service Interface
 */
public interface TrackingService {
    
    /**
     * Get tracking history for a delivery
     */
    List<TrackingHistoryResponse> getTrackingHistory(UUID deliveryId, UUID tenantId);
}

