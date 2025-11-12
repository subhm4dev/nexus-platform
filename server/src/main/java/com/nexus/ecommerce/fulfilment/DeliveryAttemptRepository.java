package com.nexus.ecommerce.fulfilment.repository;

import com.nexus.ecommerce.fulfilment.entity.DeliveryAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, UUID> {
    
    List<DeliveryAttempt> findByDeliveryIdOrderByAttemptNumberDesc(UUID deliveryId);
    
    Optional<DeliveryAttempt> findFirstByDeliveryIdOrderByAttemptNumberDesc(UUID deliveryId);
    
    @Query("SELECT da FROM DeliveryAttempt da WHERE da.deliveryId = :deliveryId " +
           "AND da.attemptStatus = 'FAILED' ORDER BY da.attemptNumber DESC")
    List<DeliveryAttempt> findFailedAttemptsByDeliveryId(@Param("deliveryId") UUID deliveryId);
    
    @Query("SELECT COUNT(da) FROM DeliveryAttempt da WHERE da.deliveryId = :deliveryId")
    Integer countAttemptsByDeliveryId(@Param("deliveryId") UUID deliveryId);
    
    @Query("SELECT da FROM DeliveryAttempt da WHERE da.tenantId = :tenantId " +
           "AND da.nextAttemptAt IS NOT NULL AND da.nextAttemptAt <= :now " +
           "AND da.nextAttemptScheduled = true")
    List<DeliveryAttempt> findScheduledAttemptsForProcessing(
        @Param("tenantId") UUID tenantId,
        @Param("now") LocalDateTime now
    );
    
    List<DeliveryAttempt> findByTenantIdAndAttemptStatus(
        UUID tenantId,
        DeliveryAttempt.AttemptStatus status
    );
}

