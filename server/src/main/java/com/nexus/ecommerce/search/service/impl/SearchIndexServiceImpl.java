package com.nexus.ecommerce.search.service.impl;

import com.nexus.ecommerce.search.entity.SearchIndex;
import com.nexus.ecommerce.search.repository.SearchIndexRepository;
import com.nexus.ecommerce.search.service.SearchIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Search Index Service Implementation
 * Maintains search index from Catalog events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIndexServiceImpl implements SearchIndexService {
    
    private final SearchIndexRepository searchIndexRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "search:";
    
    @Override
    @Transactional
    public void indexProduct(UUID productId, UUID tenantId, String name, String description, String sku, UUID categoryId, UUID subcategoryId, String keywords) {
        log.info("Indexing product: productId={}, tenantId={}", productId, tenantId);
        
        // Find existing index entry
        SearchIndex searchIndex = searchIndexRepository.findByProductIdAndTenantId(productId, tenantId)
            .orElse(SearchIndex.builder()
                .productId(productId)
                .tenantId(tenantId)
                .createdAt(LocalDateTime.now())
                .build());
        
        // Update fields
        searchIndex.setName(name);
        searchIndex.setDescription(description);
        searchIndex.setSku(sku);
        searchIndex.setCategoryId(categoryId);
        searchIndex.setSubcategoryId(subcategoryId);
        searchIndex.setKeywords(keywords);
        searchIndex.setUpdatedAt(LocalDateTime.now());
        
        searchIndexRepository.save(searchIndex);
        
        // Invalidate cache for this tenant
        invalidateCache(tenantId);
        
        log.info("Product indexed: productId={}", productId);
    }
    
    @Override
    @Transactional
    public void removeProduct(UUID productId, UUID tenantId) {
        log.info("Removing product from index: productId={}, tenantId={}", productId, tenantId);
        
        searchIndexRepository.deleteByProductIdAndTenantId(productId, tenantId);
        
        // Invalidate cache
        invalidateCache(tenantId);
        
        log.info("Product removed from index: productId={}", productId);
    }
    
    @Override
    @Transactional
    public void updateCategoryIndex(UUID categoryId, UUID tenantId) {
        log.info("Updating category index: categoryId={}, tenantId={}", categoryId, tenantId);
        
        // Invalidate cache for this category
        invalidateCache(tenantId);
        
        // TODO: Re-index all products in this category if needed
    }
    
    private void invalidateCache(UUID tenantId) {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + tenantId + ":*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Invalidated {} cache keys for tenant: {}", keys.size(), tenantId);
            }
        } catch (Exception e) {
            log.warn("Error invalidating cache: {}", e.getMessage());
        }
    }
}

