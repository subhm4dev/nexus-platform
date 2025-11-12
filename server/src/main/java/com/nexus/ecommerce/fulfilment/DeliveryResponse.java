package com.nexus.ecommerce.fulfilment.model.response;

import com.nexus.ecommerce.fulfilment.entity.Delivery;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for delivery details
 */
public record DeliveryResponse(
    UUID id,
    
    @JsonProperty("fulfillment_id")
    UUID fulfillmentId,
    
    @JsonProperty("driver_id")
    UUID driverId,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    @JsonProperty("current_location")
    String currentLocation,
    
    BigDecimal latitude,
    
    BigDecimal longitude,
    
    Delivery.DeliveryStatus status,
    
    @JsonProperty("tracking_number")
    String trackingNumber,
    
    @JsonProperty("tracking_history")
    List<TrackingHistoryResponse> trackingHistory,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {}

