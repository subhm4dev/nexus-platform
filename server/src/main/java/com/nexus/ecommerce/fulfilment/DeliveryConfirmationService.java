package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.model.request.ConfirmDeliveryRequest;
import com.nexus.ecommerce.fulfilment.model.response.DeliveryConfirmationResponse;

import java.util.UUID;

/**
 * Service for dual-confirmation delivery system
 */
public interface DeliveryConfirmationService {
    
    /**
     * Agent confirms delivery (requires proximity)
     */
    DeliveryConfirmationResponse agentConfirmDelivery(
        UUID deliveryId,
        UUID agentUserId,
        UUID tenantId,
        ConfirmDeliveryRequest request
    );
    
    /**
     * Customer confirms delivery (requires proximity)
     */
    DeliveryConfirmationResponse customerConfirmDelivery(
        UUID deliveryId,
        UUID customerUserId,
        UUID tenantId,
        ConfirmDeliveryRequest request
    );
    
    /**
     * Agent marks customer as unavailable
     */
    DeliveryConfirmationResponse agentMarkUnavailable(
        UUID deliveryId,
        UUID agentUserId,
        UUID tenantId,
        ConfirmDeliveryRequest request,
        String reason
    );
    
    /**
     * Customer marks agent as unavailable
     */
    DeliveryConfirmationResponse customerMarkUnavailable(
        UUID deliveryId,
        UUID customerUserId,
        UUID tenantId,
        ConfirmDeliveryRequest request,
        String reason
    );
    
    /**
     * Get confirmation status (for agent)
     */
    DeliveryConfirmationResponse getConfirmationStatus(
        UUID deliveryId,
        UUID userId,
        UUID tenantId,
        boolean isAgent
    );
    
    /**
     * Process reschedules and auto-returns
     * This should be called by a scheduled job
     */
    void processReschedules(UUID tenantId);
    
    /**
     * Process auto-returns (after 3 reschedules)
     * This should be called by a scheduled job
     */
    void processAutoReturns(UUID tenantId);
}

