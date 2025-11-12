package com.nexus.ecommerce.cart.service;

import com.nexus.ecommerce.cart.model.Cart;
import com.nexus.ecommerce.cart.model.request.AddItemRequest;
import com.nexus.ecommerce.cart.model.request.CouponRequest;
import com.nexus.ecommerce.cart.model.request.UpdateItemRequest;
import com.nexus.ecommerce.cart.model.response.CartResponse;

import java.util.UUID;

/**
 * Service interface for cart operations
 */
public interface CartService {
    
    /**
     * Add item to cart
     * 
     * @param userId User ID from JWT
     * @param tenantId Tenant ID from JWT
     * @param request Add item request
     * @return CartResponse with updated cart
     */
    CartResponse addItem(UUID userId, UUID tenantId, AddItemRequest request);
    
    /**
     * Update cart item quantity
     * 
     * @param userId User ID from JWT
     * @param tenantId Tenant ID from JWT
     * @param itemId Item ID
     * @param request Update request
     * @return CartResponse with updated cart
     */
    CartResponse updateItem(UUID userId, UUID tenantId, String itemId, UpdateItemRequest request);
    
    /**
     * Remove item from cart
     * 
     * @param userId User ID from JWT
     * @param tenantId Tenant ID from JWT
     * @param itemId Item ID
     * @return CartResponse with updated cart
     */
    CartResponse removeItem(UUID userId, UUID tenantId, String itemId);
    
    /**
     * Get current cart
     * 
     * @param userId User ID from JWT
     * @param tenantId Tenant ID from JWT
     * @return CartResponse
     */
    CartResponse getCart(UUID userId, UUID tenantId);
    
    /**
     * Clear cart
     * 
     * @param userId User ID from JWT
     * @param tenantId Tenant ID from JWT
     */
    void clearCart(UUID userId, UUID tenantId);
    
    /**
     * Apply coupon to cart
     * 
     * @param userId User ID from JWT
     * @param tenantId Tenant ID from JWT
     * @param request Coupon request
     * @return CartResponse with updated cart
     */
    CartResponse applyCoupon(UUID userId, UUID tenantId, CouponRequest request);
    
    /**
     * Remove coupon from cart
     * 
     * @param userId User ID from JWT
     * @param tenantId Tenant ID from JWT
     * @return CartResponse with updated cart
     */
    CartResponse removeCoupon(UUID userId, UUID tenantId);
}

