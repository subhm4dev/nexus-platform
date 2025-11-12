package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.model.request.CreateDriverRequest;
import com.nexus.ecommerce.fulfilment.model.request.UpdateDriverRequest;
import com.nexus.ecommerce.fulfilment.model.response.DriverResponse;

import java.util.List;
import java.util.UUID;

/**
 * Driver Service Interface
 */
public interface DriverService {
    
    /**
     * Create a new driver
     */
    DriverResponse createDriver(UUID tenantId, CreateDriverRequest request);
    
    /**
     * Get driver by ID
     */
    DriverResponse getDriverById(UUID driverId, UUID tenantId);
    
    /**
     * Get all drivers for tenant
     */
    List<DriverResponse> getAllDrivers(UUID tenantId);
    
    /**
     * Get available drivers
     */
    List<DriverResponse> getAvailableDrivers(UUID tenantId);
    
    /**
     * Update driver
     */
    DriverResponse updateDriver(UUID driverId, UUID tenantId, UpdateDriverRequest request);
}

