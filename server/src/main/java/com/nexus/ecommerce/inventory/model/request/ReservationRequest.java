package com.nexus.ecommerce.inventory.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReservationRequest(
    @NotNull(message = "Order ID is required")
    @JsonProperty("order_id")
    UUID orderId,
    
    @NotEmpty(message = "Items are required")
    @Valid
    List<ReservationItem> items
) {
    public record ReservationItem(
        @NotNull(message = "SKU is required")
        String sku,
        
        @NotNull(message = "Location ID is required")
        @JsonProperty("location_id")
        UUID locationId,
        
        @NotNull(message = "Quantity is required")
        @JsonProperty("quantity")
        Integer quantity
    ) {
    }
}

