package com.nexus.ecommerce.fulfilment.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a shipment with delivery provider
 */
public record CreateShipmentRequest(
    @JsonProperty("order_id")
    UUID orderId,
    
    @JsonProperty("pickup_address")
    Address pickupAddress,
    
    @JsonProperty("delivery_address")
    Address deliveryAddress,
    
    @JsonProperty("weight_kg")
    BigDecimal weightKg,
    
    @JsonProperty("dimensions")
    Dimensions dimensions,
    
    @JsonProperty("items")
    List<ShipmentItem> items,
    
    @JsonProperty("cod_amount")
    BigDecimal codAmount,
    
    @JsonProperty("is_intercity")
    Boolean isIntercity,
    
    @JsonProperty("special_instructions")
    String specialInstructions
) {
    public record Address(
        @JsonProperty("name")
        String name,
        
        @JsonProperty("phone")
        String phone,
        
        @JsonProperty("address_line1")
        String addressLine1,
        
        @JsonProperty("address_line2")
        String addressLine2,
        
        @JsonProperty("city")
        String city,
        
        @JsonProperty("state")
        String state,
        
        @JsonProperty("pincode")
        String pincode,
        
        @JsonProperty("country")
        String country
    ) {}
    
    public record Dimensions(
        @JsonProperty("length_cm")
        BigDecimal lengthCm,
        
        @JsonProperty("width_cm")
        BigDecimal widthCm,
        
        @JsonProperty("height_cm")
        BigDecimal heightCm
    ) {}
    
    public record ShipmentItem(
        @JsonProperty("name")
        String name,
        
        @JsonProperty("quantity")
        Integer quantity,
        
        @JsonProperty("value")
        BigDecimal value
    ) {}
}

