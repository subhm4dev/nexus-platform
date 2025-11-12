package com.nexus.healthcare.appointment.service;

import com.nexus.healthcare.appointment.model.request.TimeOffRequest;
import com.nexus.healthcare.appointment.model.response.TimeOffResponse;

import java.util.List;
import java.util.UUID;

public interface TimeOffService {
    
    TimeOffResponse createTimeOff(UUID tenantId, UUID domainId, TimeOffRequest request);
    
    List<TimeOffResponse> getTimeOffsByDoctor(UUID doctorId, UUID tenantId, UUID domainId);
    
    TimeOffResponse updateTimeOff(UUID timeOffId, UUID tenantId, UUID domainId, TimeOffRequest request);
    
    void deleteTimeOff(UUID timeOffId, UUID tenantId, UUID domainId);
}

