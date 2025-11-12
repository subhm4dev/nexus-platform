package com.nexus.ecommerce.inventory.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductCreatedEvent(
    @JsonProperty("event_type") String eventType,
    @JsonProperty("product_id") UUID productId,
    @JsonProperty("sku") String sku,
    @JsonProperty("tenant_id") UUID tenantId,
    @JsonProperty("seller_id") UUID sellerId,
    @JsonProperty("timestamp") LocalDateTime timestamp
) {
}

