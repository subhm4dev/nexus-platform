package com.nexus.healthcare.consultation.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.healthcare.consultation.entity.Consultation;
import com.nexus.healthcare.consultation.model.request.ConsultationRequest;
import com.nexus.healthcare.consultation.model.response.ConsultationResponse;
import com.nexus.healthcare.consultation.repository.ConsultationRepository;
import com.nexus.healthcare.consultation.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationServiceImpl implements ConsultationService {
    
    private final ConsultationRepository consultationRepository;
    
    @Override
    @Transactional
    public ConsultationResponse createConsultation(UUID userId, UUID tenantId, UUID domainId, ConsultationRequest request) {
        log.info("Creating consultation for appointment: {}, tenant: {}", request.getAppointmentId(), tenantId);
        
        consultationRepository.findByAppointmentId(request.getAppointmentId())
            .ifPresent(consultation -> {
                throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Consultation already exists for this appointment");
            });
        
        Consultation consultation = Consultation.builder()
            .appointmentId(request.getAppointmentId())
            .doctorId(request.getDoctorId())
            .patientId(request.getPatientId())
            .consultationDate(request.getConsultationDate() != null ? request.getConsultationDate() : LocalDateTime.now())
            .diagnosis(request.getDiagnosis())
            .notes(request.getNotes())
            .tenantId(tenantId)
            .domainId(domainId)
            .build();
        
        Consultation saved = consultationRepository.save(consultation);
        return mapToResponse(saved);
    }
    
    @Override
    public ConsultationResponse getConsultationById(UUID consultationId, UUID tenantId, UUID domainId) {
        Consultation consultation = consultationRepository.findById(consultationId)
            .filter(c -> c.getTenantId().equals(tenantId) && c.getDomainId().equals(domainId))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Consultation not found"));
        return mapToResponse(consultation);
    }
    
    @Override
    public ConsultationResponse getConsultationByAppointmentId(UUID appointmentId, UUID tenantId, UUID domainId) {
        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
            .filter(c -> c.getTenantId().equals(tenantId) && c.getDomainId().equals(domainId))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Consultation not found for this appointment"));
        return mapToResponse(consultation);
    }
    
    @Override
    public List<ConsultationResponse> getConsultationsByPatient(UUID patientId, UUID tenantId, UUID domainId) {
        List<Consultation> consultations = consultationRepository.findByPatientIdAndTenantIdAndDomainId(patientId, tenantId, domainId);
        return consultations.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    @Override
    public List<ConsultationResponse> getConsultationsByDoctor(UUID doctorId, UUID tenantId, UUID domainId) {
        List<Consultation> consultations = consultationRepository.findByDoctorIdAndTenantIdAndDomainId(doctorId, tenantId, domainId);
        return consultations.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ConsultationResponse updateConsultation(UUID consultationId, UUID tenantId, UUID domainId, ConsultationRequest request) {
        Consultation consultation = consultationRepository.findById(consultationId)
            .filter(c -> c.getTenantId().equals(tenantId) && c.getDomainId().equals(domainId))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Consultation not found"));
        
        if (request.getDiagnosis() != null) consultation.setDiagnosis(request.getDiagnosis());
        if (request.getNotes() != null) consultation.setNotes(request.getNotes());
        
        Consultation updated = consultationRepository.save(consultation);
        return mapToResponse(updated);
    }
    
    @Override
    public boolean existsByAppointmentId(UUID appointmentId) {
        return consultationRepository.existsByAppointmentId(appointmentId);
    }
    
    private ConsultationResponse mapToResponse(Consultation consultation) {
        return ConsultationResponse.builder()
            .id(consultation.getId())
            .appointmentId(consultation.getAppointmentId())
            .doctorId(consultation.getDoctorId())
            .patientId(consultation.getPatientId())
            .consultationDate(consultation.getConsultationDate())
            .diagnosis(consultation.getDiagnosis())
            .notes(consultation.getNotes())
            .createdAt(consultation.getCreatedAt())
            .updatedAt(consultation.getUpdatedAt())
            .build();
    }
}

