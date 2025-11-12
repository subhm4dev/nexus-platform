package com.nexus.ecommerce.search.repository;

import com.nexus.ecommerce.search.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Recommendation Repository
 */
@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {
    
    @Query("SELECT r FROM Recommendation r WHERE " +
           "r.userId = :userId AND r.tenantId = :tenantId " +
           "ORDER BY r.score DESC")
    Page<Recommendation> findByUserIdAndTenantId(
        @Param("userId") UUID userId,
        @Param("tenantId") UUID tenantId,
        Pageable pageable
    );
    
    @Query("SELECT r FROM Recommendation r WHERE " +
           "r.tenantId = :tenantId AND r.type = :type " +
           "ORDER BY r.score DESC")
    Page<Recommendation> findTrendingByTenantId(
        @Param("tenantId") UUID tenantId,
        @Param("type") String type,
        Pageable pageable
    );
    
    @Query("SELECT r FROM Recommendation r WHERE " +
           "r.productId = :productId AND r.tenantId = :tenantId " +
           "AND r.type = :type " +
           "ORDER BY r.score DESC")
    List<Recommendation> findByProductIdAndType(
        @Param("productId") UUID productId,
        @Param("tenantId") UUID tenantId,
        @Param("type") String type
    );
    
    void deleteByProductIdAndTenantId(UUID productId, UUID tenantId);
}

