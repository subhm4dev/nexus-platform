package com.nexus.ecommerce.fulfilment.model.response;

import java.time.LocalDate;
import java.util.Map;

/**
 * Response for admin dashboard metrics
 */
public record DashboardMetricsResponse(
    // Counts
    Long totalFulfillments,
    Long pendingCount,
    Long inTransitCount,
    Long deliveredCount,
    Long failedCount,
    Long cancelledCount,
    
    // Metrics
    Double averageDeliveryTime, // in hours
    Double onTimeDeliveryRate, // percentage
    Double failedDeliveryRate, // percentage
    
    // Trends
    Map<LocalDate, Long> dailyDeliveries,
    Map<LocalDate, Double> dailyAverageTime,
    
    // Provider breakdown
    Map<String, Long> deliveriesByProvider,
    
    // Status breakdown
    Map<String, Long> statusBreakdown,
    
    // Time range
    LocalDate startDate,
    LocalDate endDate
) {}

