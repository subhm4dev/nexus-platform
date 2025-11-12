package com.nexus.ecommerce.fulfilment.repository;

import com.nexus.ecommerce.fulfilment.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Driver Repository
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {
    
    List<Driver> findByTenantId(UUID tenantId);
    
    @Query("SELECT d FROM Driver d WHERE d.tenantId = :tenantId " +
           "AND (:status IS NULL OR d.status = :status)")
    List<Driver> findByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") Driver.DriverStatus status
    );
    
    Optional<Driver> findByPhoneAndTenantId(String phone, UUID tenantId);
}

