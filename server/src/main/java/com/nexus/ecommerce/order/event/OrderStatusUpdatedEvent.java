package com.nexus.ecommerce.order.event;

import com.nexus.ecommerce.order.entity.Order;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order Status Updated Event (Published to Kafka)
 */
public record OrderStatusUpdatedEvent(
    @JsonProperty("order_id")
    UUID orderId,
    
    @JsonProperty("order_number")
    String orderNumber,
    
    @JsonProperty("user_id")
    UUID userId,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    Order.OrderStatus status,
    
    @JsonProperty("previous_status")
    String previousStatus,
    
    String reason,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {
    public static OrderStatusUpdatedEvent of(
        UUID orderId,
        String orderNumber,
        UUID userId,
        UUID tenantId,
        Order.OrderStatus status,
        String previousStatus,
        String reason,
        LocalDateTime updatedAt
    ) {
        return new OrderStatusUpdatedEvent(
            orderId,
            orderNumber,
            userId,
            tenantId,
            status,
            previousStatus,
            reason,
            updatedAt
        );
    }
}

