package com.nexus.healthcare.session.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.healthcare.session.entity.SessionType;
import com.nexus.healthcare.session.model.request.SessionTypeRequest;
import com.nexus.healthcare.session.model.response.SessionTypeResponse;
import com.nexus.healthcare.session.repository.SessionTypeRepository;
import com.nexus.healthcare.session.service.SessionTypeService;
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
public class SessionTypeServiceImpl implements SessionTypeService {
    
    private final SessionTypeRepository sessionTypeRepository;
    
    @Override
    @Transactional
    public SessionTypeResponse createSessionType(UUID tenantId, UUID domainId, SessionTypeRequest request) {
        SessionType sessionType = SessionType.builder()
            .name(request.getName())
            .description(request.getDescription())
            .durationMinutes(request.getDurationMinutes())
            .tenantId(tenantId)
            .domainId(domainId)
            .isActive(true)
            .build();
        
        SessionType saved = sessionTypeRepository.save(sessionType);
        return mapToResponse(saved);
    }
    
    @Override
    public List<SessionTypeResponse> getActiveSessionTypes(UUID tenantId, UUID domainId) {
        return sessionTypeRepository.findByTenantIdAndDomainIdAndIsActiveTrue(tenantId, domainId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public SessionTypeResponse getSessionTypeById(UUID id, UUID tenantId, UUID domainId) {
        SessionType sessionType = sessionTypeRepository.findByIdAndTenantIdAndDomainIdAndIsActiveTrue(id, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Session type not found"));
        return mapToResponse(sessionType);
    }
    
    @Override
    @Transactional
    public SessionTypeResponse updateSessionType(UUID id, UUID tenantId, UUID domainId, SessionTypeRequest request) {
        SessionType sessionType = sessionTypeRepository.findByIdAndTenantIdAndDomainIdAndIsActiveTrue(id, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Session type not found"));
        
        if (request.getName() != null) sessionType.setName(request.getName());
        if (request.getDescription() != null) sessionType.setDescription(request.getDescription());
        if (request.getDurationMinutes() != null) sessionType.setDurationMinutes(request.getDurationMinutes());
        
        SessionType updated = sessionTypeRepository.save(sessionType);
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteSessionType(UUID id, UUID tenantId, UUID domainId) {
        SessionType sessionType = sessionTypeRepository.findByIdAndTenantIdAndDomainIdAndIsActiveTrue(id, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Session type not found"));
        
        sessionType.setIsActive(false);
        sessionTypeRepository.save(sessionType);
    }
    
    private SessionTypeResponse mapToResponse(SessionType sessionType) {
        return SessionTypeResponse.builder()
            .id(sessionType.getId())
            .name(sessionType.getName())
            .description(sessionType.getDescription())
            .durationMinutes(sessionType.getDurationMinutes())
            .isActive(sessionType.getIsActive())
            .createdAt(sessionType.getCreatedAt())
            .updatedAt(sessionType.getUpdatedAt())
            .build();
    }
}

