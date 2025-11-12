package com.nexus.ecommerce.promo.controller;

import com.nexus.ecommerce.promo.model.request.CouponRequest;
import com.nexus.ecommerce.promo.model.request.CouponValidationRequest;
import com.nexus.ecommerce.promo.model.request.PriceCalculationRequest;
import com.nexus.ecommerce.promo.model.request.PromotionRequest;
import com.nexus.ecommerce.promo.model.response.CouponResponse;
import com.nexus.ecommerce.promo.model.response.PriceCalculationResponse;
import com.nexus.ecommerce.promo.model.response.PromotionResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.promo.service.PromotionService;
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

import java.util.List;
import java.util.UUID;

/**
 * Promotion Controller
 * 
 * <p>This controller manages pricing rules, discounts, coupons, and surge pricing.
 * It applies dynamic pricing on top of base catalog prices, enabling promotions,
 * flash sales, and demand-based pricing.
 * 
 * <p>Why we need these APIs:
 * <ul>
 *   <li><b>Promotional Pricing:</b> Enables discounts, percentage-off deals, and
 *       fixed-amount reductions. Essential for marketing campaigns and sales events.</li>
 *   <li><b>Coupon Management:</b> Supports coupon codes that customers can apply
 *       during checkout. Tracks usage limits and expiry dates.</li>
 *   <li><b>Surge Pricing:</b> Applies dynamic pricing based on demand, time of day,
 *       or inventory levels (e.g., alcohol delivery during peak hours).</li>
 *   <li><b>Price Calculation:</b> Checkout and Cart services query this service to
 *       get final prices after applying promotions. Ensures consistent pricing across
 *       the platform.</li>
 *   <li><b>Tenant-Scoped Promotions:</b> Each tenant (seller) can create their own
 *       promotions, enabling marketplace scenarios where sellers manage their own pricing.</li>
 * </ul>
 * 
 * <p>Promotions are evaluated in order of priority, with highest-priority promotions
 * applied first. Multiple promotions can stack if configured.
 */
@RestController
@RequestMapping("/api/v1/promotion")
@Tag(name = "Promotions", description = "Pricing, discounts, coupons, and surge pricing management")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class PromotionController {
    
    private final PromotionService promotionService;

    /**
     * Calculate final price for a product
     * 
     * <p>This endpoint applies all applicable promotions to a product's base price
     * and returns the final price. Used by Cart and Checkout services to display
     * accurate pricing to customers.
     * 
     * <p>Calculation logic:
     * <ul>
     *   <li>Fetches base price from Catalog service</li>
     *   <li>Applies active promotions (percentage, fixed amount, surge)</li>
     *   <li>Applies coupon if provided</li>
     *   <li>Returns final price with breakdown (base, discount, final)</li>
     * </ul>
     * 
     * <p>This endpoint is public (for price display).
     */
    @PostMapping("/calculate")
    @Operation(
        summary = "Calculate final price with promotions",
        description = "Applies active promotions and coupons to a product's base price and returns final price"
    )
    public ResponseEntity<ApiResponse<PriceCalculationResponse>> calculatePrice(
            @Valid @RequestBody PriceCalculationRequest priceRequest,
            Authentication authentication) {
        
        log.info("Calculating price: productId={}, quantity={}", 
            priceRequest.productId(), priceRequest.quantity());
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        PriceCalculationResponse response = promotionService.calculatePrice(tenantId, priceRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Price calculated successfully"));
    }

    /**
     * Create a new promotion
     * 
     * <p>Allows sellers/admins to create promotional rules like:
     * <ul>
     *   <li>Percentage discounts (e.g., 20% off)</li>
     *   <li>Fixed amount discounts (e.g., $10 off)</li>
     *   <li>Buy X Get Y deals</li>
     *   <li>Category-wide promotions</li>
     * </ul>
     * 
     * <p>Promotions can have validity periods (start/end dates), usage limits, and
     * eligibility criteria (specific products, categories, minimum order value).
     * 
     * <p>Access control: SELLER and ADMIN roles can create promotions.
     * 
     * <p>This endpoint is protected and requires authentication.
     */
    @PostMapping
    @Operation(
        summary = "Create a new promotion",
        description = "Creates a promotional rule (discount, percentage-off, etc.) with validity period and eligibility criteria"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @Valid @RequestBody PromotionRequest promotionRequest,
            Authentication authentication) {
        
        log.info("Creating promotion: name={}", promotionRequest.name());
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        PromotionResponse response = promotionService.createPromotion(userId, tenantId, roles, promotionRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Promotion created successfully"));
    }

    /**
     * Validate and apply coupon code
     * 
     * <p>Validates a coupon code and returns discount information. Used during checkout
     * when customers enter coupon codes.
     * 
     * <p>Validation checks:
     * <ul>
     *   <li>Coupon exists and is active</li>
     *   <li>Not expired</li>
     *   <li>Usage limit not exceeded</li>
     *   <li>Applies to current cart/order items</li>
     *   <li>Minimum order value met (if required)</li>
     * </ul>
     * 
     * <p>This endpoint is public (for coupon validation during checkout).
     */
    @PostMapping("/coupon/validate")
    @Operation(
        summary = "Validate coupon code",
        description = "Validates a coupon code and returns discount information if valid and applicable"
    )
    public ResponseEntity<ApiResponse<CouponResponse>> validateCoupon(
            @Valid @RequestBody CouponValidationRequest couponRequest,
            Authentication authentication) {
        
        log.info("Validating coupon: code={}", couponRequest.couponCode());
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        CouponResponse response = promotionService.validateCoupon(tenantId, couponRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "Coupon validated successfully"));
    }

    /**
     * Create a coupon code
     * 
     * <p>Generates a new coupon code that customers can use for discounts. Supports
     * single-use or multi-use coupons with usage limits.
     * 
     * <p>Access control: SELLER and ADMIN roles can create coupons.
     * 
     * <p>This endpoint is protected and requires authentication.
     */
    @PostMapping("/coupon")
    @Operation(
        summary = "Create a coupon code",
        description = "Creates a new coupon code with discount rules, validity period, and usage limits"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(
            @Valid @RequestBody CouponRequest couponRequest,
            Authentication authentication) {
        
        log.info("Creating coupon: code={}", couponRequest.code());
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        CouponResponse response = promotionService.createCoupon(userId, tenantId, roles, couponRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Coupon created successfully"));
    }

    /**
     * Get active promotions for a product
     * 
     * <p>Returns all active promotions currently applicable to a product. Used by
     * frontend to display promotional badges and discount information.
     * 
     * <p>This endpoint is public (for product display).
     */
    @GetMapping("/product/{productId}/active")
    @Operation(
        summary = "Get active promotions for a product",
        description = "Returns all currently active promotions applicable to the specified product"
    )
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions(
            @PathVariable UUID productId,
            Authentication authentication) {
        
        log.info("Getting active promotions for product: {}", productId);
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        List<PromotionResponse> response = promotionService.getActivePromotions(productId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response, "Active promotions retrieved successfully"));
    }
    
    /**
     * Extract user ID from JWT authentication token
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return UUID.fromString(jwtToken.getUserId());
        }
        return null; // Public endpoint
    }
    
    /**
     * Extract tenant ID from JWT authentication token
     */
    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return UUID.fromString(jwtToken.getTenantId());
        }
        // For public endpoints, tenantId might come from header or be null
        // In production, you'd extract from X-Tenant-Id header
        return null;
    }
    
    /**
     * Extract roles from JWT authentication token
     */
    private List<String> getRolesFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getRoles();
        }
        return List.of();
    }
}

