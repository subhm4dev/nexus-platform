package com.nexus.ecommerce.fulfilment.repository;

import com.nexus.ecommerce.fulfilment.entity.AlternateRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlternateRecipientRepository extends JpaRepository<AlternateRecipient, UUID> {
    
    /**
     * Find by share token (for link access)
     */
    Optional<AlternateRecipient> findByShareToken(String shareToken);
    
    /**
     * Find all active alternate recipients for a delivery
     */
    @Query("SELECT ar FROM AlternateRecipient ar WHERE ar.deliveryId = :deliveryId " +
           "AND ar.status IN ('PENDING', 'ACTIVE') " +
           "AND (ar.expiresAt IS NULL OR ar.expiresAt > :now) " +
           "AND ar.revokedAt IS NULL")
    List<AlternateRecipient> findActiveByDeliveryId(
        @Param("deliveryId") UUID deliveryId,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Find all alternate recipients for a delivery
     */
    List<AlternateRecipient> findByDeliveryId(UUID deliveryId);
    
    /**
     * Find by phone number and delivery
     */
    Optional<AlternateRecipient> findByAlternatePhoneNumberAndDeliveryId(
        String phoneNumber,
        UUID deliveryId
    );
    
    /**
     * Find by user ID and delivery
     */
    Optional<AlternateRecipient> findByAlternateUserIdAndDeliveryId(
        UUID userId,
        UUID deliveryId
    );
    
    /**
     * Find expired recipients
     */
    @Query("SELECT ar FROM AlternateRecipient ar WHERE ar.status = 'ACTIVE' " +
           "AND ar.expiresAt IS NOT NULL AND ar.expiresAt <= :now")
    List<AlternateRecipient> findExpiredRecipients(@Param("now") LocalDateTime now);
    
    /**
     * Find by customer user ID
     */
    List<AlternateRecipient> findByCustomerUserId(UUID customerUserId);
}

