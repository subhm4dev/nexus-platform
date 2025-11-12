package com.nexus.ecommerce.cart.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for applying coupon
 */
public record CouponRequest(
    /**
     * Coupon code
     */
    @NotBlank(message = "Coupon code is required")
    @JsonProperty("coupon_code")
    String couponCode
) {
}

