package com.nexus.shared.payment.repository;

import com.nexus.shared.payment.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Method Repository
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    
    List<PaymentMethod> findByUserIdAndDomainIdAndTenantId(UUID userId, UUID domainId, UUID tenantId);
    
    Optional<PaymentMethod> findByUserIdAndDomainIdAndTenantIdAndId(UUID userId, UUID domainId, UUID tenantId, UUID id);
    
    Optional<PaymentMethod> findByToken(String token);
}

