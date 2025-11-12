package com.nexus.ecommerce.cart.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for adding item to cart
 */
public record AddItemRequest(
    /**
     * Product ID
     */
    @NotNull(message = "Product ID is required")
    @JsonProperty("product_id")
    UUID productId,
    
    /**
     * Quantity to add
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,
    
    /**
     * Variant ID (optional, for products with variants)
     */
    @JsonProperty("variant_id")
    UUID variantId
) {
}

