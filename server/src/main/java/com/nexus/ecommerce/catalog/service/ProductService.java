package com.nexus.ecommerce.catalog.service;

import com.nexus.ecommerce.catalog.model.request.ProductRequest;
import com.nexus.ecommerce.catalog.model.response.ProductResponse;
import com.nexus.ecommerce.catalog.model.response.ProductSearchResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for product operations
 */
public interface ProductService {
    
    /**
     * Create a new product
     * 
     * @param sellerId Seller ID (user ID who created this product)
     * @param tenantId Tenant ID from JWT claims
     * @param currentUserId Currently authenticated user ID
     * @param roles Current user's roles
     * @param request Product request DTO
     * @return ProductResponse with created product data
     * @throws com.nexus.libs.error.exception.BusinessException if SKU duplicate exists or unauthorized
     */
    ProductResponse createProduct(
        UUID sellerId,
        UUID tenantId,
        UUID currentUserId,
        List<String> roles,
        ProductRequest request
    );
    
    /**
     * Get product by ID
     * 
     * @param productId Product ID
     * @param tenantId Tenant ID from JWT claims (optional for public access)
     * @return ProductResponse if found
     * @throws com.nexus.libs.error.exception.BusinessException if product not found
     */
    ProductResponse getProductById(UUID productId, UUID tenantId);
    
    /**
     * Search products with filters
     * 
     * @param tenantId Tenant ID from JWT claims (optional for public access)
     * @param query Optional search query (name or description)
     * @param categoryId Optional category ID
     * @param minPrice Optional minimum price
     * @param maxPrice Optional maximum price
     * @param page Page number (0-based)
     * @param size Page size
     * @return ProductSearchResponse with paginated results
     */
    ProductSearchResponse searchProducts(
        UUID tenantId,
        String query,
        UUID categoryId,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        int page,
        int size
    );
    
    /**
     * Update product
     * 
     * @param productId Product ID
     * @param currentUserId Currently authenticated user ID
     * @param tenantId Tenant ID from JWT claims
     * @param roles Current user's roles
     * @param request Product request DTO with updated fields
     * @return ProductResponse with updated product data
     * @throws com.nexus.libs.error.exception.BusinessException if product not found or unauthorized
     */
    ProductResponse updateProduct(
        UUID productId,
        UUID currentUserId,
        UUID tenantId,
        List<String> roles,
        ProductRequest request
    );
    
    /**
     * Soft delete a product
     * 
     * @param productId Product ID
     * @param currentUserId Currently authenticated user ID
     * @param tenantId Tenant ID from JWT claims
     * @param roles Current user's roles
     * @throws com.nexus.libs.error.exception.BusinessException if product not found or unauthorized
     */
    void deleteProduct(
        UUID productId,
        UUID currentUserId,
        UUID tenantId,
        List<String> roles
    );
    
    /**
     * Check if user can access/modify a product
     * Users can modify their own products, admins can modify any product
     * 
     * @param currentUserId Currently authenticated user ID
     * @param sellerId Product owner's user ID
     * @param roles Current user's roles
     * @return true if access is allowed
     */
    boolean canAccessProduct(UUID currentUserId, UUID sellerId, List<String> roles);
}

