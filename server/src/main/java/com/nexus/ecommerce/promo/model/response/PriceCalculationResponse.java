package com.nexus.ecommerce.promo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for price calculation
 */
public record PriceCalculationResponse(
    @JsonProperty("base_price")
    BigDecimal basePrice,
    
    @JsonProperty("discount_amount")
    BigDecimal discountAmount,
    
    @JsonProperty("final_price")
    BigDecimal finalPrice,
    
    @JsonProperty("applied_promotions")
    List<String> appliedPromotions, // List of promotion names
    
    String currency
) {
}

