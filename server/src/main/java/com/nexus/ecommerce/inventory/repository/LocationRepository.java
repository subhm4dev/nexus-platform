package com.nexus.ecommerce.inventory.repository;

import com.nexus.ecommerce.inventory.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    List<Location> findByTenantIdAndActiveTrue(UUID tenantId);
    List<Location> findByTenantId(UUID tenantId);
    java.util.Optional<Location> findByIdAndTenantId(UUID id, UUID tenantId);
    
    /**
     * Find locations by city and tenant ID (single tenant)
     * 
     * @param city City name
     * @param tenantId Tenant ID
     * @return List of locations in the city for the tenant
     */
    List<Location> findByCityAndTenantId(String city, UUID tenantId);
    
    /**
     * Find locations by city and multiple tenant IDs (for tenant hierarchy)
     * 
     * @param city City name
     * @param tenantIds List of tenant IDs (parent + children)
     * @return List of locations in the city for any of the tenants
     */
    List<Location> findByCityAndTenantIdIn(String city, List<UUID> tenantIds);
    
    /**
     * Find active locations by city and tenant IDs
     * 
     * @param city City name
     * @param tenantIds List of tenant IDs
     * @return List of active locations in the city
     */
    List<Location> findByCityAndTenantIdInAndActiveTrue(String city, List<UUID> tenantIds);
}

