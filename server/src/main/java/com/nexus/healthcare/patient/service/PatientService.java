package com.nexus.healthcare.patient.service;

import com.nexus.healthcare.patient.model.request.PatientRequest;
import com.nexus.healthcare.patient.model.response.PatientResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PatientService {
    
    PatientResponse createPatient(UUID userId, UUID tenantId, UUID domainId, PatientRequest request);
    
    PatientResponse getPatientById(UUID patientId, UUID tenantId, UUID domainId);
    
    Page<PatientResponse> searchPatients(UUID tenantId, UUID domainId, String query, int page, int size);
    
    PatientResponse updatePatient(UUID patientId, UUID tenantId, UUID domainId, PatientRequest request);
    
    void deletePatient(UUID patientId, UUID tenantId, UUID domainId);
    
    /**
     * Transfer patient to another branch
     * Updates patient's tenant_id to target branch
     * Transfers all medical history, allergies, medications, insurance, emergency contacts
     */
    PatientResponse transferPatient(UUID patientId, UUID currentTenantId, UUID targetTenantId, UUID domainId, String jwtToken);
    
    /**
     * Add patient to another branch (keep at both branches)
     * Creates a new patient record at target branch with same user_id
     * Useful when patient visits multiple branches
     */
    PatientResponse addPatientToBranch(UUID patientId, UUID currentTenantId, UUID targetTenantId, UUID domainId, String jwtToken);
}

