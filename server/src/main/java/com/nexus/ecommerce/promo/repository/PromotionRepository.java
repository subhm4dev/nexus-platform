package com.nexus.ecommerce.promo.repository;

import com.nexus.ecommerce.promo.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Promotion entities
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    
    List<Promotion> findByTenantIdAndActiveTrue(UUID tenantId);
    
    @Query("SELECT p FROM Promotion p WHERE p.tenantId = :tenantId " +
           "AND p.active = true " +
           "AND p.startDate <= :now " +
           "AND p.endDate >= :now " +
           "ORDER BY p.priority DESC")
    List<Promotion> findActivePromotions(
        @Param("tenantId") UUID tenantId,
        @Param("now") LocalDateTime now
    );
}

