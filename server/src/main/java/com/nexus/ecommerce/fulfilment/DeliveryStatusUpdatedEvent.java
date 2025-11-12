package com.nexus.ecommerce.fulfilment.event;

import com.nexus.ecommerce.fulfilment.entity.Delivery;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Delivery Status Updated Event (Published to Kafka)
 */
public record DeliveryStatusUpdatedEvent(
    @JsonProperty("delivery_id")
    UUID deliveryId,
    
    @JsonProperty("fulfillment_id")
    UUID fulfillmentId,
    
    @JsonProperty("driver_id")
    UUID driverId,
    
    Delivery.DeliveryStatus status,
    
    BigDecimal latitude,
    
    BigDecimal longitude,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {
    public static DeliveryStatusUpdatedEvent of(
        UUID deliveryId,
        UUID fulfillmentId,
        UUID driverId,
        Delivery.DeliveryStatus status,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime updatedAt
    ) {
        return new DeliveryStatusUpdatedEvent(
            deliveryId,
            fulfillmentId,
            driverId,
            status,
            latitude,
            longitude,
            updatedAt
        );
    }
}

