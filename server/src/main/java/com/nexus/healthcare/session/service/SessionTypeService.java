package com.nexus.healthcare.session.service;

import com.nexus.healthcare.session.model.request.SessionTypeRequest;
import com.nexus.healthcare.session.model.response.SessionTypeResponse;

import java.util.List;
import java.util.UUID;

public interface SessionTypeService {
    
    SessionTypeResponse createSessionType(UUID tenantId, UUID domainId, SessionTypeRequest request);
    
    List<SessionTypeResponse> getActiveSessionTypes(UUID tenantId, UUID domainId);
    
    SessionTypeResponse getSessionTypeById(UUID id, UUID tenantId, UUID domainId);
    
    SessionTypeResponse updateSessionType(UUID id, UUID tenantId, UUID domainId, SessionTypeRequest request);
    
    void deleteSessionType(UUID id, UUID tenantId, UUID domainId);
}

