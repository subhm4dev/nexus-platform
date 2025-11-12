package com.nexus.ecommerce.promo.repository;

import com.nexus.ecommerce.promo.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Coupon entities
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    
    Optional<Coupon> findByCode(String code);
    
    Optional<Coupon> findByCodeAndTenantId(String code, UUID tenantId);
    
    boolean existsByCode(String code);
}

