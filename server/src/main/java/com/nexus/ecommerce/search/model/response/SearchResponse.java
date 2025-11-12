package com.nexus.ecommerce.search.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for search results
 */
public record SearchResponse(
    List<ProductSearchResult> products,
    
    List<CategorySearchResult> categories,
    
    List<CategorySearchResult> subcategories,
    
    @JsonProperty("total_results")
    Long totalResults,
    
    @JsonProperty("current_page")
    int currentPage,
    
    @JsonProperty("page_size")
    int pageSize,
    
    @JsonProperty("total_pages")
    int totalPages
) {
    public record ProductSearchResult(
        @JsonProperty("product_id")
        UUID productId,
        
        String name,
        
        String description,
        
        String sku,
        
        @JsonProperty("category_id")
        UUID categoryId,
        
        @JsonProperty("subcategory_id")
        UUID subcategoryId,
        
        Double relevance
    ) {}
    
    public record CategorySearchResult(
        @JsonProperty("category_id")
        UUID categoryId,
        
        String name,
        
        String description,
        
        @JsonProperty("parent_id")
        UUID parentId
    ) {}
}

