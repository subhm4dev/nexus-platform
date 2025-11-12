package com.nexus.healthcare.patient.repository;

import com.nexus.healthcare.patient.entity.PatientAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientAllergyRepository extends JpaRepository<PatientAllergy, UUID> {
    List<PatientAllergy> findByPatientIdAndTenantIdAndDomainId(UUID patientId, UUID tenantId, UUID domainId);
    List<PatientAllergy> findByPatientId(UUID patientId);
}

