package com.nexus.ecommerce.catalog.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka event published when a product is created
 * 
 * <p>This event is consumed by Inventory service to auto-initialize stock records
 * and by Search service to index products.
 */
public record ProductCreatedEvent(
    /**
     * Event type identifier
     */
    @JsonProperty("event_type")
    String eventType,

    /**
     * Product ID
     */
    @JsonProperty("product_id")
    UUID productId,

    /**
     * Product SKU
     */
    @JsonProperty("sku")
    String sku,

    /**
     * Tenant ID
     */
    @JsonProperty("tenant_id")
    UUID tenantId,

    /**
     * Seller ID (product owner)
     */
    @JsonProperty("seller_id")
    UUID sellerId,

    /**
     * Event timestamp
     */
    @JsonProperty("timestamp")
    LocalDateTime timestamp
) {
    /**
     * Factory method to create ProductCreatedEvent
     */
    public static ProductCreatedEvent of(UUID productId, String sku, UUID tenantId, UUID sellerId) {
        return new ProductCreatedEvent(
            "ProductCreated",
            productId,
            sku,
            tenantId,
            sellerId,
            LocalDateTime.now()
        );
    }
}

