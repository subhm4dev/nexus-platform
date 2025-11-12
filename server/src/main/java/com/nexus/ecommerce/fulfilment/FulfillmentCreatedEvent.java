package com.nexus.ecommerce.fulfilment.event;

import com.nexus.ecommerce.fulfilment.entity.Fulfillment;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fulfillment Created Event (Published to Kafka)
 */
public record FulfillmentCreatedEvent(
    @JsonProperty("fulfillment_id")
    UUID fulfillmentId,
    
    @JsonProperty("order_id")
    UUID orderId,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    Fulfillment.FulfillmentStatus status,
    
    @JsonProperty("delivery_address_id")
    UUID deliveryAddressId,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt
) {
    public static FulfillmentCreatedEvent of(
        UUID fulfillmentId,
        UUID orderId,
        UUID tenantId,
        Fulfillment.FulfillmentStatus status,
        UUID deliveryAddressId,
        LocalDateTime createdAt
    ) {
        return new FulfillmentCreatedEvent(
            fulfillmentId,
            orderId,
            tenantId,
            status,
            deliveryAddressId,
            createdAt
        );
    }
}

