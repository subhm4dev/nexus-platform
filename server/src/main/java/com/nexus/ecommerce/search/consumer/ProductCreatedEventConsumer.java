package com.nexus.ecommerce.search.consumer;

import com.nexus.ecommerce.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Consumer for ProductCreatedEvent
 * Updates search index when a product is created
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedEventConsumer {
    
    private final SearchIndexService searchIndexService;
    
    @KafkaListener(topics = "product-created", groupId = "search-service-group")
    @Transactional
    public void handleProductCreated(Map<String, Object> eventMap) {
        try {
            log.info("Received ProductCreated event: {}", eventMap);
            
            UUID productId = UUID.fromString(eventMap.get("product_id").toString());
            UUID tenantId = UUID.fromString(eventMap.get("tenant_id").toString());
            String sku = eventMap.get("sku").toString();
            
            log.info("Indexing product: productId={}, tenantId={}, sku={}", productId, tenantId, sku);
            
            // Note: ProductCreatedEvent only has basic info (productId, sku, tenantId, sellerId)
            // We need to fetch full product details from Catalog service to index properly
            // For now, we'll index with minimal info and update later when product details are available
            // TODO: Fetch product details from Catalog service via HTTP client
            
            // Placeholder - will be enhanced to fetch full product details
            searchIndexService.indexProduct(
                productId,
                tenantId,
                null,  // name - will fetch from Catalog
                null,  // description - will fetch from Catalog
                sku,
                null,  // categoryId - will fetch from Catalog
                null,  // subcategoryId - will fetch from Catalog
                sku   // keywords - use SKU for now
            );
            
            log.info("Product indexed: productId={}", productId);
        } catch (Exception e) {
            log.error("Error processing ProductCreated event: {}", eventMap, e);
            // Don't throw - allow processing to continue for other events
        }
    }
}

