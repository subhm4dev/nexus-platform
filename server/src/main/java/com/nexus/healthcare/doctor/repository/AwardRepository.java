package com.nexus.healthcare.doctor.repository;

import com.nexus.healthcare.doctor.entity.Award;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AwardRepository extends JpaRepository<Award, UUID> {
    
    List<Award> findByDoctorIdAndTenantIdAndDomainId(UUID doctorId, UUID tenantId, UUID domainId);
}

