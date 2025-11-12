package com.nexus.ecommerce.inventory.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record StockAdjustmentRequest(
    @NotNull(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    String sku,
    
    @NotNull(message = "Location ID is required")
    @JsonProperty("location_id")
    UUID locationId,
    
    @NotNull(message = "Delta is required")
    Integer delta,
    
    @NotNull(message = "Reason is required")
    @Size(max = 50, message = "Reason must not exceed 50 characters")
    String reason,
    
    @JsonProperty("order_id")
    UUID orderId
) {
}

