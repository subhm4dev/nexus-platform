package com.nexus.ecommerce.promo.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating a promotion
 */
public record PromotionRequest(
    @NotBlank(message = "Name is required")
    String name,
    
    @NotBlank(message = "Type is required")
    String type, // PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y
    
    @NotBlank(message = "Discount type is required")
    @JsonProperty("discount_type")
    String discountType, // PERCENTAGE, FIXED
    
    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.0", message = "Discount value must be non-negative")
    @JsonProperty("discount_value")
    BigDecimal discountValue,
    
    @NotNull(message = "Start date is required")
    @JsonProperty("start_date")
    LocalDateTime startDate,
    
    @NotNull(message = "End date is required")
    @JsonProperty("end_date")
    LocalDateTime endDate,
    
    @JsonProperty("eligibility_criteria")
    String eligibilityCriteria, // JSON string
    
    Integer priority
) {
}

