package com.nexus.ecommerce.fulfilment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for assigning a driver
 */
public record AssignDriverRequest(
    @NotNull(message = "Driver ID is required")
    @JsonProperty("driver_id")
    UUID driverId
) {}

