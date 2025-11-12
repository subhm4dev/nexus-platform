package com.nexus.ecommerce.fulfilment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for tracking information
 */
public record TrackingResponse(
    @JsonProperty("tracking_id")
    String trackingId,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("current_location")
    String currentLocation,
    
    @JsonProperty("latitude")
    BigDecimal latitude,
    
    @JsonProperty("longitude")
    BigDecimal longitude,
    
    @JsonProperty("estimated_delivery")
    LocalDateTime estimatedDelivery,
    
    @JsonProperty("history")
    List<TrackingEvent> history,
    
    @JsonProperty("error")
    String error
) {
    public record TrackingEvent(
        @JsonProperty("status")
        String status,
        
        @JsonProperty("location")
        String location,
        
        @JsonProperty("timestamp")
        LocalDateTime timestamp,
        
        @JsonProperty("description")
        String description
    ) {}
}

