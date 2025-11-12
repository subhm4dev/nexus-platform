package com.nexus.healthcare.session.repository;

import com.nexus.healthcare.session.entity.SessionOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionOfferingRepository extends JpaRepository<SessionOffering, UUID> {
    
    List<SessionOffering> findBySessionTypeIdAndTenantIdAndDomainIdAndIsActiveTrue(UUID sessionTypeId, UUID tenantId, UUID domainId);
    
    List<SessionOffering> findByDoctorIdAndTenantIdAndDomainIdAndIsActiveTrue(UUID doctorId, UUID tenantId, UUID domainId);
}

