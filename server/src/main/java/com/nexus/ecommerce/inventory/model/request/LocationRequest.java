package com.nexus.ecommerce.inventory.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a location
 */
public record LocationRequest(
    /**
     * Location name (e.g., "Main Warehouse", "Store #1")
     * Required field
     */
    @NotBlank(message = "Location name is required")
    @Size(max = 255, message = "Location name must not exceed 255 characters")
    String name,

    /**
     * Location type (e.g., "WAREHOUSE", "STORE", "DISTRIBUTION_CENTER")
     * Required field
     */
    @NotBlank(message = "Location type is required")
    @Size(max = 50, message = "Location type must not exceed 50 characters")
    String type,

    /**
     * Location address (text description, optional)
     * Note: This is just a text field, not linked to Address-Book service
     */
    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address
) {
}

