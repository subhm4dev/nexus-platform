package com.nexus.shared.iam.service;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.shared.iam.entity.Tenant;
import com.nexus.shared.iam.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tenant Hierarchy Service
 * 
 * <p>Manages tenant hierarchy relationships for multi-branch support.
 * Provides methods to expand tenant queries to include child tenants (branches).
 * 
 * <p>Usage:
 * <ul>
 *   <li>Seller with parent tenant → queries expand to include all branches</li>
 *   <li>Staff at branch tenant → queries only for their branch</li>
 *   <li>Customer at app tenant → queries only for their app</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantHierarchyService {
    
    private final TenantRepository tenantRepository;
    
    /**
     * Get all tenant IDs that should be included in queries for a given tenant.
     * 
     * <p>If tenant is a parent (has children), returns [parent, child1, child2, ...]
     * If tenant is a child or has no children, returns [tenant]
     * 
     * <p>This enables sellers at parent tenant to see all branch data,
     * while staff at branch tenant only see their branch data.
     * 
     * @param tenantId Tenant ID to expand
     * @return List of tenant IDs (parent + all children, or just the tenant if no children)
     */
    @Transactional(readOnly = true)
    public List<UUID> getTenantHierarchy(UUID tenantId) {
        // Verify tenant exists
        if (!tenantRepository.existsById(tenantId)) {
            throw new BusinessException(
                ErrorCode.SKU_REQUIRED,
                "Tenant not found: " + tenantId
            );
        }
        
        // Get all child tenants (branches)
        List<Tenant> childTenants = tenantRepository.findByParentTenantId(tenantId);
        
        if (childTenants.isEmpty()) {
            // No children - return just this tenant
            log.debug("Tenant {} has no children, returning single tenant", tenantId);
            return List.of(tenantId);
        } else {
            // Has children - return parent + all children
            List<UUID> allTenantIds = new ArrayList<>();
            allTenantIds.add(tenantId); // Parent
            childTenants.forEach(child -> {
                allTenantIds.add(child.getId()); // Children
                log.debug("Added child tenant {} to hierarchy for parent {}", child.getId(), tenantId);
            });
            log.debug("Tenant {} has {} children, returning {} tenant IDs", 
                tenantId, childTenants.size(), allTenantIds.size());
            return allTenantIds;
        }
    }
    
    /**
     * Check if tenant is a parent tenant (has children/branches)
     * 
     * @param tenantId Tenant ID to check
     * @return true if tenant has children
     */
    @Transactional(readOnly = true)
    public boolean isParentTenant(UUID tenantId) {
        return tenantRepository.existsByParentTenantId(tenantId);
    }
    
    /**
     * Check if tenant is a child tenant (has parent)
     * 
     * @param tenantId Tenant ID to check
     * @return true if tenant has parent
     */
    @Transactional(readOnly = true)
    public boolean isChildTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
            .map(Tenant::getParentTenantId)
            .isPresent();
    }
    
    /**
     * Get all child tenants (branches) of a parent tenant
     * 
     * @param parentTenantId Parent tenant ID
     * @return List of child tenants
     */
    @Transactional(readOnly = true)
    public List<Tenant> getAllChildTenants(UUID parentTenantId) {
        return tenantRepository.findByParentTenantId(parentTenantId);
    }
    
    /**
     * Get parent tenant of a child tenant
     * 
     * @param childTenantId Child tenant ID
     * @return Parent tenant, or null if not a child
     */
    @Transactional(readOnly = true)
    public Tenant getParentTenant(UUID childTenantId) {
        return tenantRepository.findById(childTenantId)
            .map(Tenant::getParentTenantId)
            .flatMap(tenantRepository::findById)
            .orElse(null);
    }
}

