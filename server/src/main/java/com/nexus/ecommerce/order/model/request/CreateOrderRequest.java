package com.nexus.ecommerce.order.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating an order
 */
public record CreateOrderRequest(
    @NotNull(message = "Shipping address ID is required")
    @JsonProperty("shipping_address_id")
    UUID shippingAddressId,
    
    @NotNull(message = "Payment ID is required")
    @JsonProperty("payment_id")
    UUID paymentId,
    
    @NotEmpty(message = "Order items are required")
    @Valid
    List<OrderItemRequest> items,
    
    @NotNull(message = "Subtotal is required")
    @Positive(message = "Subtotal must be positive")
    BigDecimal subtotal,
    
    @JsonProperty("discount_amount")
    BigDecimal discountAmount,
    
    @JsonProperty("tax_amount")
    BigDecimal taxAmount,
    
    @JsonProperty("shipping_cost")
    BigDecimal shippingCost,
    
    @NotNull(message = "Total is required")
    @Positive(message = "Total must be positive")
    BigDecimal total,
    
    String currency,
    
    String notes
) {
    public record OrderItemRequest(
        @NotNull(message = "Product ID is required")
        @JsonProperty("product_id")
        UUID productId,
        
        @NotNull(message = "SKU is required")
        String sku,
        
        @JsonProperty("product_name")
        String productName,
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,
        
        @NotNull(message = "Unit price is required")
        @Positive(message = "Unit price must be positive")
        @JsonProperty("unit_price")
        BigDecimal unitPrice,
        
        @NotNull(message = "Total price is required")
        @Positive(message = "Total price must be positive")
        @JsonProperty("total_price")
        BigDecimal totalPrice
    ) {}
}

