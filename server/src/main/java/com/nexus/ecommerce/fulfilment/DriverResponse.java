package com.nexus.ecommerce.fulfilment.model.response;

import com.nexus.ecommerce.fulfilment.entity.Driver;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for driver details
 */
public record DriverResponse(
    UUID id,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    String name,
    
    String phone,
    
    String email,
    
    @JsonProperty("vehicle_type")
    String vehicleType,
    
    @JsonProperty("vehicle_number")
    String vehicleNumber,
    
    Driver.DriverStatus status,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {}

