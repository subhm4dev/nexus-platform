package com.nexus.ecommerce.cart.controller;

import com.nexus.ecommerce.cart.model.request.AddItemRequest;
import com.nexus.ecommerce.cart.model.request.CouponRequest;
import com.nexus.ecommerce.cart.model.request.UpdateItemRequest;
import com.nexus.ecommerce.cart.model.response.CartResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.cart.service.CartService;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Shopping Cart Controller
 * 
 * <p>This controller manages shopping cart operations using Redis for high-performance
 * session storage. Carts are user-specific and stored temporarily until checkout or expiry.
 * 
 * <p>Why we need these APIs:
 * <ul>
 *   <li><b>Cart Management:</b> Allows customers to add/remove items, update quantities,
 *       and save items for later. Core shopping experience functionality.</li>
 *   <li><b>Price Calculation:</b> Integrates with Promotion service to show accurate
 *       pricing with discounts applied. Displays subtotals, discounts, and final totals.</li>
 *   <li><b>Checkout Preparation:</b> Checkout service reads cart contents to create orders.
 *       Cart serves as the source of truth for order items.</li>
 *   <li><b>Performance:</b> Redis-backed storage ensures fast cart operations even
 *       under high load. Supports concurrent updates with optimistic locking.</li>
 *   <li><b>Session Management:</b> Carts are tied to user sessions and can persist
 *       across devices if user is authenticated.</li>
 * </ul>
 * 
 * <p>Carts are stored in Redis with TTL (time-to-live) for automatic cleanup of
 * abandoned carts. Cart keys follow pattern: `cart:{tenantId}:{userId}`.
 */
@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Shopping Cart", description = "Shopping cart management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    
    private final CartService cartService;

    /**
     * Add item to cart
     * 
     * <p>Adds a product to the user's shopping cart. If the product already exists
     * in cart, increments quantity. Fetches product details from Catalog service
     * and applies promotions from Promotion service.
     * 
     * <p>Business logic:
     * <ul>
     *   <li>Validates product exists and is available</li>
     *   <li>Checks inventory availability</li>
     *   <li>Fetches current price with promotions applied</li>
     *   <li>Updates or creates cart entry</li>
     * </ul>
     * 
     * <p>This endpoint is protected and requires authentication.
     * RBAC: CUSTOMER role required (users manage own carts).
     */
    @PostMapping("/item")
    @Operation(
        summary = "Add item to cart",
        description = "Adds a product to the shopping cart. If product already exists, increments quantity."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @Valid @RequestBody AddItemRequest addItemRequest,
            Authentication authentication) {
        
        log.info("Adding item to cart: productId={}, quantity={}", 
            addItemRequest.productId(), addItemRequest.quantity());
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        CartResponse response = cartService.addItem(userId, tenantId, addItemRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Item added to cart successfully"));
    }

    /**
     * Update cart item quantity
     * 
     * <p>Modifies the quantity of an existing cart item. Validates inventory availability
     * before allowing quantity increases.
     * 
     * <p>This endpoint is protected and requires authentication.
     * RBAC: CUSTOMER role required (users manage own carts).
     */
    @PutMapping("/item/{itemId}")
    @Operation(
        summary = "Update cart item quantity",
        description = "Updates the quantity of a cart item. Validates inventory before allowing increases."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @PathVariable String itemId,
            @Valid @RequestBody UpdateItemRequest updateRequest,
            Authentication authentication) {
        
        log.info("Updating cart item: itemId={}, newQuantity={}", itemId, updateRequest.quantity());
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        CartResponse response = cartService.updateItem(userId, tenantId, itemId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart item updated successfully"));
    }

    /**
     * Remove item from cart
     * 
     * <p>Removes a product from the cart entirely. Recalculates cart totals after removal.
     * 
     * <p>This endpoint is protected and requires authentication.
     * RBAC: CUSTOMER role required (users manage own carts).
     */
    @DeleteMapping("/item/{itemId}")
    @Operation(
        summary = "Remove item from cart",
        description = "Removes a product from the shopping cart and recalculates totals"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable String itemId,
            Authentication authentication) {
        
        log.info("Removing cart item: itemId={}", itemId);
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        CartResponse response = cartService.removeItem(userId, tenantId, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Item removed from cart successfully"));
    }

    /**
     * Get current cart
     * 
     * <p>Retrieves the user's current shopping cart with all items, quantities,
     * prices, and calculated totals. Used to display cart contents in the frontend.
     * 
     * <p>This endpoint is protected and requires authentication.
     * RBAC: CUSTOMER role required (users manage own carts).
     */
    @GetMapping
    @Operation(
        summary = "Get current cart",
        description = "Retrieves the user's shopping cart with all items, quantities, prices, and totals"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        log.info("Getting cart for user");
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        CartResponse response = cartService.getCart(userId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart retrieved successfully"));
    }

    /**
     * Clear cart
     * 
     * <p>Removes all items from the cart. Useful for resetting cart state or
     * after successful checkout.
     * 
     * <p>This endpoint is protected and requires authentication.
     * RBAC: CUSTOMER role required (users manage own carts).
     */
    @DeleteMapping
    @Operation(
        summary = "Clear cart",
        description = "Removes all items from the shopping cart"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        log.info("Clearing cart for user");
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        cartService.clearCart(userId, tenantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Apply coupon to cart
     * 
     * <p>Applies a coupon code to the cart, applying discount if valid. Recalculates
     * cart totals with coupon discount applied.
     * 
     * <p>This endpoint is protected and requires authentication.
     * RBAC: CUSTOMER role required (users manage own carts).
     */
    @PostMapping("/coupon")
    @Operation(
        summary = "Apply coupon to cart",
        description = "Applies a coupon code to the cart and recalculates totals with discount"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> applyCoupon(
            @Valid @RequestBody CouponRequest couponRequest,
            Authentication authentication) {
        
        log.info("Applying coupon: couponCode={}", couponRequest.couponCode());
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        CartResponse response = cartService.applyCoupon(userId, tenantId, couponRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Coupon applied successfully"));
    }

    /**
     * Remove coupon from cart
     * 
     * <p>Removes applied coupon and recalculates cart totals without discount.
     * 
     * <p>This endpoint is protected and requires authentication.
     * RBAC: CUSTOMER role required (users manage own carts).
     */
    @DeleteMapping("/coupon")
    @Operation(
        summary = "Remove coupon from cart",
        description = "Removes applied coupon and recalculates cart totals"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> removeCoupon(Authentication authentication) {
        log.info("Removing coupon from cart");
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        CartResponse response = cartService.removeCoupon(userId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response, "Coupon removed successfully"));
    }
    
    /**
     * Extract user ID from JWT authentication token
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return UUID.fromString(jwtToken.getUserId());
        }
        throw new IllegalStateException("Invalid authentication token");
    }
    
    /**
     * Extract tenant ID from JWT authentication token
     */
    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return UUID.fromString(jwtToken.getTenantId());
        }
        throw new IllegalStateException("Invalid authentication token");
    }
}

