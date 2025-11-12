package com.nexus.ecommerce.fulfilment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for shipment creation
 */
public record CreateShipmentResponse(
    @JsonProperty("tracking_id")
    String trackingId,
    
    @JsonProperty("tracking_url")
    String trackingUrl,
    
    @JsonProperty("awb_number")
    String awbNumber,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("estimated_delivery")
    LocalDateTime estimatedDelivery,
    
    @JsonProperty("shipping_cost")
    BigDecimal shippingCost,
    
    @JsonProperty("error")
    String error
) {}

