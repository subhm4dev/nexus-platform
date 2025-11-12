package com.nexus.ecommerce.catalog.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for product information
 */
public record ProductResponse(
    /**
     * Product ID
     */
    @JsonProperty("product_id")
    UUID productId,

    /**
     * Product name
     */
    String name,

    /**
     * Stock Keeping Unit
     */
    String sku,

    /**
     * Product description
     */
    String description,

    /**
     * Product price
     */
    BigDecimal price,

    /**
     * Currency code
     */
    String currency,

    /**
     * Category ID
     */
    @JsonProperty("category_id")
    UUID categoryId,

    /**
     * Seller ID (product owner)
     */
    @JsonProperty("seller_id")
    UUID sellerId,

    /**
     * Product images (list of image URLs)
     */
    List<String> images,

    /**
     * Product status
     */
    String status,

    /**
     * Created timestamp
     */
    @JsonProperty("created_at")
    LocalDateTime createdAt,

    /**
     * Last updated timestamp
     */
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {
    /**
     * Parse images JSON string to List<String>
     */
    public static List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // If parsing fails, return empty list
            return new ArrayList<>();
        }
    }
}

