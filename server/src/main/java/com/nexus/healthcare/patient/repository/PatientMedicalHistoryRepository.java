package com.nexus.healthcare.patient.repository;

import com.nexus.healthcare.patient.entity.PatientMedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientMedicalHistoryRepository extends JpaRepository<PatientMedicalHistory, UUID> {
    List<PatientMedicalHistory> findByPatientIdAndTenantIdAndDomainId(UUID patientId, UUID tenantId, UUID domainId);
    List<PatientMedicalHistory> findByPatientId(UUID patientId);
}

