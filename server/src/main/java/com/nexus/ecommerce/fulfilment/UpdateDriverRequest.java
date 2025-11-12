package com.nexus.ecommerce.fulfilment.model.request;

import com.nexus.ecommerce.fulfilment.entity.Driver;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for updating a driver
 */
public record UpdateDriverRequest(
    String name,
    
    String phone,
    
    String email,
    
    @JsonProperty("vehicle_type")
    String vehicleType,
    
    @JsonProperty("vehicle_number")
    String vehicleNumber,
    
    Driver.DriverStatus status
) {}

