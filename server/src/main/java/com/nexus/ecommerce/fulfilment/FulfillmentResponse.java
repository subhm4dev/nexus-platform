package com.nexus.ecommerce.fulfilment.model.response;

import com.nexus.ecommerce.fulfilment.entity.Fulfillment;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for fulfillment details
 */
public record FulfillmentResponse(
    UUID id,
    
    @JsonProperty("order_id")
    UUID orderId,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    Fulfillment.FulfillmentStatus status,
    
    @JsonProperty("assigned_driver_id")
    UUID assignedDriverId,
    
    @JsonProperty("pickup_location")
    String pickupLocation,
    
    @JsonProperty("delivery_address_id")
    UUID deliveryAddressId,
    
    @JsonProperty("estimated_delivery")
    LocalDateTime estimatedDelivery,
    
    @JsonProperty("actual_delivery")
    LocalDateTime actualDelivery,
    
    List<DeliveryResponse> deliveries,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {}

