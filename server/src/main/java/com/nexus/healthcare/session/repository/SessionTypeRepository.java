package com.nexus.healthcare.session.repository;

import com.nexus.healthcare.session.entity.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionTypeRepository extends JpaRepository<SessionType, UUID> {
    
    List<SessionType> findByTenantIdAndDomainIdAndIsActiveTrue(UUID tenantId, UUID domainId);
    
    Optional<SessionType> findByIdAndTenantIdAndDomainIdAndIsActiveTrue(UUID id, UUID tenantId, UUID domainId);
}

