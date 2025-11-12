package com.nexus.ecommerce.fulfilment.repository;

import com.nexus.ecommerce.fulfilment.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Delivery Repository
 */
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    
    Optional<Delivery> findByTrackingNumberAndTenantId(String trackingNumber, UUID tenantId);
    
    @Query("SELECT d FROM Delivery d WHERE d.driverId = :driverId " +
           "AND d.tenantId = :tenantId " +
           "AND (:status IS NULL OR d.status = :status)")
    List<Delivery> findByDriverIdAndTenantIdAndStatus(
        @Param("driverId") UUID driverId,
        @Param("tenantId") UUID tenantId,
        @Param("status") Delivery.DeliveryStatus status
    );
    
    @Query("SELECT d FROM Delivery d WHERE d.fulfillmentId = :fulfillmentId " +
           "AND d.tenantId = :tenantId")
    Optional<Delivery> findByFulfillmentIdAndTenantId(
        @Param("fulfillmentId") UUID fulfillmentId,
        @Param("tenantId") UUID tenantId
    );
}

