package com.nexus.ecommerce.promo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for coupon
 */
public record CouponResponse(
    @JsonProperty("coupon_id")
    UUID couponId,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    String code,
    
    @JsonProperty("discount_type")
    String discountType,
    
    @JsonProperty("discount_value")
    BigDecimal discountValue,
    
    @JsonProperty("usage_limit")
    Integer usageLimit,
    
    @JsonProperty("used_count")
    Integer usedCount,
    
    @JsonProperty("expiry_date")
    LocalDateTime expiryDate,
    
    @JsonProperty("min_order_value")
    BigDecimal minOrderValue,
    
    Boolean active,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {
}

