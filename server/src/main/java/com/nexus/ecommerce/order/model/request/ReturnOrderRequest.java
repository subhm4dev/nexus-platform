package com.nexus.ecommerce.order.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for requesting order return
 */
public record ReturnOrderRequest(
    @NotEmpty(message = "At least one item ID is required")
    @JsonProperty("item_ids")
    List<UUID> itemIds,
    
    @NotNull(message = "Reason is required")
    String reason
) {}

