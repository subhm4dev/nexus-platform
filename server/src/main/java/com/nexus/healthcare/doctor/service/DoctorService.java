package com.nexus.healthcare.doctor.service;

import com.nexus.healthcare.doctor.model.request.DoctorRequest;
import com.nexus.healthcare.doctor.model.response.DoctorResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface DoctorService {
    
    DoctorResponse createDoctor(UUID userId, UUID tenantId, UUID domainId, DoctorRequest request);
    
    DoctorResponse getDoctorById(UUID doctorId, UUID tenantId, UUID domainId);
    
    Page<DoctorResponse> searchDoctors(UUID tenantId, UUID domainId, String verificationStatus, String query, int page, int size);
    
    DoctorResponse updateDoctor(UUID doctorId, UUID tenantId, UUID domainId, DoctorRequest request);
    
    void deleteDoctor(UUID doctorId, UUID tenantId, UUID domainId);
    
    DoctorResponse verifyDoctor(UUID doctorId, UUID tenantId, UUID domainId, String verificationStatus);
    
    /**
     * Transfer doctor to another branch
     * Soft deletes doctor at current branch and creates new record at target branch
     * Transfers all qualifications, awards, and specializations
     */
    DoctorResponse transferDoctor(UUID doctorId, UUID currentTenantId, UUID targetTenantId, UUID domainId, String jwtToken);
    
    /**
     * Add doctor to another branch (keep at both branches)
     * Creates a new doctor record at target branch with same user_id
     * Useful when doctor works at multiple branches
     */
    DoctorResponse addDoctorToBranch(UUID doctorId, UUID currentTenantId, UUID targetTenantId, UUID domainId, String jwtToken);
    
    /**
     * Get distinct qualification names from all existing qualifications
     * Used for populating dropdowns in forms
     */
    List<String> getDistinctQualificationNames();
}

