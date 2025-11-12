package com.nexus.ecommerce.order.controller;

import com.nexus.ecommerce.order.entity.Order;
import com.nexus.ecommerce.order.model.request.CancelOrderRequest;
import com.nexus.ecommerce.order.model.request.CreateOrderRequest;
import com.nexus.ecommerce.order.model.request.ReturnOrderRequest;
import com.nexus.ecommerce.order.model.request.UpdateOrderStatusRequest;
import com.nexus.ecommerce.order.model.response.OrderResponse;
import com.nexus.ecommerce.order.model.response.OrderSummaryResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.order.service.OrderService;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Order Controller
 * 
 * <p>This controller manages order lifecycle from creation to delivery. It tracks
 * order status, manages order history, and coordinates with fulfillment services
 * for order processing.
 */
@RestController
@RequestMapping("/api/v1/order")
@Tag(name = "Orders", description = "Order management and tracking endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;

    /**
     * Create order
     * 
     * <p>Creates a new order from checkout data. This endpoint is typically called
     * by Checkout service after successful payment and inventory reservation.
     * 
     * <p>RBAC: CUSTOMER role required (or service-to-service call).
     */
    @PostMapping
    @Operation(
        summary = "Create order",
        description = "Creates a new order from checkout data. Triggers OrderCreated event to Kafka."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        
        log.info("Creating order");
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        OrderResponse response = orderService.createOrder(userId, tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Order created successfully"));
    }

    /**
     * Get order by ID
     * 
     * <p>Retrieves detailed order information including items, status, shipping address,
     * payment information, and tracking details.
     * 
     * <p>RBAC: Users can view their own orders. Admins/Sellers/Staff can view any order.
     */
    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves detailed order information including items, status, and tracking"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable UUID orderId,
            Authentication authentication) {
        
        log.info("Getting order: orderId={}", orderId);
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        java.util.List<String> roles = getRolesFromAuthentication(authentication);
        
        OrderResponse response = orderService.getOrderById(orderId, userId, tenantId, roles);
        return ResponseEntity.ok(ApiResponse.success(response, "Order retrieved successfully"));
    }

    /**
     * Get user's order history
     * 
     * <p>Returns paginated list of orders for the authenticated user.
     * 
     * <p>RBAC: CUSTOMER role required (users view own orders).
     */
    @GetMapping
    @Operation(
        summary = "Get order history",
        description = "Returns paginated list of orders for the authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getOrderHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) String sort,
            Authentication authentication) {
        
        log.info("Getting order history: page={}, size={}, status={}, sort={}", page, size, status, sort);
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        // Parse sort parameter (format: "field,direction" e.g., "created_at,desc")
        Pageable pageable;
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String field = sortParts[0].trim();
                Sort.Direction direction = sortParts[1].trim().equalsIgnoreCase("desc") 
                    ? Sort.Direction.DESC 
                    : Sort.Direction.ASC;
                pageable = PageRequest.of(page, size, Sort.by(direction, field));
            } else {
                // Default to created_at desc if sort format is invalid
                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            }
        } else {
            // Default to created_at desc if no sort specified
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        
        Page<OrderSummaryResponse> response = orderService.getOrderHistory(userId, tenantId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Order history retrieved successfully"));
    }

    /**
     * Find order by payment ID
     * 
     * <p>Finds an order by its payment ID. Used for idempotency checks in checkout service.
     * 
     * <p>RBAC: CUSTOMER role required (users can only find their own orders).
     */
    @GetMapping("/by-payment/{paymentId}")
    @Operation(
        summary = "Find order by payment ID",
        description = "Finds an order by payment ID. Used for idempotency checks in checkout."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<OrderSummaryResponse>> findOrderByPaymentId(
            @PathVariable UUID paymentId,
            Authentication authentication) {
        
        log.info("Finding order by payment ID: paymentId={}", paymentId);
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        OrderSummaryResponse response = orderService.findOrderByPaymentId(paymentId, userId, tenantId);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No order found with the given payment ID"));
        }
        return ResponseEntity.ok(ApiResponse.success(response, "Order found successfully"));
    }

    /**
     * Update order status
     * 
     * <p>Updates order status. Typically accessed by Fulfillment service or Admin users.
     * 
     * <p>RBAC: ADMIN, SELLER, or STAFF role required.
     */
    @PutMapping("/{orderId}/status")
    @Operation(
        summary = "Update order status",
        description = "Updates order status. Used by fulfillment service or admins. Triggers status update events."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            Authentication authentication) {
        
        log.info("Updating order status: orderId={}, status={}", orderId, request.status());
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        java.util.List<String> roles = getRolesFromAuthentication(authentication);
        
        OrderResponse response = orderService.updateOrderStatus(orderId, userId, tenantId, roles, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Order status updated successfully"));
    }

    /**
     * Cancel order
     * 
     * <p>Cancels an order that hasn't been shipped yet.
     * 
     * <p>RBAC: Users can cancel their own orders. Admins can cancel any order.
     */
    @PostMapping("/{orderId}/cancel")
    @Operation(
        summary = "Cancel order",
        description = "Cancels an order. Releases inventory, processes refund, and sends cancellation notification."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('SELLER')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @Valid @RequestBody CancelOrderRequest request,
            Authentication authentication) {
        
        log.info("Cancelling order: orderId={}", orderId);
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        java.util.List<String> roles = getRolesFromAuthentication(authentication);
        
        OrderResponse response = orderService.cancelOrder(orderId, userId, tenantId, roles, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Order cancelled successfully"));
    }

    /**
     * Request return for order
     * 
     * <p>Initiates a return request for an order or order items.
     * 
     * <p>RBAC: CUSTOMER role required (users can return their own orders).
     */
    @PostMapping("/{orderId}/return")
    @Operation(
        summary = "Request return for order",
        description = "Initiates a return request for order items. Requires approval before refund processing."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> requestReturn(
            @PathVariable UUID orderId,
            @Valid @RequestBody ReturnOrderRequest request,
            Authentication authentication) {
        
        log.info("Requesting return for order: orderId={}", orderId);
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        OrderResponse response = orderService.requestReturn(orderId, userId, tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Return requested successfully"));
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
    
    /**
     * Extract roles from JWT authentication token
     */
    private java.util.List<String> getRolesFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getRoles();
        }
        return java.util.Collections.emptyList();
    }
}

