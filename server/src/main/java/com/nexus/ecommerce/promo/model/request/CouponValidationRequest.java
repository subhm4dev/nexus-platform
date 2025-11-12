package com.nexus.ecommerce.promo.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for coupon validation
 */
public record CouponValidationRequest(
    @NotBlank(message = "Coupon code is required")
    @JsonProperty("coupon_code")
    String couponCode,
    
    @JsonProperty("order_total")
    BigDecimal orderTotal,
    
    @JsonProperty("item_ids")
    List<UUID> itemIds
) {
}

