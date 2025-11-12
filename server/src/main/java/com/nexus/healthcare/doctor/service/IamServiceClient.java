package com.nexus.healthcare.doctor.service;

import com.nexus.shared.iam.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * IAM Service Client
 * 
 * <p>Handles communication with IAM service for tenant validation and user domain mapping updates.
 * In modulith architecture, uses direct service calls instead of HTTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IamServiceClient {
    
    private final TenantService tenantService;
    
    /**
     * Validate that target tenant is a BRANCH tenant under the same APP tenant as current tenant
     */
    public void validateBranchTransfer(UUID currentTenantId, UUID targetTenantId, UUID domainId, String jwtToken) {
        log.debug("Validating branch transfer: currentTenantId={}, targetTenantId={}", currentTenantId, targetTenantId);
        // Direct service call in modulith - no HTTP needed
        tenantService.validateBranchTransfer(currentTenantId, targetTenantId, domainId);
    }
    
    /**
     * Update user domain mapping in IAM service
     */
    public void updateUserDomainMapping(UUID userId, UUID newTenantId, UUID domainId, String jwtToken) {
        log.debug("Updating user domain mapping: userId={}, newTenantId={}, domainId={}", userId, newTenantId, domainId);
        try {
            // Direct service call in modulith - no HTTP needed
            tenantService.updateUserDomainMapping(userId, newTenantId, domainId);
            log.debug("User domain mapping updated successfully");
        } catch (Exception e) {
            log.warn("Failed to update user domain mapping (non-critical): {}", e.getMessage());
            // Non-critical - don't fail the transfer if this fails
        }
    }
}

