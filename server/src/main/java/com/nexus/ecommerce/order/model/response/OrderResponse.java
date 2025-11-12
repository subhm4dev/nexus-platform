package com.nexus.ecommerce.order.model.response;

import com.nexus.ecommerce.order.entity.Order;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for order details
 */
public record OrderResponse(
    UUID id,
    
    @JsonProperty("order_number")
    String orderNumber,
    
    @JsonProperty("user_id")
    UUID userId,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    Order.OrderStatus status,
    
    @JsonProperty("shipping_address_id")
    UUID shippingAddressId,
    
    @JsonProperty("payment_id")
    UUID paymentId,
    
    List<OrderItemResponse> items,
    
    BigDecimal subtotal,
    
    @JsonProperty("discount_amount")
    BigDecimal discountAmount,
    
    @JsonProperty("tax_amount")
    BigDecimal taxAmount,
    
    @JsonProperty("shipping_cost")
    BigDecimal shippingCost,
    
    BigDecimal total,
    
    String currency,
    
    String notes,
    
    @JsonProperty("status_history")
    List<OrderStatusHistoryResponse> statusHistory,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {
    public record OrderItemResponse(
        UUID id,
        
        @JsonProperty("product_id")
        UUID productId,
        
        String sku,
        
        @JsonProperty("product_name")
        String productName,
        
        Integer quantity,
        
        @JsonProperty("unit_price")
        BigDecimal unitPrice,
        
        @JsonProperty("total_price")
        BigDecimal totalPrice,
        
        String currency
    ) {}
    
    public record OrderStatusHistoryResponse(
        UUID id,
        
        Order.OrderStatus status,
        
        @JsonProperty("previous_status")
        String previousStatus,
        
        String reason,
        
        @JsonProperty("changed_by")
        UUID changedBy,
        
        @JsonProperty("created_at")
        LocalDateTime createdAt
    ) {}
}

