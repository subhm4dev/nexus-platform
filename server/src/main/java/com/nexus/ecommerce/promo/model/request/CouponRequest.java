package com.nexus.ecommerce.promo.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a coupon
 */
public record CouponRequest(
    String code, // Optional - will be auto-generated if not provided
    
    @NotBlank(message = "Discount type is required")
    @JsonProperty("discount_type")
    String discountType, // PERCENTAGE, FIXED
    
    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", message = "Discount value must be non-negative")
    @JsonProperty("discount_value")
    BigDecimal discountValue,
    
    @JsonProperty("usage_limit")
    Integer usageLimit, // NULL means unlimited
    
    @NotNull(message = "Expiry date is required")
    @JsonProperty("expiry_date")
    LocalDateTime expiryDate,
    
    @JsonProperty("min_order_value")
    @DecimalMin(value = "0.0", message = "Minimum order value must be non-negative")
    BigDecimal minOrderValue
) {
}

