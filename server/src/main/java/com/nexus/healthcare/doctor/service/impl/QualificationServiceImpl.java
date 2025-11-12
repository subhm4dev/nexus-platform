package com.nexus.healthcare.doctor.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.healthcare.doctor.entity.QualificationType;
import com.nexus.healthcare.doctor.model.request.QualificationRequest;
import com.nexus.healthcare.doctor.model.response.QualificationResponse;
import com.nexus.healthcare.doctor.repository.QualificationTypeRepository;
import com.nexus.healthcare.doctor.service.QualificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QualificationServiceImpl implements QualificationService {
    
    private final QualificationTypeRepository qualificationTypeRepository;
    
    @Override
    @Transactional
    public QualificationResponse createQualification(QualificationRequest request) {
        if (qualificationTypeRepository.findByCode(request.getCode()).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Qualification with code already exists");
        }
        
        QualificationType qualificationType = QualificationType.builder()
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .active(request.getActive() != null ? request.getActive() : true)
            .build();
        
        QualificationType saved = qualificationTypeRepository.save(qualificationType);
        return mapToResponse(saved);
    }
    
    @Override
    public List<QualificationResponse> getAllActiveQualifications() {
        return qualificationTypeRepository.findByActiveTrue()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public QualificationResponse getQualificationById(UUID id) {
        QualificationType qualificationType = qualificationTypeRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Qualification not found"));
        return mapToResponse(qualificationType);
    }
    
    @Override
    @Transactional
    public QualificationResponse updateQualification(UUID id, QualificationRequest request) {
        QualificationType qualificationType = qualificationTypeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Qualification not found"));
        
        if (request.getName() != null) {
            qualificationType.setName(request.getName());
        }
        if (request.getDescription() != null) {
            qualificationType.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            qualificationType.setActive(request.getActive());
        }
        
        QualificationType updated = qualificationTypeRepository.save(qualificationType);
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteQualification(UUID id) {
        QualificationType qualificationType = qualificationTypeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Qualification not found"));
        
        qualificationType.setActive(false);
        qualificationTypeRepository.save(qualificationType);
    }
    
    private QualificationResponse mapToResponse(QualificationType qualificationType) {
        return QualificationResponse.builder()
            .id(qualificationType.getId())
            .code(qualificationType.getCode())
            .name(qualificationType.getName())
            .description(qualificationType.getDescription())
            .active(qualificationType.getActive())
            .createdAt(qualificationType.getCreatedAt())
            .updatedAt(qualificationType.getUpdatedAt())
            .build();
    }
}

