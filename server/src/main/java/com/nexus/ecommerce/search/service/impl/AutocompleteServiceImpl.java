package com.nexus.ecommerce.search.service.impl;

import com.nexus.ecommerce.search.model.response.AutocompleteResponse;
import com.nexus.ecommerce.search.repository.SearchIndexRepository;
import com.nexus.ecommerce.search.service.AutocompleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Autocomplete Service Implementation
 * Uses Redis sorted sets for fast prefix matching
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AutocompleteServiceImpl implements AutocompleteService {
    
    private final SearchIndexRepository searchIndexRepository;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${search.cache.autocomplete-size:10}")
    private int autocompleteSize;
    
    private static final String AUTocomplete_KEY_PREFIX = "autocomplete:";
    
    @Override
    @Transactional(readOnly = true)
    public AutocompleteResponse getSuggestions(UUID tenantId, String query) {
        log.debug("Getting autocomplete suggestions: tenantId={}, query={}", tenantId, query);
        
        if (query == null || query.trim().isEmpty()) {
            return new AutocompleteResponse(List.of(), List.of(), List.of());
        }
        
        String prefix = query.toLowerCase().trim();
        String redisKey = AUTocomplete_KEY_PREFIX + tenantId;
        
        // Try Redis sorted set first (fastest)
        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            Set<String> suggestions = zSetOps.rangeByLex(
                redisKey,
                org.springframework.data.redis.connection.RedisZSetCommands.Range.range()
                    .gte(prefix)
                    .lt(prefix + Character.MAX_VALUE)
            );
            
            if (suggestions != null && !suggestions.isEmpty()) {
                List<String> productSuggestions = suggestions.stream()
                    .limit(autocompleteSize)
                    .collect(Collectors.toList());
                
                log.debug("Redis autocomplete hit: {} suggestions", productSuggestions.size());
                return new AutocompleteResponse(
                    productSuggestions,
                    List.of(),  // TODO: Add category/subcategory autocomplete
                    List.of()
                );
            }
        } catch (Exception e) {
            log.warn("Error reading from Redis autocomplete: {}", e.getMessage());
        }
        
        // Fallback to database query
        List<String> productSuggestions = searchIndexRepository.findAutocompleteSuggestions(
            tenantId,
            prefix,
            autocompleteSize
        );
        
        // Update Redis sorted set for future queries
        try {
            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
            for (String suggestion : productSuggestions) {
                zSetOps.add(redisKey, suggestion, 0);
            }
            redisTemplate.expire(redisKey, Duration.ofHours(24));
        } catch (Exception e) {
            log.warn("Error updating Redis autocomplete: {}", e.getMessage());
        }
        
        return new AutocompleteResponse(
            productSuggestions,
            List.of(),  // TODO: Add category/subcategory autocomplete
            List.of()
        );
    }
}

