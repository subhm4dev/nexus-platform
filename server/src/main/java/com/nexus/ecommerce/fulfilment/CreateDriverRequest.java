package com.nexus.ecommerce.fulfilment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a driver
 */
public record CreateDriverRequest(
    @NotBlank(message = "Name is required")
    String name,
    
    @NotBlank(message = "Phone is required")
    String phone,
    
    @Email(message = "Email must be valid")
    String email,
    
    @JsonProperty("vehicle_type")
    String vehicleType,
    
    @JsonProperty("vehicle_number")
    String vehicleNumber
) {}

