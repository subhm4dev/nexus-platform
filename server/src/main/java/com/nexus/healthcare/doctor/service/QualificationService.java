package com.nexus.healthcare.doctor.service;

import com.nexus.healthcare.doctor.model.request.QualificationRequest;
import com.nexus.healthcare.doctor.model.response.QualificationResponse;

import java.util.List;
import java.util.UUID;

public interface QualificationService {
    
    QualificationResponse createQualification(QualificationRequest request);
    
    List<QualificationResponse> getAllActiveQualifications();
    
    QualificationResponse getQualificationById(UUID id);
    
    QualificationResponse updateQualification(UUID id, QualificationRequest request);
    
    void deleteQualification(UUID id);
}

