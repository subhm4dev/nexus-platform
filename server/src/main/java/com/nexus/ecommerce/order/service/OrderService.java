package com.nexus.ecommerce.order.service;

import com.nexus.ecommerce.order.entity.Order;
import com.nexus.ecommerce.order.model.request.CancelOrderRequest;
import com.nexus.ecommerce.order.model.request.CreateOrderRequest;
import com.nexus.ecommerce.order.model.request.ReturnOrderRequest;
import com.nexus.ecommerce.order.model.request.UpdateOrderStatusRequest;
import com.nexus.ecommerce.order.model.response.OrderResponse;
import com.nexus.ecommerce.order.model.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Order Service Interface
 */
public interface OrderService {
    
    /**
     * Create a new order
     */
    OrderResponse createOrder(UUID userId, UUID tenantId, CreateOrderRequest request);
    
    /**
     * Get order by ID
     */
    OrderResponse getOrderById(UUID orderId, UUID userId, UUID tenantId, java.util.List<String> userRoles);
    
    /**
     * Get order history for user
     */
    Page<OrderSummaryResponse> getOrderHistory(
        UUID userId, 
        UUID tenantId, 
        Order.OrderStatus status, 
        Pageable pageable
    );
    
    /**
     * Update order status
     */
    OrderResponse updateOrderStatus(
        UUID orderId, 
        UUID userId, 
        UUID tenantId, 
        java.util.List<String> userRoles,
        UpdateOrderStatusRequest request
    );
    
    /**
     * Cancel order
     */
    OrderResponse cancelOrder(
        UUID orderId, 
        UUID userId, 
        UUID tenantId, 
        java.util.List<String> userRoles,
        CancelOrderRequest request
    );
    
    /**
     * Request order return
     */
    OrderResponse requestReturn(
        UUID orderId, 
        UUID userId, 
        UUID tenantId, 
        ReturnOrderRequest request
    );
    
    /**
     * Check if user can access order
     */
    boolean canAccessOrder(UUID currentUserId, UUID orderUserId, java.util.List<String> userRoles);
    
    /**
     * Find order by payment ID
     * Used for idempotency checks in checkout service
     */
    OrderSummaryResponse findOrderByPaymentId(UUID paymentId, UUID userId, UUID tenantId);
}

