package com.nexus.ecommerce.fulfilment.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Delivery Completed Event (Published to Kafka)
 */
public record DeliveryCompletedEvent(
    @JsonProperty("delivery_id")
    UUID deliveryId,
    
    @JsonProperty("fulfillment_id")
    UUID fulfillmentId,
    
    @JsonProperty("driver_id")
    UUID driverId,
    
    @JsonProperty("order_id")
    UUID orderId,
    
    @JsonProperty("completed_at")
    LocalDateTime completedAt
) {
    public static DeliveryCompletedEvent of(
        UUID deliveryId,
        UUID fulfillmentId,
        UUID driverId,
        UUID orderId,
        LocalDateTime completedAt
    ) {
        return new DeliveryCompletedEvent(
            deliveryId,
            fulfillmentId,
            driverId,
            orderId,
            completedAt
        );
    }
}

