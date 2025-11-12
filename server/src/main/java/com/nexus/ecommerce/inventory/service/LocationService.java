package com.nexus.ecommerce.inventory.service;

import com.nexus.ecommerce.inventory.model.request.LocationRequest;
import com.nexus.ecommerce.inventory.model.response.LocationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for location operations
 */
public interface LocationService {
    
    /**
     * Create a new location
     * 
     * @param tenantId Tenant ID from JWT claims
     * @param currentUserId Currently authenticated user ID
     * @param roles Current user's roles
     * @param request Location request DTO
     * @return LocationResponse with created location data
     * @throws com.nexus.libs.error.exception.BusinessException if location name duplicate exists or unauthorized
     */
    LocationResponse createLocation(
        UUID tenantId,
        UUID currentUserId,
        List<String> roles,
        LocationRequest request
    );
    
    /**
     * Get location by ID
     * 
     * @param locationId Location ID
     * @param tenantId Tenant ID from JWT claims
     * @return LocationResponse if found
     * @throws com.nexus.libs.error.exception.BusinessException if location not found
     */
    LocationResponse getLocationById(UUID locationId, UUID tenantId);
    
    /**
     * Get all locations for a tenant
     * 
     * @param tenantId Tenant ID from JWT claims
     * @param activeOnly If true, returns only active locations
     * @return List of LocationResponse
     */
    List<LocationResponse> getAllLocations(UUID tenantId, boolean activeOnly);
    
    /**
     * Update location
     * 
     * @param locationId Location ID
     * @param tenantId Tenant ID from JWT claims
     * @param currentUserId Currently authenticated user ID
     * @param roles Current user's roles
     * @param request Location request DTO with updated fields
     * @return LocationResponse with updated location data
     * @throws com.nexus.libs.error.exception.BusinessException if location not found or unauthorized
     */
    LocationResponse updateLocation(
        UUID locationId,
        UUID tenantId,
        UUID currentUserId,
        List<String> roles,
        LocationRequest request
    );
    
    /**
     * Deactivate location (soft delete)
     * 
     * @param locationId Location ID
     * @param tenantId Tenant ID from JWT claims
     * @param currentUserId Currently authenticated user ID
     * @param roles Current user's roles
     * @throws com.nexus.libs.error.exception.BusinessException if location not found or unauthorized
     */
    void deactivateLocation(
        UUID locationId,
        UUID tenantId,
        UUID currentUserId,
        List<String> roles
    );
    
    /**
     * Check if user has permission to manage locations
     * Only SELLER and ADMIN can manage locations
     * 
     * @param roles Current user's roles
     * @return true if user can manage locations
     */
    boolean canManageLocations(List<String> roles);
}

