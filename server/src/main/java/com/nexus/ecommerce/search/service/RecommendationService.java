package com.nexus.ecommerce.search.service;

import com.nexus.ecommerce.search.model.response.RecommendationResponse;

import java.util.UUID;

/**
 * Recommendation Service Interface
 */
public interface RecommendationService {
    
    /**
     * Get personalized recommendations for user
     */
    RecommendationResponse getRecommendations(UUID userId, UUID tenantId, int limit);
    
    /**
     * Get trending products
     */
    RecommendationResponse getTrendingProducts(UUID tenantId, int limit);
}

