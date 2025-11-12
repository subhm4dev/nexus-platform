package com.nexus.healthcare.doctor.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.healthcare.doctor.entity.Specialization;
import com.nexus.healthcare.doctor.model.request.SpecializationRequest;
import com.nexus.healthcare.doctor.model.response.SpecializationResponse;
import com.nexus.healthcare.doctor.repository.SpecializationRepository;
import com.nexus.healthcare.doctor.service.SpecializationService;
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
public class SpecializationServiceImpl implements SpecializationService {
    
    private final SpecializationRepository specializationRepository;
    
    @Override
    @Transactional
    public SpecializationResponse createSpecialization(SpecializationRequest request) {
        if (specializationRepository.findByCode(request.getCode()).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Specialization with code already exists");
        }
        
        Specialization specialization = Specialization.builder()
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .active(request.getActive() != null ? request.getActive() : true)
            .build();
        
        Specialization saved = specializationRepository.save(specialization);
        return mapToResponse(saved);
    }
    
    @Override
    public List<SpecializationResponse> getAllActiveSpecializations() {
        return specializationRepository.findByActiveTrue()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public SpecializationResponse getSpecializationById(UUID id) {
        Specialization specialization = specializationRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Specialization not found"));
        return mapToResponse(specialization);
    }
    
    @Override
    @Transactional
    public SpecializationResponse updateSpecialization(UUID id, SpecializationRequest request) {
        Specialization specialization = specializationRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Specialization not found"));
        
        if (request.getName() != null) {
            specialization.setName(request.getName());
        }
        if (request.getDescription() != null) {
            specialization.setDescription(request.getDescription());
        }
        if (request.getActive() != null) {
            specialization.setActive(request.getActive());
        }
        
        Specialization updated = specializationRepository.save(specialization);
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteSpecialization(UUID id) {
        Specialization specialization = specializationRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Specialization not found"));
        
        specialization.setActive(false);
        specializationRepository.save(specialization);
    }
    
    private SpecializationResponse mapToResponse(Specialization specialization) {
        return SpecializationResponse.builder()
            .id(specialization.getId())
            .code(specialization.getCode())
            .name(specialization.getName())
            .description(specialization.getDescription())
            .active(specialization.getActive())
            .createdAt(specialization.getCreatedAt())
            .updatedAt(specialization.getUpdatedAt())
            .build();
    }
}

