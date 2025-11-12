package com.nexus.ecommerce.catalog.service;

import com.nexus.ecommerce.catalog.model.request.CategoryRequest;
import com.nexus.ecommerce.catalog.model.response.CategoryResponse;
import com.nexus.ecommerce.catalog.model.response.CategoryTreeResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for category operations
 */
public interface CategoryService {
    
    /**
     * Create a new category
     * 
     * @param tenantId Tenant ID from JWT claims
     * @param currentUserId Currently authenticated user ID
     * @param roles Current user's roles
     * @param request Category request DTO
     * @return CategoryResponse with created category data
     * @throws com.nexus.libs.error.exception.BusinessException if category name duplicate exists or unauthorized
     */
    CategoryResponse createCategory(
        UUID tenantId,
        UUID currentUserId,
        List<String> roles,
        CategoryRequest request
    );
    
    /**
     * Get category by ID
     * 
     * @param categoryId Category ID
     * @param tenantId Tenant ID from JWT claims
     * @return CategoryResponse if found
     * @throws com.nexus.libs.error.exception.BusinessException if category not found
     */
    CategoryResponse getCategoryById(UUID categoryId, UUID tenantId);
    
    /**
     * Get all categories for a tenant (flat list)
     * 
     * @param tenantId Tenant ID from JWT claims
     * @return List of CategoryResponse
     */
    List<CategoryResponse> getAllCategories(UUID tenantId);
    
    /**
     * Get category tree (hierarchical structure)
     * 
     * @param tenantId Tenant ID from JWT claims
     * @return List of CategoryTreeResponse (top-level categories with nested children)
     */
    List<CategoryTreeResponse> getCategoryTree(UUID tenantId);
    
    /**
     * Get child categories for a parent category
     * 
     * @param parentId Parent category ID
     * @param tenantId Tenant ID from JWT claims
     * @return List of CategoryResponse (child categories)
     */
    List<CategoryResponse> getChildCategories(UUID parentId, UUID tenantId);
    
    /**
     * Update category
     * 
     * @param categoryId Category ID
     * @param tenantId Tenant ID from JWT claims
     * @param currentUserId Currently authenticated user ID
     * @param roles Current user's roles
     * @param request Category request DTO with updated fields
     * @return CategoryResponse with updated category data
     * @throws com.nexus.libs.error.exception.BusinessException if category not found or unauthorized
     */
    CategoryResponse updateCategory(
        UUID categoryId,
        UUID tenantId,
        UUID currentUserId,
        List<String> roles,
        CategoryRequest request
    );
    
    /**
     * Delete category
     * 
     * @param categoryId Category ID
     * @param tenantId Tenant ID from JWT claims
     * @param currentUserId Currently authenticated user ID
     * @param roles Current user's roles
     * @throws com.nexus.libs.error.exception.BusinessException if category not found, has products, or unauthorized
     */
    void deleteCategory(
        UUID categoryId,
        UUID tenantId,
        UUID currentUserId,
        List<String> roles
    );
    
    /**
     * Check if user has permission to manage categories
     * Only SELLER and ADMIN can manage categories
     * 
     * @param roles Current user's roles
     * @return true if user can manage categories
     */
    boolean canManageCategories(List<String> roles);
}

