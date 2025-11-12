package com.nexus.ecommerce.catalog.controller;

import com.nexus.ecommerce.catalog.model.request.CategoryRequest;
import com.nexus.ecommerce.catalog.model.response.CategoryResponse;
import com.nexus.ecommerce.catalog.model.response.CategoryTreeResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.catalog.service.CategoryService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Category Controller
 * 
 * <p>This controller manages product categories with hierarchical support.
 * Categories help organize products and enable better browsing experience.
 */
@RestController
@RequestMapping("/api/v1/category")
@Tag(name = "Category Management", description = "Category CRUD and hierarchy endpoints")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Create a new category
     * 
     * <p>Access control: Only SELLER and ADMIN can create categories.
     */
    @PostMapping
    @Operation(
        summary = "Create a new category",
        description = "Creates a new category. Supports hierarchical structure via parentId."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest categoryRequest,
            Authentication authentication) {
        
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        log.info("Creating category for tenant: {}", tenantId);
        
        CategoryResponse response = categoryService.createCategory(
            tenantId,
            currentUserId,
            roles,
            categoryRequest
        );
        
        return ApiResponse.success(response, "Category created successfully");
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{categoryId}")
    @Operation(
        summary = "Get category by ID",
        description = "Retrieves category details including children IDs"
    )
    public ApiResponse<CategoryResponse> getCategory(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) UUID tenantId, // Optional tenant ID for public access
            Authentication authentication) {
        
        // Extract tenant ID: priority: query param > JWT > default
        if (tenantId == null && authentication != null) {
            tenantId = getTenantIdFromAuthentication(authentication);
        }
        
        if (tenantId == null) {
            tenantId = getDefaultTenantId();
            if (tenantId == null) {
                throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    "Tenant ID is required. Please provide 'tenantId' as a query parameter or authenticate."
                );
            }
        }
        
        log.info("Getting category {} for tenant: {}", categoryId, tenantId);
        
        CategoryResponse response = categoryService.getCategoryById(categoryId, tenantId);
        
        return ApiResponse.success(response, "Category retrieved successfully");
    }

    /**
     * Get all categories (flat list)
     */
    @GetMapping
    @Operation(
        summary = "Get all categories",
        description = "Returns a flat list of all categories for the tenant"
    )
    public ApiResponse<List<CategoryResponse>> getAllCategories(
            @RequestParam(required = false) UUID tenantId, // Optional tenant ID for public access
            Authentication authentication) {
        
        // Extract tenant ID: priority: query param > JWT > default
        if (tenantId == null && authentication != null) {
            tenantId = getTenantIdFromAuthentication(authentication);
        }
        
        if (tenantId == null) {
            tenantId = getDefaultTenantId();
            if (tenantId == null) {
                throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    "Tenant ID is required. Please provide 'tenantId' as a query parameter or authenticate."
                );
            }
        }
        
        log.info("Getting all categories for tenant: {}", tenantId);
        
        List<CategoryResponse> response = categoryService.getAllCategories(tenantId);
        
        return ApiResponse.success(response, "Categories retrieved successfully");
    }

    /**
     * Get category tree (hierarchical structure)
     */
    @GetMapping("/tree")
    @Operation(
        summary = "Get category tree",
        description = "Returns hierarchical category structure with nested children"
    )
    public ApiResponse<List<CategoryTreeResponse>> getCategoryTree(
            @RequestParam(required = false) UUID tenantId, // Optional tenant ID for public access
            Authentication authentication) {
        
        // Extract tenant ID: priority: query param > JWT > default
        if (tenantId == null && authentication != null) {
            tenantId = getTenantIdFromAuthentication(authentication);
        }
        
        if (tenantId == null) {
            tenantId = getDefaultTenantId();
            if (tenantId == null) {
                throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    "Tenant ID is required. Please provide 'tenantId' as a query parameter or authenticate."
                );
            }
        }
        
        log.info("Getting category tree for tenant: {}", tenantId);
        
        List<CategoryTreeResponse> response = categoryService.getCategoryTree(tenantId);
        
        return ApiResponse.success(response, "Category tree retrieved successfully");
    }

    /**
     * Get child categories for a parent
     */
    @GetMapping("/{parentId}/children")
    @Operation(
        summary = "Get child categories",
        description = "Returns all child categories for a given parent category"
    )
    public ApiResponse<List<CategoryResponse>> getChildCategories(
            @PathVariable UUID parentId,
            @RequestParam(required = false) UUID tenantId, // Optional tenant ID for public access
            Authentication authentication) {
        
        // Extract tenant ID: priority: query param > JWT > default
        if (tenantId == null && authentication != null) {
            tenantId = getTenantIdFromAuthentication(authentication);
        }
        
        if (tenantId == null) {
            tenantId = getDefaultTenantId();
            if (tenantId == null) {
                throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    "Tenant ID is required. Please provide 'tenantId' as a query parameter or authenticate."
                );
            }
        }
        
        log.info("Getting children of category {} for tenant: {}", parentId, tenantId);
        
        List<CategoryResponse> response = categoryService.getChildCategories(parentId, tenantId);
        
        return ApiResponse.success(response, "Child categories retrieved successfully");
    }

    /**
     * Update category
     * 
     * <p>Access control: Only SELLER and ADMIN can update categories.
     */
    @PutMapping("/{categoryId}")
    @Operation(
        summary = "Update category",
        description = "Updates category information. Only SELLER and ADMIN can update."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequest categoryRequest,
            Authentication authentication) {
        
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        log.info("Updating category {} for tenant: {}", categoryId, tenantId);
        
        CategoryResponse response = categoryService.updateCategory(
            categoryId,
            tenantId,
            currentUserId,
            roles,
            categoryRequest
        );
        
        return ApiResponse.success(response, "Category updated successfully");
    }

    /**
     * Delete category
     * 
     * <p>Access control: Only SELLER and ADMIN can delete categories.
     * Cannot delete category if it has products or child categories.
     */
    @DeleteMapping("/{categoryId}")
    @Operation(
        summary = "Delete category",
        description = "Deletes a category. Cannot delete if category has products or child categories."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID categoryId,
            Authentication authentication) {
        
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        log.info("Deleting category {} for tenant: {}", categoryId, tenantId);
        
        categoryService.deleteCategory(
            categoryId,
            tenantId,
            currentUserId,
            roles
        );
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "User ID is required. Please ensure you are authenticated."
            );
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        String userIdStr = jwtAuth.getUserId();
        
        try {
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format in JWT: {}", userIdStr);
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Invalid user ID format"
            );
        }
    }

    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Tenant ID is required. Please ensure you are authenticated."
            );
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        String tenantIdStr = jwtAuth.getTenantId();
        
        try {
            return UUID.fromString(tenantIdStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid tenant ID format in JWT: {}", tenantIdStr);
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Invalid tenant ID format"
            );
        }
    }

    private List<String> getRolesFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            return List.of();
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        return jwtAuth.getRoles();
    }

    /**
     * Get default tenant ID for public browsing
     * 
     * <p>For public access without authentication, we use the default marketplace tenant.
     * This is the same tenant ID used for customers in the Identity service.
     */
    private UUID getDefaultTenantId() {
        // Default marketplace tenant ID (same as in Identity service)
        // This is the tenant where all customers belong by default
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}

