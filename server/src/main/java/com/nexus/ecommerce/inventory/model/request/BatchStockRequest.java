package com.nexus.ecommerce.inventory.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record BatchStockRequest(
    @NotEmpty(message = "Items are required")
    @Valid
    List<BatchStockItem> items
) {
    public record BatchStockItem(
        @JsonProperty("sku")
        String sku,
        
        @JsonProperty("location_id")
        UUID locationId
    ) {
    }
}

