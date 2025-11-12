package com.nexus.ecommerce.catalog.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for product search results with pagination
 */
public record ProductSearchResponse(
    /**
     * List of products
     */
    List<ProductResponse> products,

    /**
     * Current page number (0-based)
     */
    @JsonProperty("page")
    int page,

    /**
     * Page size
     */
    @JsonProperty("size")
    int size,

    /**
     * Total number of elements across all pages
     */
    @JsonProperty("total_elements")
    long totalElements,

    /**
     * Total number of pages
     */
    @JsonProperty("total_pages")
    int totalPages,

    /**
     * Whether this is the first page
     */
    @JsonProperty("is_first")
    boolean isFirst,

    /**
     * Whether this is the last page
     */
    @JsonProperty("is_last")
    boolean isLast
) {
}

