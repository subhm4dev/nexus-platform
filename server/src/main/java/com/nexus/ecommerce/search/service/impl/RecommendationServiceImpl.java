package com.nexus.ecommerce.search.service.impl;

import com.nexus.ecommerce.search.entity.Recommendation;
import com.nexus.ecommerce.search.model.response.RecommendationResponse;
import com.nexus.ecommerce.search.repository.RecommendationRepository;
import com.nexus.ecommerce.search.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Recommendation Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {
    
    private final RecommendationRepository recommendationRepository;
    
    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse getRecommendations(UUID userId, UUID tenantId, int limit) {
        log.debug("Getting recommendations: userId={}, tenantId={}, limit={}", userId, tenantId, limit);
        
        List<Recommendation> recommendations = recommendationRepository
            .findByUserIdAndTenantId(userId, tenantId, PageRequest.of(0, limit))
            .getContent();
        
        List<RecommendationResponse.RecommendedProduct> products = recommendations.stream()
            .map(r -> new RecommendationResponse.RecommendedProduct(
                r.getProductId(),
                null,  // TODO: Fetch product name from Catalog service
                null,  // TODO: Fetch SKU from Catalog service
                r.getScore(),
                "Based on your preferences"
            ))
            .collect(Collectors.toList());
        
        return new RecommendationResponse(
            products,
            "PERSONALIZED",
            products.size()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public RecommendationResponse getTrendingProducts(UUID tenantId, int limit) {
        log.debug("Getting trending products: tenantId={}, limit={}", tenantId, limit);
        
        List<Recommendation> recommendations = recommendationRepository
            .findTrendingByTenantId(tenantId, "TRENDING", PageRequest.of(0, limit))
            .getContent();
        
        List<RecommendationResponse.RecommendedProduct> products = recommendations.stream()
            .map(r -> new RecommendationResponse.RecommendedProduct(
                r.getProductId(),
                null,  // TODO: Fetch product name from Catalog service
                null,  // TODO: Fetch SKU from Catalog service
                r.getScore(),
                "Trending now"
            ))
            .collect(Collectors.toList());
        
        return new RecommendationResponse(
            products,
            "TRENDING",
            products.size()
        );
    }
}

