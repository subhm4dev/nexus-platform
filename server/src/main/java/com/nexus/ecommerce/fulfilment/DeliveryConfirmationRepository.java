package com.nexus.ecommerce.fulfilment.repository;

import com.nexus.ecommerce.fulfilment.entity.DeliveryConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Delivery Confirmation
 */
@Repository
public interface DeliveryConfirmationRepository extends JpaRepository<DeliveryConfirmation, UUID> {
    
    Optional<DeliveryConfirmation> findByDeliveryId(UUID deliveryId);
    
    Optional<DeliveryConfirmation> findByDeliveryIdAndTenantId(UUID deliveryId, UUID tenantId);
    
    List<DeliveryConfirmation> findByTenantIdAndConfirmationStatus(
        UUID tenantId, 
        DeliveryConfirmation.ConfirmationStatus status
    );
    
    @Query("SELECT dc FROM DeliveryConfirmation dc WHERE dc.tenantId = :tenantId " +
           "AND dc.confirmationStatus IN ('PENDING', 'AGENT_CONFIRMED', 'CUSTOMER_CONFIRMED') " +
           "AND dc.nextAttemptAt <= :now")
    List<DeliveryConfirmation> findPendingConfirmationsForReschedule(
        @Param("tenantId") UUID tenantId,
        @Param("now") LocalDateTime now
    );
    
    @Query("SELECT dc FROM DeliveryConfirmation dc WHERE dc.tenantId = :tenantId " +
           "AND dc.rescheduleCount >= 3 " +
           "AND dc.autoReturnInitiated = false")
    List<DeliveryConfirmation> findDeliveriesForAutoReturn(UUID tenantId);
}

