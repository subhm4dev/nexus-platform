package com.nexus.shared.iam.repository;

import com.nexus.shared.iam.constants.TenantType;
import com.nexus.shared.iam.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    
    /**
     * Find tenant by name
     * Used to find the default marketplace tenant
     */
    Optional<Tenant> findByName(String name);
    
    /**
     * Find tenant by name and domain ID
     * Used to find the default marketplace tenant in a specific domain
     * 
     * @param name Tenant name (e.g., "Marketplace")
     * @param domainId Domain ID
     * @return Optional Tenant
     */
    Optional<Tenant> findByNameAndDomainId(String name, UUID domainId);
    
    /**
     * Find all tenants in a specific domain
     * 
     * @param domainId Domain ID
     * @return List of tenants
     */
    List<Tenant> findByDomainId(UUID domainId);
    
    /**
     * Find all child tenants (branches) of a parent tenant
     * 
     * @param parentTenantId Parent tenant ID
     * @return List of child tenants
     */
    List<Tenant> findByParentTenantId(UUID parentTenantId);
    
    /**
     * Check if tenant has children (is a parent tenant)
     * 
     * @param parentTenantId Tenant ID to check
     * @return true if tenant has children
     */
    boolean existsByParentTenantId(UUID parentTenantId);
    
    /**
     * Find tenants by type and domain
     * 
     * @param type Tenant type
     * @param domainId Domain ID
     * @return List of tenants
     */
    List<Tenant> findByTypeAndDomainId(TenantType type, UUID domainId);
}
