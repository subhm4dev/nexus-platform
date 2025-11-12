package com.nexus.ecommerce.search.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for recommendations
 */
public record RecommendationResponse(
    List<RecommendedProduct> products,
    
    String type,
    
    @JsonProperty("total_count")
    int totalCount
) {
    public record RecommendedProduct(
        @JsonProperty("product_id")
        UUID productId,
        
        String name,
        
        String sku,
        
        BigDecimal score,
        
        String reason
    ) {}
}

