package com.nexus.ecommerce.cart.repository;

import com.nexus.ecommerce.cart.model.Cart;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for cart operations in Redis
 * 
 * <p>Cart keys follow pattern: cart:{tenantId}:{userId}
 */
public interface CartRepository {
    
    /**
     * Find cart by tenant ID and user ID
     * 
     * @param tenantId Tenant ID
     * @param userId User ID
     * @return Optional Cart if found
     */
    Optional<Cart> findByTenantIdAndUserId(UUID tenantId, UUID userId);
    
    /**
     * Save cart to Redis with TTL
     * 
     * @param cart Cart to save
     * @param ttlSeconds Time to live in seconds (default: 7 days for abandoned carts)
     */
    void save(Cart cart, long ttlSeconds);
    
    /**
     * Save cart to Redis with default TTL (7 days)
     * 
     * @param cart Cart to save
     */
    void save(Cart cart);
    
    /**
     * Delete cart from Redis
     * 
     * @param tenantId Tenant ID
     * @param userId User ID
     */
    void delete(UUID tenantId, UUID userId);
    
    /**
     * Check if cart exists
     * 
     * @param tenantId Tenant ID
     * @param userId User ID
     * @return true if cart exists
     */
    boolean exists(UUID tenantId, UUID userId);
}

