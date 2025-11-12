package com.nexus.ecommerce.fulfilment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for shipment cancellation
 */
public record CancelShipmentResponse(
    @JsonProperty("tracking_id")
    String trackingId,
    
    @JsonProperty("success")
    Boolean success,
    
    @JsonProperty("message")
    String message,
    
    @JsonProperty("refund_amount")
    java.math.BigDecimal refundAmount,
    
    @JsonProperty("error")
    String error
) {}

