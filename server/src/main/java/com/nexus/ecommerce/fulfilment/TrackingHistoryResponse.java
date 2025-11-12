package com.nexus.ecommerce.fulfilment.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for tracking history
 */
public record TrackingHistoryResponse(
    UUID id,
    
    @JsonProperty("delivery_id")
    UUID deliveryId,
    
    BigDecimal latitude,
    
    BigDecimal longitude,
    
    @JsonProperty("location_description")
    String locationDescription,
    
    String status,
    
    @JsonProperty("updated_by")
    UUID updatedBy,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt
) {}

