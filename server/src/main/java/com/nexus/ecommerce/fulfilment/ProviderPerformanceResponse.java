package com.nexus.ecommerce.fulfilment.model.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response for provider performance metrics
 */
public record ProviderPerformanceResponse(
    UUID providerId,
    String providerCode,
    String providerName,
    
    // Delivery counts
    Long totalDeliveries,
    Long completedDeliveries,
    Long failedDeliveries,
    
    // Performance metrics
    Double averageDeliveryTime, // in hours
    Double onTimeDeliveryRate, // percentage
    
    // Cost metrics
    BigDecimal totalCost,
    BigDecimal averageCostPerDelivery
) {}

