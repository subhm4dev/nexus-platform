package com.nexus.ecommerce.catalog.model.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for category tree (hierarchical structure)
 */
public record CategoryTreeResponse(
    /**
     * Category ID
     */
    UUID id,

    /**
     * Category name
     */
    String name,

    /**
     * Category description
     */
    String description,

    /**
     * Parent category ID (null if top-level)
     */
    UUID parentId,

    /**
     * Tenant ID for multi-tenant isolation
     */
    UUID tenantId,

    /**
     * List of child categories (recursive structure)
     */
    List<CategoryTreeResponse> children,

    /**
     * Category creation timestamp
     */
    LocalDateTime createdAt,

    /**
     * Category last update timestamp
     */
    LocalDateTime updatedAt
) {
}

