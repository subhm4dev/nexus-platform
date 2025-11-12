package com.nexus.ecommerce.inventory.model.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for location
 */
public record LocationResponse(
    /**
     * Location ID
     */
    UUID id,

    /**
     * Location name
     */
    String name,

    /**
     * Location type (WAREHOUSE, STORE, etc.)
     */
    String type,

    /**
     * Location address (text description)
     */
    String address,

    /**
     * Tenant ID for multi-tenant isolation
     */
    UUID tenantId,

    /**
     * Whether location is active
     */
    Boolean active,

    /**
     * Location creation timestamp
     */
    LocalDateTime createdAt,

    /**
     * Location last update timestamp
     */
    LocalDateTime updatedAt
) {
}

