package com.nexus.ecommerce.order.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order Cancelled Event (Published to Kafka)
 */
public record OrderCancelledEvent(
    @JsonProperty("order_id")
    UUID orderId,
    
    @JsonProperty("order_number")
    String orderNumber,
    
    @JsonProperty("user_id")
    UUID userId,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    @JsonProperty("payment_id")
    UUID paymentId,
    
    String reason,
    
    @JsonProperty("cancelled_at")
    LocalDateTime cancelledAt
) {
    public static OrderCancelledEvent of(
        UUID orderId,
        String orderNumber,
        UUID userId,
        UUID tenantId,
        UUID paymentId,
        String reason,
        LocalDateTime cancelledAt
    ) {
        return new OrderCancelledEvent(
            orderId,
            orderNumber,
            userId,
            tenantId,
            paymentId,
            reason,
            cancelledAt
        );
    }
}

