package com.nexus.healthcare.consultation.service;

import com.nexus.healthcare.consultation.model.request.ConsultationRequest;
import com.nexus.healthcare.consultation.model.response.ConsultationResponse;

import java.util.List;
import java.util.UUID;

public interface ConsultationService {
    
    ConsultationResponse createConsultation(UUID userId, UUID tenantId, UUID domainId, ConsultationRequest request);
    
    ConsultationResponse getConsultationById(UUID consultationId, UUID tenantId, UUID domainId);
    
    ConsultationResponse getConsultationByAppointmentId(UUID appointmentId, UUID tenantId, UUID domainId);
    
    List<ConsultationResponse> getConsultationsByPatient(UUID patientId, UUID tenantId, UUID domainId);
    
    List<ConsultationResponse> getConsultationsByDoctor(UUID doctorId, UUID tenantId, UUID domainId);
    
    ConsultationResponse updateConsultation(UUID consultationId, UUID tenantId, UUID domainId, ConsultationRequest request);
    
    boolean existsByAppointmentId(UUID appointmentId);
}

