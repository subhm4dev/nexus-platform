package com.nexus.ecommerce.order.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.ecommerce.order.entity.Order;
import com.nexus.ecommerce.order.entity.OrderItem;
import com.nexus.ecommerce.order.entity.OrderStatusHistory;
import com.nexus.ecommerce.order.event.OrderCancelledEvent;
import com.nexus.ecommerce.order.event.OrderCreatedEvent;
import com.nexus.ecommerce.order.event.OrderStatusUpdatedEvent;
import com.nexus.ecommerce.order.model.request.CancelOrderRequest;
import com.nexus.ecommerce.order.model.request.CreateOrderRequest;
import com.nexus.ecommerce.order.model.request.ReturnOrderRequest;
import com.nexus.ecommerce.order.model.request.UpdateOrderStatusRequest;
import com.nexus.ecommerce.order.model.response.OrderResponse;
import com.nexus.ecommerce.order.model.response.OrderSummaryResponse;
import com.nexus.ecommerce.order.repository.OrderItemRepository;
import com.nexus.ecommerce.order.repository.OrderRepository;
import com.nexus.ecommerce.order.repository.OrderStatusHistoryRepository;
import com.nexus.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Order Service Implementation
 */
@Service("orderService")
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String ORDER_CREATED_TOPIC = "order-created";
    private static final String ORDER_STATUS_UPDATED_TOPIC = "order-status-updated";
    private static final String ORDER_CANCELLED_TOPIC = "order-cancelled";
    
    @Override
    @Transactional
    public OrderResponse createOrder(UUID userId, UUID tenantId, CreateOrderRequest request) {
        log.info("Creating order: userId={}, tenantId={}", userId, tenantId);
        
        // Generate order number
        String orderNumber = generateOrderNumber();
        
        // Create order entity
        Order order = Order.builder()
            .userId(userId)
            .tenantId(tenantId)
            .orderNumber(orderNumber)
            .status(Order.OrderStatus.PLACED)
            .shippingAddressId(request.shippingAddressId())
            .paymentId(request.paymentId())
            .subtotal(request.subtotal())
            .discountAmount(request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO)
            .taxAmount(request.taxAmount() != null ? request.taxAmount() : BigDecimal.ZERO)
            .shippingCost(request.shippingCost() != null ? request.shippingCost() : BigDecimal.ZERO)
            .total(request.total())
            .currency(request.currency() != null ? request.currency() : "INR")
            .notes(request.notes())
            .createdAt(LocalDateTime.now())
            .build();
        
        // Create order items
        List<OrderItem> items = request.items().stream()
            .map(itemRequest -> OrderItem.builder()
                .order(order)
                .productId(itemRequest.productId())
                .sku(itemRequest.sku())
                .productName(itemRequest.productName())
                .quantity(itemRequest.quantity())
                .unitPrice(itemRequest.unitPrice())
                .totalPrice(itemRequest.totalPrice())
                .currency(request.currency() != null ? request.currency() : "INR")
                .build())
            .collect(Collectors.toList());
        
        order.setItems(items);
        
        // Create initial status history
        OrderStatusHistory statusHistory = OrderStatusHistory.builder()
            .order(order)
            .status(Order.OrderStatus.PLACED)
            .previousStatus(null)
            .reason("Order placed")
            .changedBy(userId)
            .createdAt(LocalDateTime.now())
            .build();
        
        order.setStatusHistory(List.of(statusHistory));
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        statusHistoryRepository.save(statusHistory);
        
        log.info("Order created: orderId={}, orderNumber={}", savedOrder.getId(), savedOrder.getOrderNumber());
        
        // Publish OrderCreated event
        try {
            OrderCreatedEvent event = OrderCreatedEvent.of(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getUserId(),
                savedOrder.getTenantId(),
                savedOrder.getShippingAddressId(),
                savedOrder.getPaymentId(),
                savedOrder.getItems().stream()
                    .map(item -> new OrderCreatedEvent.OrderItemEvent(
                        item.getProductId(),
                        item.getSku(),
                        item.getQuantity(),
                        item.getUnitPrice()
                    ))
                    .collect(Collectors.toList()),
                savedOrder.getTotal(),
                savedOrder.getCurrency(),
                savedOrder.getCreatedAt()
            );
            kafkaTemplate.send(ORDER_CREATED_TOPIC, savedOrder.getId().toString(), event);
            log.info("Published OrderCreated event: orderId={}", savedOrder.getId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCreated event: orderId={}", savedOrder.getId(), e);
        }
        
        return toResponse(savedOrder);
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId, UUID userId, UUID tenantId, List<String> userRoles) {
        log.debug("Getting order: orderId={}, userId={}", orderId, userId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Order not found: " + orderId
            ));
        
        // Check access
        if (!canAccessOrder(userId, order.getUserId(), userRoles)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Access denied to order: " + orderId
            );
        }
        
        // Verify tenant
        if (!order.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Order belongs to different tenant"
            );
        }
        
        return toResponse(order);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOrderHistory(
        UUID userId, 
        UUID tenantId, 
        Order.OrderStatus status, 
        Pageable pageable
    ) {
        log.debug("Getting order history: userId={}, status={}", userId, status);
        
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByUserIdAndTenantIdAndStatus(userId, tenantId, status, pageable);
        } else {
            orders = orderRepository.findByUserIdAndTenantId(userId, tenantId, pageable);
        }
        
        return orders.map(this::toSummaryResponse);
    }
    
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(
        UUID orderId, 
        UUID userId, 
        UUID tenantId, 
        List<String> userRoles,
        UpdateOrderStatusRequest request
    ) {
        log.info("Updating order status: orderId={}, newStatus={}", orderId, request.status());
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Order not found: " + orderId
            ));
        
        // Verify tenant
        if (!order.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Order belongs to different tenant"
            );
        }
        
        // Check authorization (admin/seller/staff can update, users can only update their own)
        boolean isAdmin = userRoles != null && (
            userRoles.contains("ADMIN") || 
            userRoles.contains("SELLER") ||
            userRoles.contains("STAFF")
        );
        
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Only admins or order owner can update order status"
            );
        }
        
        // Validate status transition
        validateStatusTransition(order.getStatus(), request.status());
        
        // Update status
        Order.OrderStatus previousStatus = order.getStatus();
        order.setStatus(request.status());
        order.setUpdatedAt(LocalDateTime.now());
        
        // Create status history
        OrderStatusHistory statusHistory = OrderStatusHistory.builder()
            .order(order)
            .status(request.status())
            .previousStatus(previousStatus.name())
            .reason(request.reason())
            .changedBy(userId)
            .createdAt(LocalDateTime.now())
            .build();
        
        statusHistoryRepository.save(statusHistory);
        order.getStatusHistory().add(statusHistory);
        
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order status updated: orderId={}, status={} -> {}", orderId, previousStatus, request.status());
        
        // Publish OrderStatusUpdated event
        try {
            OrderStatusUpdatedEvent event = OrderStatusUpdatedEvent.of(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getUserId(),
                savedOrder.getTenantId(),
                savedOrder.getStatus(),
                previousStatus.name(),
                request.reason(),
                savedOrder.getUpdatedAt()
            );
            kafkaTemplate.send(ORDER_STATUS_UPDATED_TOPIC, savedOrder.getId().toString(), event);
            log.info("Published OrderStatusUpdated event: orderId={}", savedOrder.getId());
        } catch (Exception e) {
            log.error("Failed to publish OrderStatusUpdated event: orderId={}", savedOrder.getId(), e);
        }
        
        return toResponse(savedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse cancelOrder(
        UUID orderId, 
        UUID userId, 
        UUID tenantId, 
        List<String> userRoles,
        CancelOrderRequest request
    ) {
        log.info("Cancelling order: orderId={}", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Order not found: " + orderId
            ));
        
        // Check access
        if (!canAccessOrder(userId, order.getUserId(), userRoles)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Access denied to order: " + orderId
            );
        }
        
        // Verify tenant
        if (!order.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Order belongs to different tenant"
            );
        }
        
        // Check if order can be cancelled
        if (order.getStatus() == Order.OrderStatus.SHIPPED || 
            order.getStatus() == Order.OrderStatus.DELIVERED ||
            order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessException(
                ErrorCode.INVALID_OPERATION,
                "Order cannot be cancelled. Current status: " + order.getStatus()
            );
        }
        
        // Update status
        Order.OrderStatus previousStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Create status history
        OrderStatusHistory statusHistory = OrderStatusHistory.builder()
            .order(order)
            .status(Order.OrderStatus.CANCELLED)
            .previousStatus(previousStatus.name())
            .reason(request.reason())
            .changedBy(userId)
            .createdAt(LocalDateTime.now())
            .build();
        
        statusHistoryRepository.save(statusHistory);
        order.getStatusHistory().add(statusHistory);
        
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order cancelled: orderId={}", orderId);
        
        // Publish OrderCancelled event
        try {
            OrderCancelledEvent event = OrderCancelledEvent.of(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getUserId(),
                savedOrder.getTenantId(),
                savedOrder.getPaymentId(),
                request.reason(),
                savedOrder.getUpdatedAt()
            );
            kafkaTemplate.send(ORDER_CANCELLED_TOPIC, savedOrder.getId().toString(), event);
            log.info("Published OrderCancelled event: orderId={}", savedOrder.getId());
        } catch (Exception e) {
            log.error("Failed to publish OrderCancelled event: orderId={}", savedOrder.getId(), e);
        }
        
        return toResponse(savedOrder);
    }
    
    @Override
    @Transactional
    public OrderResponse requestReturn(
        UUID orderId, 
        UUID userId, 
        UUID tenantId, 
        ReturnOrderRequest request
    ) {
        log.info("Requesting return for order: orderId={}", orderId);
        
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Order not found: " + orderId
            ));
        
        // Check ownership
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Access denied to order: " + orderId
            );
        }
        
        // Verify tenant
        if (!order.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Order belongs to different tenant"
            );
        }
        
        // Check if order is eligible for return
        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new BusinessException(
                ErrorCode.INVALID_OPERATION,
                "Order must be delivered to request return. Current status: " + order.getStatus()
            );
        }
        
        // For now, just update status to RETURNED
        // In a full implementation, you'd create a ReturnRequest entity
        order.setStatus(Order.OrderStatus.RETURNED);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Create status history
        OrderStatusHistory statusHistory = OrderStatusHistory.builder()
            .order(order)
            .status(Order.OrderStatus.RETURNED)
            .previousStatus(order.getStatus().name())
            .reason(request.reason())
            .changedBy(userId)
            .createdAt(LocalDateTime.now())
            .build();
        
        statusHistoryRepository.save(statusHistory);
        order.getStatusHistory().add(statusHistory);
        
        Order savedOrder = orderRepository.save(order);
        
        log.info("Return requested for order: orderId={}", orderId);
        
        return toResponse(savedOrder);
    }
    
    @Override
    public boolean canAccessOrder(UUID currentUserId, UUID orderUserId, List<String> userRoles) {
        // Users can always view their own orders
        if (currentUserId.equals(orderUserId)) {
            return true;
        }
        
        // Admins, Sellers, and Staff can view any order
        boolean isAdmin = userRoles != null && (
            userRoles.contains("ADMIN") || 
            userRoles.contains("SELLER") ||
            userRoles.contains("STAFF")
        );
        
        if (isAdmin) {
            log.debug("Admin access granted for order");
            return true;
        }
        
        // Default: deny access
        log.warn("Access denied: user {} attempted to access order {}", currentUserId, orderUserId);
        return false;
    }
    
    /**
     * Validate status transition
     */
    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Allow same status (idempotent)
        if (currentStatus == newStatus) {
            return;
        }
        
        // Define valid transitions
        boolean isValid = switch (currentStatus) {
            case PLACED -> newStatus == Order.OrderStatus.CONFIRMED || 
                          newStatus == Order.OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == Order.OrderStatus.PROCESSING || 
                             newStatus == Order.OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == Order.OrderStatus.SHIPPED || 
                              newStatus == Order.OrderStatus.CANCELLED;
            case SHIPPED -> newStatus == Order.OrderStatus.DELIVERED;
            case DELIVERED -> newStatus == Order.OrderStatus.RETURNED;
            case CANCELLED, RETURNED -> false; // Terminal states
        };
        
        if (!isValid) {
            throw new BusinessException(
                ErrorCode.INVALID_OPERATION,
                "Invalid status transition: " + currentStatus + " -> " + newStatus
            );
        }
    }
    
    /**
     * Generate unique order number
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Convert Order entity to OrderResponse DTO
     */
    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getUserId(),
            order.getTenantId(),
            order.getStatus(),
            order.getShippingAddressId(),
            order.getPaymentId(),
            order.getItems().stream()
                .map(item -> new OrderResponse.OrderItemResponse(
                    item.getId(),
                    item.getProductId(),
                    item.getSku(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice(),
                    item.getCurrency()
                ))
                .collect(Collectors.toList()),
            order.getSubtotal(),
            order.getDiscountAmount(),
            order.getTaxAmount(),
            order.getShippingCost(),
            order.getTotal(),
            order.getCurrency(),
            order.getNotes(),
            order.getStatusHistory().stream()
                .map(history -> new OrderResponse.OrderStatusHistoryResponse(
                    history.getId(),
                    history.getStatus(),
                    history.getPreviousStatus(),
                    history.getReason(),
                    history.getChangedBy(),
                    history.getCreatedAt()
                ))
                .collect(Collectors.toList()),
            order.getCreatedAt(),
            order.getUpdatedAt()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public OrderSummaryResponse findOrderByPaymentId(UUID paymentId, UUID userId, UUID tenantId) {
        log.debug("Finding order by payment ID: paymentId={}, userId={}", paymentId, userId);
        
        Order order = orderRepository.findByPaymentIdAndUserIdAndTenantId(paymentId, userId, tenantId)
            .orElse(null);
        
        if (order == null) {
            log.debug("No order found with payment ID: paymentId={}, userId={}", paymentId, userId);
            return null;
        }
        
        log.info("Found order by payment ID: orderId={}, paymentId={}", order.getId(), paymentId);
        return toSummaryResponse(order);
    }
    
    /**
     * Convert Order entity to OrderSummaryResponse DTO
     */
    private OrderSummaryResponse toSummaryResponse(Order order) {
        return new OrderSummaryResponse(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            order.getPaymentId(),
            order.getTotal(),
            order.getCurrency(),
            order.getItems().size(),
            order.getCreatedAt()
        );
    }
}

