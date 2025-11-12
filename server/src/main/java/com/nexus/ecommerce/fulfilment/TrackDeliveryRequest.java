package com.nexus.ecommerce.fulfilment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for tracking delivery location
 */
public record TrackDeliveryRequest(
    @NotNull(message = "Latitude is required")
    BigDecimal latitude,
    
    @NotNull(message = "Longitude is required")
    BigDecimal longitude,
    
    @JsonProperty("location_description")
    String locationDescription,
    
    String status
) {}

