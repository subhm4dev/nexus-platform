package com.nexus.healthcare.appointment.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.healthcare.appointment.entity.TimeOff;
import com.nexus.healthcare.appointment.model.request.TimeOffRequest;
import com.nexus.healthcare.appointment.model.response.TimeOffResponse;
import com.nexus.healthcare.appointment.repository.TimeOffRepository;
import com.nexus.healthcare.appointment.service.TimeOffService;
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
public class TimeOffServiceImpl implements TimeOffService {
    
    private final TimeOffRepository timeOffRepository;
    
    @Override
    @Transactional
    public TimeOffResponse createTimeOff(UUID tenantId, UUID domainId, TimeOffRequest request) {
        TimeOff timeOff = TimeOff.builder()
            .doctorId(request.getDoctorId())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .reason(request.getReason())
            .tenantId(tenantId)
            .domainId(domainId)
            .build();
        
        TimeOff saved = timeOffRepository.save(timeOff);
        return mapToResponse(saved);
    }
    
    @Override
    public List<TimeOffResponse> getTimeOffsByDoctor(UUID doctorId, UUID tenantId, UUID domainId) {
        return timeOffRepository.findByDoctorIdAndTenantIdAndDomainId(doctorId, tenantId, domainId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public TimeOffResponse updateTimeOff(UUID timeOffId, UUID tenantId, UUID domainId, TimeOffRequest request) {
        TimeOff timeOff = timeOffRepository.findById(timeOffId)
            .filter(t -> t.getTenantId().equals(tenantId) && t.getDomainId().equals(domainId))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Time-off not found"));
        
        if (request.getStartTime() != null) timeOff.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) timeOff.setEndTime(request.getEndTime());
        if (request.getReason() != null) timeOff.setReason(request.getReason());
        
        TimeOff updated = timeOffRepository.save(timeOff);
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteTimeOff(UUID timeOffId, UUID tenantId, UUID domainId) {
        TimeOff timeOff = timeOffRepository.findById(timeOffId)
            .filter(t -> t.getTenantId().equals(tenantId) && t.getDomainId().equals(domainId))
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Time-off not found"));
        
        timeOffRepository.delete(timeOff);
    }
    
    private TimeOffResponse mapToResponse(TimeOff timeOff) {
        return TimeOffResponse.builder()
            .id(timeOff.getId())
            .doctorId(timeOff.getDoctorId())
            .startTime(timeOff.getStartTime())
            .endTime(timeOff.getEndTime())
            .reason(timeOff.getReason())
            .createdAt(timeOff.getCreatedAt())
            .updatedAt(timeOff.getUpdatedAt())
            .build();
    }
}

