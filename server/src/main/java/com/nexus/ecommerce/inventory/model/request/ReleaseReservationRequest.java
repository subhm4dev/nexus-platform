package com.nexus.ecommerce.inventory.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReleaseReservationRequest(
    @NotNull(message = "Order ID is required")
    @JsonProperty("order_id")
    UUID orderId
) {
}

