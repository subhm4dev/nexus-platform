package com.nexus.ecommerce.search.consumer;

import com.nexus.ecommerce.search.entity.Recommendation;
import com.nexus.ecommerce.search.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consumer for OrderCreatedEvent
 * Updates trending products based on order data
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventConsumer {
    
    private final RecommendationRepository recommendationRepository;
    
    @KafkaListener(topics = "order-created", groupId = "search-service-group")
    @Transactional
    public void handleOrderCreated(Map<String, Object> eventMap) {
        try {
            log.info("Received OrderCreated event: {}", eventMap);
            
            UUID tenantId = UUID.fromString(eventMap.get("tenant_id").toString());
            
            // Extract order items from event
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) eventMap.get("items");
            
            if (items != null) {
                for (Map<String, Object> item : items) {
                    UUID productId = UUID.fromString(item.get("product_id").toString());
                    
                    // Update trending score for this product
                    updateTrendingScore(productId, tenantId);
                }
            }
            
        } catch (Exception e) {
            log.error("Error processing OrderCreated event: {}", eventMap, e);
        }
    }
    
    private void updateTrendingScore(UUID productId, UUID tenantId) {
        // Find existing recommendation or create new
        List<Recommendation> existing = recommendationRepository.findByProductIdAndType(
            productId, tenantId, "TRENDING"
        );
        
        Recommendation recommendation;
        if (!existing.isEmpty()) {
            recommendation = existing.get(0);
            // Increase score by 0.1 for each order
            recommendation.setScore(recommendation.getScore().add(new BigDecimal("0.1")));
            recommendation.setUpdatedAt(LocalDateTime.now());
        } else {
            recommendation = Recommendation.builder()
                .productId(productId)
                .tenantId(tenantId)
                .type("TRENDING")
                .score(new BigDecimal("0.1"))
                .createdAt(LocalDateTime.now())
                .build();
        }
        
        recommendationRepository.save(recommendation);
        log.debug("Updated trending score for product: productId={}, score={}", productId, recommendation.getScore());
    }
}

