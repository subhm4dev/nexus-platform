package com.nexus.ecommerce.catalog.repository;

import com.nexus.ecommerce.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Product entity
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    /**
     * Find product by SKU and tenant ID (for SKU uniqueness check)
     * 
     * @param sku SKU
     * @param tenantId Tenant ID
     * @return Optional Product
     */
    Optional<Product> findBySkuAndTenantId(String sku, UUID tenantId);
    
    /**
     * Find active product by ID and tenant ID
     * 
     * @param id Product ID
     * @param tenantId Tenant ID
     * @return Optional Product (only if not deleted)
     */
    Optional<Product> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);
    
    /**
     * Find product by ID (including deleted)
     * Used by admins for recovery/audit
     * 
     * @param id Product ID
     * @return Optional Product (may be deleted)
     */
    @Override
    @NonNull
    Optional<Product> findById(@NonNull UUID id);
    
    /**
     * Find all active products for a seller within a tenant
     * 
     * @param sellerId Seller ID
     * @param tenantId Tenant ID
     * @return List of active products
     */
    List<Product> findBySellerIdAndTenantIdAndDeletedFalse(UUID sellerId, UUID tenantId);
    
    /**
     * Search products with filters
     * 
     * @param tenantId Tenant ID
     * @param categoryId Optional category ID
     * @param minPrice Optional minimum price
     * @param maxPrice Optional maximum price
     * @param query Optional search query (name or description)
     * @param pageable Pagination
     * @return Page of products
     */
    @Query(value = "SELECT p.* FROM products p WHERE " +
           "p.tenant_id = :tenantId AND " +
           "p.deleted = false AND " +
           "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:query IS NULL OR LOWER(CAST(p.name AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(p.description IS NOT NULL AND LOWER(CAST(p.description AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))))",
           nativeQuery = true,
           countQuery = "SELECT COUNT(*) FROM products p WHERE " +
           "p.tenant_id = :tenantId AND " +
           "p.deleted = false AND " +
           "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:query IS NULL OR LOWER(CAST(p.name AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(p.description IS NOT NULL AND LOWER(CAST(p.description AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))))")
    Page<Product> searchProducts(
        @Param("tenantId") UUID tenantId,
        @Param("categoryId") UUID categoryId,
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice,
        @Param("query") String query,
        Pageable pageable
    );
    
    /**
     * Find all active products by category
     * 
     * @param categoryId Category ID
     * @param tenantId Tenant ID
     * @param pageable Pagination
     * @return Page of products
     */
    Page<Product> findByCategoryIdAndTenantIdAndDeletedFalse(
        UUID categoryId,
        UUID tenantId,
        Pageable pageable
    );
    
    /**
     * Count products by category (for validation before category deletion)
     * 
     * @param tenantId Tenant ID
     * @param categoryId Category ID
     * @return Count of active products in this category
     */
    long countByTenantIdAndCategoryIdAndDeletedFalse(UUID tenantId, UUID categoryId);
    
    /**
     * Find all active products for multiple tenants (for tenant hierarchy)
     * 
     * @param tenantIds List of tenant IDs (parent + children)
     * @return List of active products
     */
    List<Product> findByTenantIdInAndDeletedFalse(List<UUID> tenantIds);
    
    /**
     * Find products by seller and multiple tenant IDs
     * 
     * @param sellerId Seller ID
     * @param tenantIds List of tenant IDs
     * @return List of active products
     */
    List<Product> findBySellerIdAndTenantIdInAndDeletedFalse(UUID sellerId, List<UUID> tenantIds);
    
    /**
     * Search products with tenant hierarchy support
     * 
     * @param tenantIds List of tenant IDs (parent + children)
     * @param categoryId Optional category ID
     * @param minPrice Optional minimum price
     * @param maxPrice Optional maximum price
     * @param query Optional search query
     * @param pageable Pagination
     * @return Page of products
     */
    @Query(value = "SELECT p.* FROM products p WHERE " +
           "p.tenant_id IN :tenantIds AND " +
           "p.deleted = false AND " +
           "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:query IS NULL OR LOWER(CAST(p.name AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(p.description IS NOT NULL AND LOWER(CAST(p.description AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))))",
           nativeQuery = true,
           countQuery = "SELECT COUNT(*) FROM products p WHERE " +
           "p.tenant_id IN :tenantIds AND " +
           "p.deleted = false AND " +
           "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:query IS NULL OR LOWER(CAST(p.name AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(p.description IS NOT NULL AND LOWER(CAST(p.description AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))))")
    Page<Product> searchProductsWithTenantHierarchy(
        @Param("tenantIds") List<UUID> tenantIds,
        @Param("categoryId") UUID categoryId,
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice,
        @Param("query") String query,
        Pageable pageable
    );
    
    /**
     * Find all products by parent tenant ID (seller ownership)
     * Used to get all products owned by a seller (across all branches)
     * 
     * @param parentTenantId SELLER tenant ID
     * @return List of products owned by the seller
     */
    List<Product> findByParentTenantId(UUID parentTenantId);
    
    /**
     * Find active products by parent tenant ID
     * 
     * @param parentTenantId SELLER tenant ID
     * @return List of active products owned by the seller
     */
    List<Product> findByParentTenantIdAndDeletedFalse(UUID parentTenantId);
    
    /**
     * Find products by parent tenant IDs (for tenant hierarchy)
     * Used when seller has multiple SELLER tenants or for app-level queries
     * 
     * @param parentTenantIds List of SELLER tenant IDs
     * @return List of products
     */
    List<Product> findByParentTenantIdIn(List<UUID> parentTenantIds);
    
    /**
     * Find active products by parent tenant IDs
     * 
     * @param parentTenantIds List of SELLER tenant IDs
     * @return List of active products
     */
    List<Product> findByParentTenantIdInAndDeletedFalse(List<UUID> parentTenantIds);
    
    /**
     * Search products by parent tenant ID (seller ownership)
     * 
     * @param parentTenantId SELLER tenant ID
     * @param categoryId Optional category ID
     * @param minPrice Optional minimum price
     * @param maxPrice Optional maximum price
     * @param query Optional search query
     * @param pageable Pagination
     * @return Page of products
     */
    @Query(value = "SELECT p.* FROM products p WHERE " +
           "p.parent_tenant_id = :parentTenantId AND " +
           "p.deleted = false AND " +
           "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:query IS NULL OR LOWER(CAST(p.name AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(p.description IS NOT NULL AND LOWER(CAST(p.description AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))))",
           nativeQuery = true,
           countQuery = "SELECT COUNT(*) FROM products p WHERE " +
           "p.parent_tenant_id = :parentTenantId AND " +
           "p.deleted = false AND " +
           "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:query IS NULL OR LOWER(CAST(p.name AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "(p.description IS NOT NULL AND LOWER(CAST(p.description AS TEXT)) LIKE LOWER(CONCAT('%', :query, '%'))))")
    Page<Product> searchProductsByParentTenant(
        @Param("parentTenantId") UUID parentTenantId,
        @Param("categoryId") UUID categoryId,
        @Param("minPrice") java.math.BigDecimal minPrice,
        @Param("maxPrice") java.math.BigDecimal maxPrice,
        @Param("query") String query,
        Pageable pageable
    );
}

