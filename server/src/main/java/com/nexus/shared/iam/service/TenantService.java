package com.nexus.shared.iam.service;

import com.nexus.shared.iam.entity.Tenant;
import com.nexus.shared.iam.entity.UserDomain;

import java.util.Map;
import java.util.UUID;

/**
 * Tenant Service Interface
 * 
 * <p>Provides tenant-related operations for use by other modules.
 * In modulith architecture, this allows direct service calls instead of HTTP.
 */
public interface TenantService {
    
    /**
     * Get tenant by ID
     * 
     * @param tenantId Tenant ID
     * @return Tenant entity
     * @throws RuntimeException if tenant not found
     */
    Tenant getTenantById(UUID tenantId);
    
    /**
     * Get tenant information as a map (for compatibility with existing code)
     * 
     * @param tenantId Tenant ID
     * @return Map containing tenant information
     */
    Map<String, Object> getTenantInfo(UUID tenantId);
    
    /**
     * Update user domain mapping
     * 
     * @param userId User ID
     * @param newTenantId New tenant ID
     * @param domainId Domain ID
     */
    void updateUserDomainMapping(UUID userId, UUID newTenantId, UUID domainId);
    
    /**
     * Validate branch transfer
     * Validates that target tenant is a BRANCH tenant under the same APP tenant as current tenant
     * 
     * @param currentTenantId Current tenant ID
     * @param targetTenantId Target tenant ID
     * @param domainId Domain ID
     * @throws RuntimeException if validation fails
     */
    void validateBranchTransfer(UUID currentTenantId, UUID targetTenantId, UUID domainId);
}

