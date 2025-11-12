package com.nexus.healthcare.doctor.service;

import com.nexus.healthcare.doctor.model.request.SpecializationRequest;
import com.nexus.healthcare.doctor.model.response.SpecializationResponse;

import java.util.List;
import java.util.UUID;

public interface SpecializationService {
    
    SpecializationResponse createSpecialization(SpecializationRequest request);
    
    List<SpecializationResponse> getAllActiveSpecializations();
    
    SpecializationResponse getSpecializationById(UUID id);
    
    SpecializationResponse updateSpecialization(UUID id, SpecializationRequest request);
    
    void deleteSpecialization(UUID id);
}

