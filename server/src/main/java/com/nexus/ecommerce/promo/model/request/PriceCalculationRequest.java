package com.nexus.ecommerce.promo.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for price calculation
 */
public record PriceCalculationRequest(
    @NotNull(message = "Product ID is required")
    @JsonProperty("product_id")
    UUID productId,
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,
    
    @JsonProperty("coupon_code")
    String couponCode
) {
}

