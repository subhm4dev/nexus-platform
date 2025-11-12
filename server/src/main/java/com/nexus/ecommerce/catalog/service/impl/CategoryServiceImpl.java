package com.nexus.ecommerce.catalog.service.impl;

import com.nexus.ecommerce.catalog.entity.Category;
import com.nexus.ecommerce.catalog.model.request.CategoryRequest;
import com.nexus.ecommerce.catalog.model.response.CategoryResponse;
import com.nexus.ecommerce.catalog.model.response.CategoryTreeResponse;
import com.nexus.ecommerce.catalog.repository.CategoryRepository;
import com.nexus.ecommerce.catalog.repository.ProductRepository;
import com.nexus.ecommerce.catalog.service.CategoryService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CategoryService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(
            UUID tenantId,
            UUID currentUserId,
            List<String> roles,
            CategoryRequest request) {
        
        if (!canManageCategories(roles)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Only SELLER and ADMIN can create categories"
            );
        }

        // Check if category name already exists for this tenant
        if (categoryRepository.findByNameAndTenantId(request.name(), tenantId).isPresent()) {
            throw new BusinessException(
                ErrorCode.CATEGORY_NAME_ALREADY_EXISTS,
                "Category name already exists for this tenant"
            );
        }

        // Validate parent category if provided
        if (request.parentId() != null) {
            categoryRepository.findByIdAndTenantId(request.parentId(), tenantId)
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.CATEGORY_NOT_FOUND,
                    "Parent category not found"
                ));
        }

        Category category = Category.builder()
            .name(request.name())
            .description(request.description())
            .parentId(request.parentId())
            .tenantId(tenantId)
            .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category: {} for tenant: {}", saved.getId(), tenantId);

        return toCategoryResponse(saved);
    }

    @Override
    public CategoryResponse getCategoryById(UUID categoryId, UUID tenantId) {
        Category category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.CATEGORY_NOT_FOUND,
                "Category not found"
            ));

        return toCategoryResponse(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories(UUID tenantId) {
        // For tenant hierarchy support, expand tenantId to include children
        // For now, use single tenantId (backward compatible)
        // Controllers can expand hierarchy and call getAllCategoriesWithHierarchy
        List<Category> categories = categoryRepository.findByTenantId(tenantId);
        return categories.stream()
            .map(this::toCategoryResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all categories for multiple tenants (tenant hierarchy support)
     * Used when tenant has child branches (e.g., seller managing multiple branches)
     * 
     * @param tenantIds List of tenant IDs (parent + children)
     * @return List of categories from all tenants
     */
    public List<CategoryResponse> getAllCategoriesWithHierarchy(List<UUID> tenantIds) {
        List<Category> categories = categoryRepository.findByTenantIdIn(tenantIds);
        return categories.stream()
            .map(this::toCategoryResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<CategoryTreeResponse> getCategoryTree(UUID tenantId) {
        // For tenant hierarchy support, use getCategoryTreeWithHierarchy
        List<Category> allCategories = categoryRepository.findByTenantId(tenantId);
        
        // Build a map of parent ID to children
        Map<UUID, List<Category>> childrenMap = allCategories.stream()
            .filter(cat -> cat.getParentId() != null)
            .collect(Collectors.groupingBy(Category::getParentId));

        // Get top-level categories (no parent)
        List<Category> topLevel = allCategories.stream()
            .filter(cat -> cat.getParentId() == null)
            .collect(Collectors.toList());

        // Build tree recursively
        return topLevel.stream()
            .map(cat -> buildCategoryTree(cat, childrenMap))
            .collect(Collectors.toList());
    }
    
    /**
     * Get category tree for multiple tenants (tenant hierarchy support)
     * 
     * @param tenantIds List of tenant IDs (parent + children)
     * @return List of category trees from all tenants
     */
    public List<CategoryTreeResponse> getCategoryTreeWithHierarchy(List<UUID> tenantIds) {
        List<Category> allCategories = categoryRepository.findByTenantIdIn(tenantIds);
        
        // Build a map of parent ID to children
        Map<UUID, List<Category>> childrenMap = allCategories.stream()
            .filter(cat -> cat.getParentId() != null)
            .collect(Collectors.groupingBy(Category::getParentId));

        // Get top-level categories (no parent)
        List<Category> topLevel = allCategories.stream()
            .filter(cat -> cat.getParentId() == null)
            .collect(Collectors.toList());

        // Build tree recursively
        return topLevel.stream()
            .map(cat -> buildCategoryTree(cat, childrenMap))
            .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getChildCategories(UUID parentId, UUID tenantId) {
        // Verify parent exists
        categoryRepository.findByIdAndTenantId(parentId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.CATEGORY_NOT_FOUND,
                "Parent category not found"
            ));

        List<Category> children = categoryRepository.findByParentIdAndTenantId(parentId, tenantId);
        return children.stream()
            .map(this::toCategoryResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(
            UUID categoryId,
            UUID tenantId,
            UUID currentUserId,
            List<String> roles,
            CategoryRequest request) {
        
        if (!canManageCategories(roles)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Only SELLER and ADMIN can update categories"
            );
        }

        Category category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.CATEGORY_NOT_FOUND,
                "Category not found"
            ));

        // Check if name changed and if new name already exists
        if (!category.getName().equals(request.name())) {
            categoryRepository.findByNameAndTenantId(request.name(), tenantId)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(categoryId)) {
                        throw new BusinessException(
                            ErrorCode.CATEGORY_NAME_ALREADY_EXISTS,
                            "Category name already exists for this tenant"
                        );
                    }
                });
        }

        // Validate parent category if provided
        if (request.parentId() != null && !request.parentId().equals(category.getParentId())) {
            // Prevent circular reference
            if (request.parentId().equals(categoryId)) {
                throw new BusinessException(
                    ErrorCode.INVALID_PARENT_CATEGORY,
                    "Category cannot be its own parent"
                );
            }
            
            categoryRepository.findByIdAndTenantId(request.parentId(), tenantId)
                .orElseThrow(() -> new BusinessException(
                    ErrorCode.CATEGORY_NOT_FOUND,
                    "Parent category not found"
                ));
        }

        category.setName(request.name());
        category.setDescription(request.description());
        category.setParentId(request.parentId());

        Category updated = categoryRepository.save(category);
        log.info("Updated category: {} for tenant: {}", categoryId, tenantId);

        return toCategoryResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(
            UUID categoryId,
            UUID tenantId,
            UUID currentUserId,
            List<String> roles) {
        
        if (!canManageCategories(roles)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Only SELLER and ADMIN can delete categories"
            );
        }

        Category category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.CATEGORY_NOT_FOUND,
                "Category not found"
            ));

        // Check if category has products
        long productCount = productRepository.countByTenantIdAndCategoryIdAndDeletedFalse(tenantId, categoryId);
        if (productCount > 0) {
            throw new BusinessException(
                ErrorCode.CATEGORY_HAS_PRODUCTS,
                "Cannot delete category with associated products"
            );
        }

        // Check if category has children
        List<Category> children = categoryRepository.findByParentIdAndTenantId(categoryId, tenantId);
        if (!children.isEmpty()) {
            throw new BusinessException(
                ErrorCode.CATEGORY_HAS_CHILDREN,
                "Cannot delete category with child categories. Please delete or reassign child categories first."
            );
        }

        categoryRepository.delete(category);
        log.info("Deleted category: {} for tenant: {}", categoryId, tenantId);
    }

    @Override
    public boolean canManageCategories(List<String> roles) {
        return roles.contains("SELLER") || roles.contains("ADMIN");
    }

    private CategoryResponse toCategoryResponse(Category category) {
        List<UUID> childrenIds = categoryRepository.findByParentIdAndTenantId(category.getId(), category.getTenantId())
            .stream()
            .map(Category::getId)
            .collect(Collectors.toList());

        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getParentId(),
            category.getTenantId(),
            childrenIds,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }

    private CategoryTreeResponse buildCategoryTree(Category category, Map<UUID, List<Category>> childrenMap) {
        List<Category> children = childrenMap.getOrDefault(category.getId(), new ArrayList<>());
        
        List<CategoryTreeResponse> childTrees = children.stream()
            .map(child -> buildCategoryTree(child, childrenMap))
            .collect(Collectors.toList());

        return new CategoryTreeResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getParentId(),
            category.getTenantId(),
            childTrees,
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}

