package com.nexus.ecommerce.fulfilment.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response for driver dashboard
 */
public record DriverDashboardResponse(
    UUID driverId,
    String driverName,
    String driverStatus, // AVAILABLE, BUSY, OFFLINE
    
    // Today's deliveries
    List<DeliverySummary> todaysDeliveries,
    Integer todaysDeliveryCount,
    Integer completedToday,
    Integer pendingToday,
    
    // Earnings
    BigDecimal todaysEarnings,
    BigDecimal weeklyEarnings,
    BigDecimal monthlyEarnings,
    
    // Performance
    Integer totalDeliveries,
    Double averageDeliveryTime,
    Double onTimeDeliveryRate,
    
    // Current location
    BigDecimal currentLatitude,
    BigDecimal currentLongitude,
    LocalDateTime lastLocationUpdate
) {
    public record DeliverySummary(
        UUID deliveryId,
        UUID fulfillmentId,
        String trackingNumber,
        String status,
        String deliveryAddress,
        BigDecimal deliveryLatitude,
        BigDecimal deliveryLongitude,
        LocalDateTime estimatedArrival,
        Integer attemptCount,
        String priority
    ) {}
}

