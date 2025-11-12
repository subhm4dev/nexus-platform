package com.nexus.ecommerce.fulfilment.repository;

import com.nexus.ecommerce.fulfilment.entity.AgeVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgeVerificationRepository extends JpaRepository<AgeVerification, UUID> {
    
    Optional<AgeVerification> findByDeliveryConfirmationId(UUID confirmationId);
    
    Optional<AgeVerification> findByDeliveryId(UUID deliveryId);
    
    List<AgeVerification> findByTenantIdAndVerificationStatus(
        UUID tenantId,
        AgeVerification.VerificationStatus status
    );
    
    @Query("SELECT av FROM AgeVerification av WHERE av.deliveryConfirmationId = :confirmationId " +
           "AND av.verificationStatus = 'VERIFIED' AND av.ageVerified = true")
    Optional<AgeVerification> findVerifiedByConfirmationId(@Param("confirmationId") UUID confirmationId);
    
    @Query("SELECT av FROM AgeVerification av WHERE av.deliveryId = :deliveryId " +
           "AND av.verificationStatus = 'VERIFIED' AND av.ageVerified = true")
    Optional<AgeVerification> findVerifiedByDeliveryId(@Param("deliveryId") UUID deliveryId);
}

