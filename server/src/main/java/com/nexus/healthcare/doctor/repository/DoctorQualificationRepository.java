package com.nexus.healthcare.doctor.repository;

import com.nexus.healthcare.doctor.entity.DoctorQualification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorQualificationRepository extends JpaRepository<DoctorQualification, UUID> {
    
    List<DoctorQualification> findByDoctorIdAndTenantIdAndDomainId(UUID doctorId, UUID tenantId, UUID domainId);
    
    /**
     * Get distinct qualification names from all qualifications
     * Used for populating dropdown in forms
     */
    @Query("SELECT DISTINCT q.name FROM DoctorQualification q ORDER BY q.name")
    List<String> findDistinctQualificationNames();
}

