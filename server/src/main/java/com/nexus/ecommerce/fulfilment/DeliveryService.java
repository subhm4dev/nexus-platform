package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.model.request.TrackDeliveryRequest;
import com.nexus.ecommerce.fulfilment.model.response.DeliveryResponse;
import com.nexus.ecommerce.fulfilment.provider.dto.TrackingResponse;

import java.util.List;
import java.util.UUID;

/**
 * Delivery Service Interface
 */
public interface DeliveryService {
    
    /**
     * Update delivery location (tracking)
     */
    DeliveryResponse trackDelivery(UUID deliveryId, UUID driverId, UUID tenantId, TrackDeliveryRequest request);
    
    /**
     * Get tracking information (public)
     */
    TrackingResponse getTracking(String trackingNumber, UUID tenantId);
    
    /**
     * Get delivery by ID
     */
    DeliveryResponse getDeliveryById(UUID deliveryId, UUID tenantId, List<String> userRoles);
    
    /**
     * Mark delivery as completed
     */
    DeliveryResponse completeDelivery(UUID deliveryId, UUID driverId, UUID tenantId);
    
    /**
     * Get deliveries for a driver
     */
    List<DeliveryResponse> getDeliveriesByDriver(UUID driverId, UUID tenantId);
    
    /**
     * Create delivery with provider (third-party or own fleet)
     */
    DeliveryResponse createDeliveryWithProvider(
        UUID fulfillmentId, 
        UUID tenantId, 
        String providerCode,
        boolean isIntercity
    );
    
    /**
     * Sync tracking from provider
     */
    DeliveryResponse syncTrackingFromProvider(UUID deliveryId, UUID tenantId);
}

