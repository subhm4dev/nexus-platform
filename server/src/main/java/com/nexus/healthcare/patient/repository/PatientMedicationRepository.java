package com.nexus.healthcare.patient.repository;

import com.nexus.healthcare.patient.entity.PatientMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientMedicationRepository extends JpaRepository<PatientMedication, UUID> {
    List<PatientMedication> findByPatientIdAndTenantIdAndDomainId(UUID patientId, UUID tenantId, UUID domainId);
    List<PatientMedication> findByPatientId(UUID patientId);
}

