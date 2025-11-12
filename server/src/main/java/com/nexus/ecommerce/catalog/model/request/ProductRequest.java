package com.nexus.ecommerce.catalog.model.request;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating or updating a product
 */
public record ProductRequest(
    /**
     * Product name
     */
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    String name,

    /**
     * Stock Keeping Unit - must be unique within tenant
     */
    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    String sku,

    /**
     * Product description
     */
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description,

    /**
     * Product price
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Price must have at most 15 integer digits and 2 decimal places")
    BigDecimal price,

    /**
     * Currency code (ISO 4217, e.g., "USD", "EUR")
     */
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-character ISO 4217 code")
    String currency,

    /**
     * Category ID (optional)
     */
    @JsonProperty("category_id")
    UUID categoryId,

    /**
     * Product images (list of image URLs)
     */
    List<@NotBlank(message = "Image URL cannot be blank") String> images,

    /**
     * Product status (ACTIVE, INACTIVE, DRAFT)
     */
    @Pattern(regexp = "ACTIVE|INACTIVE|DRAFT", message = "Status must be ACTIVE, INACTIVE, or DRAFT")
    String status
) {
    /**
     * Default status if not provided
     */
    public String status() {
        return status != null ? status : "ACTIVE";
    }
}

