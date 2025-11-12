package com.nexus.ecommerce.cart.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for shopping cart
 */
public record CartResponse(
    /**
     * User ID
     */
    @JsonProperty("user_id")
    UUID userId,
    
    /**
     * Tenant ID
     */
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    /**
     * Cart items
     */
    List<CartItemResponse> items,
    
    /**
     * Applied coupon code (if any)
     */
    @JsonProperty("coupon_code")
    String couponCode,
    
    /**
     * Subtotal
     */
    BigDecimal subtotal,
    
    /**
     * Total discount amount
     */
    @JsonProperty("discount_amount")
    BigDecimal discountAmount,
    
    /**
     * Tax amount
     */
    @JsonProperty("tax_amount")
    BigDecimal taxAmount,
    
    /**
     * Shipping cost
     */
    @JsonProperty("shipping_cost")
    BigDecimal shippingCost,
    
    /**
     * Final total
     */
    BigDecimal total,
    
    /**
     * Currency code
     */
    String currency,
    
    /**
     * Created timestamp
     */
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    
    /**
     * Last updated timestamp
     */
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {
}

