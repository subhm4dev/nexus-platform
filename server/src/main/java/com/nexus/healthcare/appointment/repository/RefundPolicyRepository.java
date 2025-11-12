package com.nexus.healthcare.appointment.repository;

import com.nexus.healthcare.appointment.entity.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, UUID> {
    
    Optional<RefundPolicy> findByTenantIdAndDomainIdAndPolicyTypeAndIsActiveTrue(
        UUID tenantId, UUID domainId, String policyType);
    
    List<RefundPolicy> findByTenantIdAndDomainIdAndIsActiveTrue(UUID tenantId, UUID domainId);
}

