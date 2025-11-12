package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.entity.Fulfillment;
import com.nexus.ecommerce.fulfilment.model.request.AssignDriverRequest;
import com.nexus.ecommerce.fulfilment.model.request.CreateFulfillmentRequest;
import com.nexus.ecommerce.fulfilment.model.request.UpdateFulfillmentStatusRequest;
import com.nexus.ecommerce.fulfilment.model.response.FulfillmentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Fulfillment Service Interface
 */
public interface FulfillmentService {
    
    /**
     * Create fulfillment for an order
     */
    FulfillmentResponse createFulfillment(UUID tenantId, CreateFulfillmentRequest request);
    
    /**
     * Get fulfillment by ID
     */
    FulfillmentResponse getFulfillmentById(UUID fulfillmentId, UUID tenantId, List<String> userRoles);
    
    /**
     * Get fulfillment by order ID
     */
    FulfillmentResponse getFulfillmentByOrderId(UUID orderId, UUID tenantId, List<String> userRoles);
    
    /**
     * Assign driver to fulfillment (for own fleet)
     */
    FulfillmentResponse assignDriver(UUID fulfillmentId, UUID tenantId, List<String> userRoles, AssignDriverRequest request);
    
    /**
     * Assign provider to fulfillment (for third-party providers)
     */
    FulfillmentResponse assignProvider(
        UUID fulfillmentId, 
        UUID tenantId, 
        List<String> userRoles, 
        String providerCode,
        boolean isIntercity
    );
    
    /**
     * Update fulfillment status
     */
    FulfillmentResponse updateStatus(UUID fulfillmentId, UUID tenantId, List<String> userRoles, UpdateFulfillmentStatusRequest request);
    
    /**
     * Auto-create fulfillment from OrderCreatedEvent
     */
    void createFulfillmentFromOrder(UUID orderId, UUID tenantId, UUID deliveryAddressId);
    
    /**
     * Check if user can access fulfillment
     */
    boolean canAccessFulfillment(UUID currentUserId, UUID fulfillmentUserId, List<String> userRoles);
}

