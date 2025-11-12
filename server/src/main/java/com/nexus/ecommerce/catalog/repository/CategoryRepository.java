package com.nexus.ecommerce.catalog.repository;

import com.nexus.ecommerce.catalog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    /**
     * Find all categories for a tenant
     * 
     * @param tenantId Tenant ID
     * @return List of categories
     */
    List<Category> findByTenantId(UUID tenantId);
    
    /**
     * Find categories by parent ID
     * 
     * @param parentId Parent category ID
     * @param tenantId Tenant ID
     * @return List of child categories
     */
    List<Category> findByParentIdAndTenantId(UUID parentId, UUID tenantId);
    
    /**
     * Find category by name and tenant ID
     * 
     * @param name Category name
     * @param tenantId Tenant ID
     * @return Optional Category
     */
    java.util.Optional<Category> findByNameAndTenantId(String name, UUID tenantId);
    
    /**
     * Find category by ID and tenant ID
     * 
     * @param id Category ID
     * @param tenantId Tenant ID
     * @return Optional Category
     */
    java.util.Optional<Category> findByIdAndTenantId(UUID id, UUID tenantId);
    
    /**
     * Find all categories for multiple tenants (for tenant hierarchy)
     * 
     * @param tenantIds List of tenant IDs (parent + children)
     * @return List of categories
     */
    List<Category> findByTenantIdIn(List<UUID> tenantIds);
    
    /**
     * Find categories by parent ID and multiple tenant IDs
     * 
     * @param parentId Parent category ID
     * @param tenantIds List of tenant IDs
     * @return List of child categories
     */
    List<Category> findByParentIdAndTenantIdIn(UUID parentId, List<UUID> tenantIds);
}

