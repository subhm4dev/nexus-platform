package com.nexus.healthcare.appointment.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.healthcare.appointment.entity.Availability;
import com.nexus.healthcare.appointment.model.request.AvailabilityRequest;
import com.nexus.healthcare.appointment.model.response.AvailabilityResponse;
import com.nexus.healthcare.appointment.repository.AvailabilityRepository;
import com.nexus.healthcare.appointment.service.AvailabilityService;
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
public class AvailabilityServiceImpl implements AvailabilityService {
    
    private final AvailabilityRepository availabilityRepository;
    
    @Override
    @Transactional
    public AvailabilityResponse createAvailability(UUID tenantId, UUID domainId, AvailabilityRequest request) {
        Availability availability = Availability.builder()
            .doctorId(request.getDoctorId())
            .dayOfWeek(request.getDayOfWeek())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .tenantId(tenantId)
            .domainId(domainId)
            .isActive(true)
            .build();
        
        Availability saved = availabilityRepository.save(availability);
        return mapToResponse(saved);
    }
    
    @Override
    public List<AvailabilityResponse> getAvailabilitiesByDoctor(UUID doctorId, UUID tenantId, UUID domainId) {
        return availabilityRepository.findByDoctorIdAndTenantIdAndDomainId(doctorId, tenantId, domainId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public AvailabilityResponse updateAvailability(UUID availabilityId, UUID tenantId, UUID domainId, AvailabilityRequest request) {
        Availability availability = availabilityRepository.findById(availabilityId)
            .filter(a -> a.getTenantId().equals(tenantId) && a.getDomainId().equals(domainId))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Availability not found"));
        
        if (request.getStartTime() != null) availability.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) availability.setEndTime(request.getEndTime());
        if (request.getDayOfWeek() != null) availability.setDayOfWeek(request.getDayOfWeek());
        
        Availability updated = availabilityRepository.save(availability);
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteAvailability(UUID availabilityId, UUID tenantId, UUID domainId) {
        Availability availability = availabilityRepository.findById(availabilityId)
            .filter(a -> a.getTenantId().equals(tenantId) && a.getDomainId().equals(domainId))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Availability not found"));
        
        availability.setIsActive(false);
        availabilityRepository.save(availability);
    }
    
    private AvailabilityResponse mapToResponse(Availability availability) {
        return AvailabilityResponse.builder()
            .id(availability.getId())
            .doctorId(availability.getDoctorId())
            .dayOfWeek(availability.getDayOfWeek())
            .startTime(availability.getStartTime())
            .endTime(availability.getEndTime())
            .isActive(availability.getIsActive())
            .createdAt(availability.getCreatedAt())
            .updatedAt(availability.getUpdatedAt())
            .build();
    }
}

