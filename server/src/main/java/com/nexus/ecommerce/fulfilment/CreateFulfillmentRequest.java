package com.nexus.ecommerce.fulfilment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for creating a fulfillment
 */
public record CreateFulfillmentRequest(
    @NotNull(message = "Order ID is required")
    @JsonProperty("order_id")
    UUID orderId,
    
    @JsonProperty("pickup_location")
    String pickupLocation,
    
    @NotNull(message = "Delivery address ID is required")
    @JsonProperty("delivery_address_id")
    UUID deliveryAddressId,
    
    @JsonProperty("estimated_delivery")
    LocalDateTime estimatedDelivery
) {}

