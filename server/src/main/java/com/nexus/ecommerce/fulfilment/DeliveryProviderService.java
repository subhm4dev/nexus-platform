package com.nexus.ecommerce.fulfilment.provider;

import com.nexus.ecommerce.fulfilment.provider.dto.CreateShipmentRequest;
import com.nexus.ecommerce.fulfilment.provider.dto.CreateShipmentResponse;
import com.nexus.ecommerce.fulfilment.provider.dto.TrackingResponse;
import com.nexus.ecommerce.fulfilment.provider.dto.CancelShipmentResponse;

import java.util.UUID;

/**
 * Delivery Provider Service Interface (Adapter Pattern)
 * 
 * <p>Abstract interface for delivery provider integration. Allows switching
 * between different providers (BlueDart, Delhivery, Shiprocket, Dunzo, Rapido, OWN_FLEET)
 * without changing the service layer code.
 * 
 * <p>Similar to PaymentGateway pattern used in Payment service.
 */
public interface DeliveryProviderService {
    
    /**
     * Create a shipment with the provider
     * 
     * @param request Shipment creation request with pickup/delivery details
     * @return Shipment response with tracking ID and status
     */
    CreateShipmentResponse createShipment(CreateShipmentRequest request);
    
    /**
     * Get tracking information from provider
     * 
     * @param providerTrackingId Provider's tracking ID
     * @return Tracking response with current status and location
     */
    TrackingResponse getTracking(String providerTrackingId);
    
    /**
     * Cancel a shipment
     * 
     * @param providerTrackingId Provider's tracking ID
     * @return Cancel response with status
     */
    CancelShipmentResponse cancelShipment(String providerTrackingId);
    
    /**
     * Verify webhook signature from provider
     * 
     * @param payload Webhook payload
     * @param signature Webhook signature
     * @return true if signature is valid
     */
    boolean verifyWebhookSignature(String payload, String signature);
    
    /**
     * Get provider code
     * 
     * @return Provider code (e.g., "BLUEDART", "DELHIVERY", "OWN_FLEET")
     */
    String getProviderCode();
    
    /**
     * Check if provider supports the delivery type
     * 
     * @param isIntercity true for intercity, false for intracity
     * @return true if provider supports this delivery type
     */
    boolean supportsDeliveryType(boolean isIntercity);
}

