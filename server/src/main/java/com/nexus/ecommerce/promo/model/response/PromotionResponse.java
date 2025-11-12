package com.nexus.ecommerce.promo.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for promotion
 */
public record PromotionResponse(
    @JsonProperty("promotion_id")
    UUID promotionId,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    String name,
    
    String type,
    
    @JsonProperty("discount_type")
    String discountType,
    
    @JsonProperty("discount_value")
    BigDecimal discountValue,
    
    @JsonProperty("start_date")
    LocalDateTime startDate,
    
    @JsonProperty("end_date")
    LocalDateTime endDate,
    
    @JsonProperty("eligibility_criteria")
    String eligibilityCriteria,
    
    Integer priority,
    
    Boolean active,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {
}

