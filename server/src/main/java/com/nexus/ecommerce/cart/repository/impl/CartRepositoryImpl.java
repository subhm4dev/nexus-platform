package com.nexus.ecommerce.cart.repository.impl;

import com.nexus.ecommerce.cart.model.Cart;
import com.nexus.ecommerce.cart.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis implementation of CartRepository
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CartRepositoryImpl implements CartRepository {
    
    private static final String CART_KEY_PREFIX = "cart:";
    private static final long DEFAULT_TTL_SECONDS = 7 * 24 * 60 * 60; // 7 days
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper;
    
    private String buildKey(UUID tenantId, UUID userId) {
        return CART_KEY_PREFIX + tenantId + ":" + userId;
    }
    
    @Override
    public Optional<Cart> findByTenantIdAndUserId(UUID tenantId, UUID userId) {
        String key = buildKey(tenantId, userId);
        
        try {
            Object value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                return Optional.empty();
            }
            
            // Handle deserialization: GenericJackson2JsonRedisSerializer may return LinkedHashMap
            // or the deserialization may fail due to missing type information
            if (value instanceof Cart) {
                return Optional.of((Cart) value);
            } else if (value instanceof LinkedHashMap) {
                // Convert LinkedHashMap to Cart using ObjectMapper
                try {
                    Cart cart = redisObjectMapper.convertValue(value, Cart.class);
                    return Optional.of(cart);
                } catch (Exception e) {
                    log.error("Failed to convert LinkedHashMap to Cart: {}", e.getMessage());
                    // If conversion fails, delete the corrupted entry
                    redisTemplate.delete(key);
                    return Optional.empty();
                }
            } else {
                log.warn("Unexpected type from Redis: {}", value.getClass().getName());
                return Optional.empty();
            }
        } catch (org.springframework.data.redis.serializer.SerializationException e) {
            // Handle deserialization errors (e.g., missing @class property in old data)
            log.warn("Failed to deserialize cart from Redis (key: {}), deleting corrupted entry: {}", key, e.getMessage());
            try {
                redisTemplate.delete(key);
            } catch (Exception deleteEx) {
                log.error("Failed to delete corrupted cart entry: {}", deleteEx.getMessage());
            }
            return Optional.empty();
        }
    }
    
    @Override
    public void save(Cart cart, long ttlSeconds) {
        String key = buildKey(cart.getTenantId(), cart.getUserId());
        redisTemplate.opsForValue().set(key, cart, Duration.ofSeconds(ttlSeconds));
        log.debug("Saved cart to Redis: {}", key);
    }
    
    @Override
    public void save(Cart cart) {
        save(cart, DEFAULT_TTL_SECONDS);
    }
    
    @Override
    public void delete(UUID tenantId, UUID userId) {
        String key = buildKey(tenantId, userId);
        redisTemplate.delete(key);
        log.debug("Deleted cart from Redis: {}", key);
    }
    
    @Override
    public boolean exists(UUID tenantId, UUID userId) {
        String key = buildKey(tenantId, userId);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}

