package com.nexus.ecommerce.search.service.impl;

import com.nexus.ecommerce.search.entity.SearchIndex;
import com.nexus.ecommerce.search.model.response.SearchResponse;
import com.nexus.ecommerce.search.repository.SearchIndexRepository;
import com.nexus.ecommerce.search.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Search Service Implementation
 * Fast live search with Redis caching and PostgreSQL full-text search
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    
    private final SearchIndexRepository searchIndexRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${search.cache.ttl-seconds:3600}")
    private long cacheTtlSeconds;
    
    private static final String CACHE_KEY_PREFIX = "search:";
    
    @Override
    @Transactional(readOnly = true)
    public SearchResponse search(UUID tenantId, String query, UUID categoryId, UUID subcategoryId, int page, int size) {
        log.debug("Searching: tenantId={}, query={}, categoryId={}, subcategoryId={}, page={}, size={}", 
            tenantId, query, categoryId, subcategoryId, page, size);
        
        // Generate cache key
        String cacheKey = generateCacheKey(tenantId, query, categoryId, subcategoryId, page, size);
        
        // Try to get from cache first
        try {
            String cachedResult = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                log.debug("Cache hit for search: {}", cacheKey);
                return objectMapper.readValue(cachedResult, SearchResponse.class);
            }
        } catch (Exception e) {
            log.warn("Error reading from cache: {}", e.getMessage());
        }
        
        // Cache miss - perform search
        log.debug("Cache miss - performing database search");
        Page<SearchIndex> searchResults;
        
        try {
            // Try full-text search first
            searchResults = searchIndexRepository.search(
                tenantId,
                query,
                categoryId,
                subcategoryId,
                PageRequest.of(page, size)
            );
        } catch (Exception e) {
            log.warn("Full-text search failed, falling back to simple search: {}", e.getMessage());
            // Fallback to simple LIKE search
            searchResults = searchIndexRepository.searchSimple(
                tenantId,
                query,
                categoryId,
                subcategoryId,
                PageRequest.of(page, size)
            );
        }
        
        // Convert to response
        SearchResponse response = toSearchResponse(searchResults, query);
        
        // Cache the result
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, responseJson, Duration.ofSeconds(cacheTtlSeconds));
            log.debug("Cached search result: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Error caching search result: {}", e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public SearchResponse searchByCategory(UUID categoryId, UUID tenantId, String query, int page, int size) {
        log.debug("Searching by category: categoryId={}, tenantId={}, query={}", categoryId, tenantId, query);
        
        String cacheKey = generateCacheKey(tenantId, query, categoryId, null, page, size);
        
        // Try cache
        try {
            String cachedResult = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return objectMapper.readValue(cachedResult, SearchResponse.class);
            }
        } catch (Exception e) {
            log.warn("Error reading from cache: {}", e.getMessage());
        }
        
        Page<SearchIndex> searchResults = searchIndexRepository.findByCategoryIdAndTenantId(
            categoryId, tenantId, PageRequest.of(page, size)
        );
        
        SearchResponse response = toSearchResponse(searchResults, query);
        
        // Cache result
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, responseJson, Duration.ofSeconds(cacheTtlSeconds));
        } catch (Exception e) {
            log.warn("Error caching search result: {}", e.getMessage());
        }
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public SearchResponse searchBySubcategory(UUID subcategoryId, UUID tenantId, String query, int page, int size) {
        log.debug("Searching by subcategory: subcategoryId={}, tenantId={}, query={}", subcategoryId, tenantId, query);
        
        String cacheKey = generateCacheKey(tenantId, query, null, subcategoryId, page, size);
        
        // Try cache
        try {
            String cachedResult = redisTemplate.opsForValue().get(cacheKey);
            if (cachedResult != null) {
                return objectMapper.readValue(cachedResult, SearchResponse.class);
            }
        } catch (Exception e) {
            log.warn("Error reading from cache: {}", e.getMessage());
        }
        
        Page<SearchIndex> searchResults = searchIndexRepository.findBySubcategoryIdAndTenantId(
            subcategoryId, tenantId, PageRequest.of(page, size)
        );
        
        SearchResponse response = toSearchResponse(searchResults, query);
        
        // Cache result
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, responseJson, Duration.ofSeconds(cacheTtlSeconds));
        } catch (Exception e) {
            log.warn("Error caching search result: {}", e.getMessage());
        }
        
        return response;
    }
    
    private String generateCacheKey(UUID tenantId, String query, UUID categoryId, UUID subcategoryId, int page, int size) {
        return String.format("%s%s:%s:%s:%s:%d:%d", 
            CACHE_KEY_PREFIX,
            tenantId,
            query != null ? query.toLowerCase() : "",
            categoryId != null ? categoryId : "",
            subcategoryId != null ? subcategoryId : "",
            page,
            size
        );
    }
    
    private SearchResponse toSearchResponse(Page<SearchIndex> searchResults, String query) {
        List<SearchResponse.ProductSearchResult> products = searchResults.getContent().stream()
            .map(si -> new SearchResponse.ProductSearchResult(
                si.getProductId(),
                si.getName(),
                si.getDescription(),
                si.getSku(),
                si.getCategoryId(),
                si.getSubcategoryId(),
                1.0  // Relevance score (would be calculated from ts_rank in real implementation)
            ))
            .collect(Collectors.toList());
        
        // Extract unique categories and subcategories
        List<SearchResponse.CategorySearchResult> categories = new ArrayList<>();
        List<SearchResponse.CategorySearchResult> subcategories = new ArrayList<>();
        
        // TODO: Fetch category/subcategory details from Catalog service if needed
        
        return new SearchResponse(
            products,
            categories,
            subcategories,
            searchResults.getTotalElements(),
            searchResults.getNumber(),
            searchResults.getSize(),
            searchResults.getTotalPages()
        );
    }
}

