package com.nexus.ecommerce.order.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Order Created Event (Published to Kafka)
 */
public record OrderCreatedEvent(
    @JsonProperty("order_id")
    UUID orderId,
    
    @JsonProperty("order_number")
    String orderNumber,
    
    @JsonProperty("user_id")
    UUID userId,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    @JsonProperty("shipping_address_id")
    UUID shippingAddressId,
    
    @JsonProperty("payment_id")
    UUID paymentId,
    
    List<OrderItemEvent> items,
    
    BigDecimal total,
    
    String currency,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt
) {
    public static OrderCreatedEvent of(
        UUID orderId,
        String orderNumber,
        UUID userId,
        UUID tenantId,
        UUID shippingAddressId,
        UUID paymentId,
        List<OrderItemEvent> items,
        BigDecimal total,
        String currency,
        LocalDateTime createdAt
    ) {
        return new OrderCreatedEvent(
            orderId,
            orderNumber,
            userId,
            tenantId,
            shippingAddressId,
            paymentId,
            items,
            total,
            currency,
            createdAt
        );
    }
    
    public record OrderItemEvent(
        @JsonProperty("product_id")
        UUID productId,
        
        String sku,
        
        Integer quantity,
        
        @JsonProperty("unit_price")
        BigDecimal unitPrice
    ) {}
}

