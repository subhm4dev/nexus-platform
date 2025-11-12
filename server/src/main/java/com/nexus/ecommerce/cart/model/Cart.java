package com.nexus.ecommerce.cart.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cart Entity (Redis-serializable)
 * 
 * <p>Represents a shopping cart stored in Redis.
 * Cart key pattern: cart:{tenantId}:{userId}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    
    /**
     * User ID (cart owner)
     */
    @JsonProperty("user_id")
    private UUID userId;
    
    /**
     * Tenant ID
     */
    @JsonProperty("tenant_id")
    private UUID tenantId;
    
    /**
     * Cart items
     */
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    
    /**
     * Applied coupon code (if any)
     */
    @JsonProperty("coupon_code")
    private String couponCode;
    
    /**
     * Subtotal (sum of all item prices * quantities)
     */
    private BigDecimal subtotal;
    
    /**
     * Total discount amount (from promotions + coupon)
     */
    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;
    
    /**
     * Tax amount (if applicable)
     */
    @JsonProperty("tax_amount")
    private BigDecimal taxAmount;
    
    /**
     * Shipping cost (calculated during checkout)
     */
    @JsonProperty("shipping_cost")
    private BigDecimal shippingCost;
    
    /**
     * Final total (subtotal - discount + tax + shipping)
     */
    private BigDecimal total;
    
    /**
     * Currency code
     */
    private String currency;
    
    /**
     * Cart creation timestamp
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    /**
     * Last updated timestamp
     */
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Cart Item nested class
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        
        /**
         * Unique item ID (generated when added to cart)
         */
        @JsonProperty("item_id")
        private String itemId;
        
        /**
         * Product ID
         */
        @JsonProperty("product_id")
        private UUID productId;
        
        /**
         * Product SKU
         */
        private String sku;
        
        /**
         * Product name (cached from catalog)
         */
        private String name;
        
        /**
         * Product image URL (cached from catalog)
         */
        private String imageUrl;
        
        /**
         * Quantity
         */
        private Integer quantity;
        
        /**
         * Unit price (with promotions applied)
         */
        @JsonProperty("unit_price")
        private BigDecimal unitPrice;
        
        /**
         * Total price for this item (unitPrice * quantity)
         */
        @JsonProperty("total_price")
        private BigDecimal totalPrice;
        
        /**
         * Variant ID (if product has variants)
         */
        @JsonProperty("variant_id")
        private UUID variantId;
        
        /**
         * Currency code
         */
        private String currency;
    }
}

