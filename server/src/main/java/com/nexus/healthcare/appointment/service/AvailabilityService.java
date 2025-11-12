package com.nexus.healthcare.appointment.service;

import com.nexus.healthcare.appointment.model.request.AvailabilityRequest;
import com.nexus.healthcare.appointment.model.response.AvailabilityResponse;

import java.util.List;
import java.util.UUID;

public interface AvailabilityService {
    
    AvailabilityResponse createAvailability(UUID tenantId, UUID domainId, AvailabilityRequest request);
    
    List<AvailabilityResponse> getAvailabilitiesByDoctor(UUID doctorId, UUID tenantId, UUID domainId);
    
    AvailabilityResponse updateAvailability(UUID availabilityId, UUID tenantId, UUID domainId, AvailabilityRequest request);
    
    void deleteAvailability(UUID availabilityId, UUID tenantId, UUID domainId);
}

