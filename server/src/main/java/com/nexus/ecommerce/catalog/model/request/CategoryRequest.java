package com.nexus.ecommerce.catalog.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Request DTO for creating or updating a category
 */
public record CategoryRequest(
    /**
     * Category name
     * Required field
     */
    @NotBlank(message = "Category name is required")
    @Size(max = 255, message = "Category name must not exceed 255 characters")
    String name,

    /**
     * Category description
     * Optional field
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    /**
     * Parent category ID (for hierarchical categories)
     * Null if this is a top-level category
     * Optional field
     */
    UUID parentId
) {
}

