package com.nexus.ecommerce.order.model.request;

import com.nexus.ecommerce.order.entity.Order;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating order status
 */
public record UpdateOrderStatusRequest(
    @NotNull(message = "Status is required")
    Order.OrderStatus status,
    
    String reason,
    
    @JsonProperty("changed_by")
    String changedBy
) {}

