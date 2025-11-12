package com.nexus.ecommerce.inventory.controller;

import com.nexus.ecommerce.inventory.model.request.BatchStockRequest;
import com.nexus.ecommerce.inventory.model.request.ReservationRequest;
import com.nexus.ecommerce.inventory.model.request.ReleaseReservationRequest;
import com.nexus.ecommerce.inventory.model.request.StockAdjustmentRequest;
import com.nexus.ecommerce.inventory.model.response.StockResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.inventory.service.InventoryService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Inventory Controller
 * 
 * <p>This controller manages stock levels across multiple locations (warehouses, stores).
 * Inventory tracking is critical for preventing overselling and ensuring accurate
 * availability information for customers.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "Stock and inventory management endpoints")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Adjust stock quantity
     * 
     * <p>This is the primary endpoint for inventory management. It adjusts stock levels
     * atomically, supporting various operations:
     * <ul>
     *   <li>Restocking: Positive delta increases stock</li>
     *   <li>Sales: Negative delta decreases stock (order fulfillment)</li>
     *   <li>Returns: Positive delta for returned items</li>
     *   <li>Damaged goods: Negative delta for write-offs</li>
     * </ul>
     * 
     * <p>The endpoint prevents stock from going negative (unless business rules allow
     * backorders). It tracks the reason for adjustment (ORDER_RESERVE, RESTOCK, RETURN, etc.)
     * for audit purposes.
     * 
     * <p>Access control: SELLER and ADMIN roles can adjust inventory.
     * 
     * <p>This endpoint is protected and requires authentication.
     */
    @PostMapping("/adjust")
    @Operation(
        summary = "Adjust stock quantity",
        description = "Atomically adjusts stock levels for a product at a location. Supports restocking, sales, returns, and write-offs."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<StockResponse> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest adjustRequest,
            Authentication authentication) {
        
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        log.info("Adjusting stock for user: {}, tenant: {}", currentUserId, tenantId);
        
        StockResponse response = inventoryService.adjustStock(currentUserId, tenantId, roles, adjustRequest);
        
        return ApiResponse.success(response, "Stock adjusted successfully");
    }

    /**
     * Get stock level for a product at a location
     * 
     * <p>Retrieves current stock quantity for a specific product SKU at a location.
     * Used by checkout service to verify availability before order creation, and
     * by frontend to display stock status.
     * 
     * <p>This endpoint may be public (for availability checks) or protected.
     */
    @GetMapping("/stock")
    @Operation(
        summary = "Get stock level",
        description = "Retrieves current stock quantity for a product SKU at a specific location"
    )
    public ApiResponse<StockResponse> getStock(
            @RequestParam String sku,
            @RequestParam UUID locationId,
            Authentication authentication) {
        
        UUID tenantId = authentication != null ? getTenantIdFromAuthentication(authentication) : null;
        if (tenantId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Tenant ID is required");
        }
        
        StockResponse response = inventoryService.getStock(sku, locationId, tenantId);
        
        return ApiResponse.success(response, "Stock retrieved successfully");
    }

    /**
     * Get stock levels for multiple products
     * 
     * <p>Batch endpoint to retrieve stock levels for multiple SKUs at once. Optimizes
     * performance when checking availability for cart items or product listings.
     * 
     * <p>This endpoint may be public (for availability checks) or protected.
     */
    @PostMapping("/stock/batch")
    @Operation(
        summary = "Get stock levels for multiple products",
        description = "Batch retrieval of stock levels for multiple SKUs at specified locations"
    )
    public ApiResponse<List<StockResponse>> getBatchStock(
            @Valid @RequestBody BatchStockRequest batchStockRequest,
            Authentication authentication) {
        
        UUID tenantId = authentication != null ? getTenantIdFromAuthentication(authentication) : null;
        if (tenantId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Tenant ID is required");
        }
        
        List<StockResponse> response = inventoryService.getBatchStock(tenantId, batchStockRequest);
        
        return ApiResponse.success(response, "Stock levels retrieved successfully");
    }

    /**
     * Get all locations with stock for a product
     * 
     * <p>Returns all locations (warehouses, stores) that have stock for a given SKU.
     * Used for location-based fulfillment decisions and showing available pickup
     * locations to customers.
     * 
     * <p>This endpoint may be public or protected.
     */
    @GetMapping("/stock/{sku}/locations")
    @Operation(
        summary = "Get locations with stock for a product",
        description = "Returns all locations that have stock available for the specified SKU"
    )
    public ApiResponse<List<StockResponse>> getProductLocations(
            @PathVariable String sku,
            Authentication authentication) {
        
        UUID tenantId = authentication != null ? getTenantIdFromAuthentication(authentication) : null;
        if (tenantId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Tenant ID is required");
        }
        
        List<StockResponse> response = inventoryService.getProductLocations(sku, tenantId);
        
        return ApiResponse.success(response, "Locations retrieved successfully");
    }

    /**
     * Reserve inventory for an order
     * 
     * <p>Atomically reserves inventory items for a pending order. This prevents
     * overselling by locking stock until order confirmation or cancellation.
     * 
     * <p>Checkout service calls this before creating an order. If reservation fails
     * (insufficient stock), checkout cannot proceed.
     * 
     * <p>This endpoint is protected and requires authentication.
     */
    @PostMapping("/reserve")
    @Operation(
        summary = "Reserve inventory for order",
        description = "Atomically reserves stock items for a pending order. Used by checkout service before order creation."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> reserveInventory(
            @Valid @RequestBody ReservationRequest reserveRequest,
            Authentication authentication) {
        
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        log.info("Reserving inventory for order: {}, user: {}", reserveRequest.orderId(), currentUserId);
        
        inventoryService.reserveInventory(currentUserId, tenantId, reserveRequest);
        
        return ApiResponse.success(null, "Inventory reserved successfully");
    }

    /**
     * Release inventory reservation
     * 
     * <p>Releases previously reserved inventory. Used when:
     * <ul>
     *   <li>Order is cancelled</li>
     *   <li>Reservation expires (timeout)</li>
     *   <li>Payment fails during checkout</li>
     * </ul>
     * 
     * <p>This endpoint is protected and requires authentication.
     */
    @PostMapping("/release")
    @Operation(
        summary = "Release inventory reservation",
        description = "Releases reserved inventory back to available stock. Used for order cancellations or reservation expiry."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> releaseReservation(
            @Valid @RequestBody ReleaseReservationRequest releaseRequest,
            Authentication authentication) {
        
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        log.info("Releasing reservation for order: {}, user: {}", releaseRequest.orderId(), currentUserId);
        
        inventoryService.releaseReservation(currentUserId, tenantId, releaseRequest);
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User ID is required. Please ensure you are authenticated.");
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        try {
            return UUID.fromString(jwtAuth.getUserId());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid user ID format");
        }
    }

    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Tenant ID is required. Please ensure you are authenticated.");
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        try {
            return UUID.fromString(jwtAuth.getTenantId());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid tenant ID format");
        }
    }

    private List<String> getRolesFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            return List.of();
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        return jwtAuth.getRoles();
    }
}

