package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.model.response.DashboardMetricsResponse;
import com.nexus.ecommerce.fulfilment.model.response.DriverPerformanceResponse;
import com.nexus.ecommerce.fulfilment.model.response.ProviderPerformanceResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service for analytics and dashboard metrics
 */
public interface AnalyticsService {
    
    /**
     * Get admin dashboard metrics
     */
    DashboardMetricsResponse getDashboardMetrics(UUID tenantId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get driver performance metrics
     */
    List<DriverPerformanceResponse> getDriverPerformance(UUID tenantId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get provider performance metrics
     */
    List<ProviderPerformanceResponse> getProviderPerformance(UUID tenantId, LocalDate startDate, LocalDate endDate);
}

