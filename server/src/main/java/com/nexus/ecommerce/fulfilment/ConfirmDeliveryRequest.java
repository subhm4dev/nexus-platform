package com.nexus.ecommerce.fulfilment.model.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request for confirming delivery (Agent or Customer)
 */
public record ConfirmDeliveryRequest(
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    BigDecimal latitude,
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    BigDecimal longitude,
    
    @NotNull(message = "Location accuracy is required")
    @DecimalMin(value = "0.1", message = "Location accuracy must be positive")
    @DecimalMax(value = "20.0", message = "Location accuracy must be <= 20 meters")
    BigDecimal locationAccuracy, // in meters
    
    Boolean customerNotAvailable, // For agent: true if customer not available
    Boolean agentNotAvailable      // For customer: true if agent not available
) {}

