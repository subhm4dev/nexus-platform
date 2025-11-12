package com.nexus.shared.payment.repository;

import com.nexus.shared.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Payment Repository
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);
    
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);
    
    Page<Payment> findByUserIdAndDomainIdAndTenantId(UUID userId, UUID domainId, UUID tenantId, Pageable pageable);
    
    Page<Payment> findByOrderId(UUID orderId, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId AND p.domainId = :domainId AND p.tenantId = :tenantId " +
           "AND (:status IS NULL OR p.status = :status)")
    Page<Payment> findByUserIdAndDomainIdAndTenantIdAndStatus(
        @Param("userId") UUID userId,
        @Param("domainId") UUID domainId,
        @Param("tenantId") UUID tenantId,
        @Param("status") Payment.PaymentStatus status,
        Pageable pageable
    );
}

