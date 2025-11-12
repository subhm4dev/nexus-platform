package com.nexus.ecommerce.cart.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for cart item
 */
public record CartItemResponse(
    /**
     * Item ID
     */
    @JsonProperty("item_id")
    String itemId,
    
    /**
     * Product ID
     */
    @JsonProperty("product_id")
    UUID productId,
    
    /**
     * Product SKU
     */
    String sku,
    
    /**
     * Product name
     */
    String name,
    
    /**
     * Product image URL
     */
    @JsonProperty("image_url")
    String imageUrl,
    
    /**
     * Quantity
     */
    Integer quantity,
    
    /**
     * Unit price
     */
    @JsonProperty("unit_price")
    BigDecimal unitPrice,
    
    /**
     * Total price for this item
     */
    @JsonProperty("total_price")
    BigDecimal totalPrice,
    
    /**
     * Variant ID (if applicable)
     */
    @JsonProperty("variant_id")
    UUID variantId,
    
    /**
     * Currency code
     */
    String currency
) {
}

