package com.nexus.ecommerce.fulfilment.model.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response for driver performance metrics
 */
public record DriverPerformanceResponse(
    UUID driverId,
    String driverName,
    String driverPhone,
    
    // Delivery counts
    Long totalDeliveries,
    Long completedDeliveries,
    Long failedDeliveries,
    
    // Performance metrics
    Double averageDeliveryTime, // in hours
    Double onTimeDeliveryRate, // percentage
    Double customerRating, // average rating
    
    // Earnings
    BigDecimal totalEarnings,
    BigDecimal averageEarningsPerDelivery
) {}

