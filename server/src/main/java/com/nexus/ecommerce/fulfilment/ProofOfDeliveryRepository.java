package com.nexus.ecommerce.fulfilment.repository;

import com.nexus.ecommerce.fulfilment.entity.ProofOfDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProofOfDeliveryRepository extends JpaRepository<ProofOfDelivery, UUID> {
    
    Optional<ProofOfDelivery> findByDeliveryId(UUID deliveryId);
    
    Optional<ProofOfDelivery> findByDeliveryConfirmationId(UUID confirmationId);
    
    List<ProofOfDelivery> findByFulfillmentId(UUID fulfillmentId);
    
    List<ProofOfDelivery> findByTenantIdAndPodStatus(
        UUID tenantId,
        ProofOfDelivery.PODStatus status
    );
    
    @Query("SELECT pod FROM ProofOfDelivery pod WHERE pod.tenantId = :tenantId " +
           "AND pod.podStatus = 'MANUAL_REVIEW'")
    List<ProofOfDelivery> findPendingReview(@Param("tenantId") UUID tenantId);
}

