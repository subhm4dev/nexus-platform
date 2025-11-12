package com.nexus.ecommerce.search.repository;

import com.nexus.ecommerce.search.entity.SearchIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Search Index Repository
 */
@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, UUID> {
    
    Optional<SearchIndex> findByProductIdAndTenantId(UUID productId, UUID tenantId);
    
    List<SearchIndex> findByTenantId(UUID tenantId);
    
    /**
     * Full-text search using PostgreSQL tsvector (native query)
     */
    @Query(value = """
        SELECT * FROM search_index si 
        WHERE si.tenant_id = :tenantId 
        AND (:query IS NULL OR si.search_vector @@ plainto_tsquery('english', :query))
        AND (:categoryId IS NULL OR si.category_id = :categoryId)
        AND (:subcategoryId IS NULL OR si.subcategory_id = :subcategoryId)
        ORDER BY ts_rank(si.search_vector, plainto_tsquery('english', COALESCE(:query, ''))) DESC
        """, 
        countQuery = """
        SELECT COUNT(*) FROM search_index si 
        WHERE si.tenant_id = :tenantId 
        AND (:query IS NULL OR si.search_vector @@ plainto_tsquery('english', :query))
        AND (:categoryId IS NULL OR si.category_id = :categoryId)
        AND (:subcategoryId IS NULL OR si.subcategory_id = :subcategoryId)
        """,
        nativeQuery = true)
    Page<SearchIndex> search(
        @Param("tenantId") UUID tenantId,
        @Param("query") String query,
        @Param("categoryId") UUID categoryId,
        @Param("subcategoryId") UUID subcategoryId,
        Pageable pageable
    );
    
    /**
     * Simple text search (fallback if full-text search not available)
     */
    @Query("SELECT si FROM SearchIndex si WHERE " +
           "si.tenantId = :tenantId AND " +
           "(:query IS NULL OR LOWER(si.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(si.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(si.sku) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:categoryId IS NULL OR si.categoryId = :categoryId) AND " +
           "(:subcategoryId IS NULL OR si.subcategoryId = :subcategoryId)")
    Page<SearchIndex> searchSimple(
        @Param("tenantId") UUID tenantId,
        @Param("query") String query,
        @Param("categoryId") UUID categoryId,
        @Param("subcategoryId") UUID subcategoryId,
        Pageable pageable
    );
    
    /**
     * Search by category
     */
    Page<SearchIndex> findByCategoryIdAndTenantId(UUID categoryId, UUID tenantId, Pageable pageable);
    
    /**
     * Search by subcategory
     */
    Page<SearchIndex> findBySubcategoryIdAndTenantId(UUID subcategoryId, UUID tenantId, Pageable pageable);
    
    /**
     * Autocomplete suggestions
     */
    @Query("SELECT DISTINCT si.name FROM SearchIndex si WHERE " +
           "si.tenantId = :tenantId AND " +
           "LOWER(si.name) LIKE LOWER(CONCAT(:prefix, '%')) " +
           "ORDER BY si.name LIMIT :limit")
    List<String> findAutocompleteSuggestions(
        @Param("tenantId") UUID tenantId,
        @Param("prefix") String prefix,
        @Param("limit") int limit
    );
    
    /**
     * Delete by product ID
     */
    void deleteByProductIdAndTenantId(UUID productId, UUID tenantId);
    
    /**
     * Find search indices for multiple tenants (for tenant hierarchy)
     * 
     * @param tenantIds List of tenant IDs (parent + children)
     * @return List of search indices
     */
    List<SearchIndex> findByTenantIdIn(List<UUID> tenantIds);
    
    /**
     * Full-text search with tenant hierarchy support
     */
    @Query(value = """
        SELECT * FROM search_index si 
        WHERE si.tenant_id IN :tenantIds 
        AND (:query IS NULL OR si.search_vector @@ plainto_tsquery('english', :query))
        AND (:categoryId IS NULL OR si.category_id = :categoryId)
        AND (:subcategoryId IS NULL OR si.subcategory_id = :subcategoryId)
        ORDER BY ts_rank(si.search_vector, plainto_tsquery('english', COALESCE(:query, ''))) DESC
        """, 
        countQuery = """
        SELECT COUNT(*) FROM search_index si 
        WHERE si.tenant_id IN :tenantIds 
        AND (:query IS NULL OR si.search_vector @@ plainto_tsquery('english', :query))
        AND (:categoryId IS NULL OR si.category_id = :categoryId)
        AND (:subcategoryId IS NULL OR si.subcategory_id = :subcategoryId)
        """,
        nativeQuery = true)
    Page<SearchIndex> searchWithTenantHierarchy(
        @Param("tenantIds") List<UUID> tenantIds,
        @Param("query") String query,
        @Param("categoryId") UUID categoryId,
        @Param("subcategoryId") UUID subcategoryId,
        Pageable pageable
    );
    
    /**
     * Simple text search with tenant hierarchy support
     */
    @Query("SELECT si FROM SearchIndex si WHERE " +
           "si.tenantId IN :tenantIds AND " +
           "(:query IS NULL OR LOWER(si.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(si.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(si.sku) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:categoryId IS NULL OR si.categoryId = :categoryId) AND " +
           "(:subcategoryId IS NULL OR si.subcategoryId = :subcategoryId)")
    Page<SearchIndex> searchSimpleWithTenantHierarchy(
        @Param("tenantIds") List<UUID> tenantIds,
        @Param("query") String query,
        @Param("categoryId") UUID categoryId,
        @Param("subcategoryId") UUID subcategoryId,
        Pageable pageable
    );
}

