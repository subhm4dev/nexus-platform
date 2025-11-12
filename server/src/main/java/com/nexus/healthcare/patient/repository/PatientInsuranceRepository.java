package com.nexus.healthcare.patient.repository;

import com.nexus.healthcare.patient.entity.PatientInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientInsuranceRepository extends JpaRepository<PatientInsurance, UUID> {
    List<PatientInsurance> findByPatientIdAndTenantIdAndDomainId(UUID patientId, UUID tenantId, UUID domainId);
    List<PatientInsurance> findByPatientId(UUID patientId);
}

