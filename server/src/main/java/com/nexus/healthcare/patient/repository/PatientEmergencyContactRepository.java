package com.nexus.healthcare.patient.repository;

import com.nexus.healthcare.patient.entity.PatientEmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientEmergencyContactRepository extends JpaRepository<PatientEmergencyContact, UUID> {
    List<PatientEmergencyContact> findByPatientIdAndTenantIdAndDomainId(UUID patientId, UUID tenantId, UUID domainId);
    List<PatientEmergencyContact> findByPatientId(UUID patientId);
}

