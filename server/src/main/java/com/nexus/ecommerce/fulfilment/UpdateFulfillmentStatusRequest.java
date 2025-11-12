package com.nexus.ecommerce.fulfilment.model.request;

import com.nexus.ecommerce.fulfilment.entity.Fulfillment;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating fulfillment status
 */
public record UpdateFulfillmentStatusRequest(
    @NotNull(message = "Status is required")
    Fulfillment.FulfillmentStatus status,
    
    String reason
) {}

