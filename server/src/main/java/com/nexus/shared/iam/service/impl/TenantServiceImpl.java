package com.nexus.shared.iam.service.impl;

import com.nexus.shared.iam.entity.Tenant;
import com.nexus.shared.iam.entity.UserDomain;
import com.nexus.shared.iam.repository.TenantRepository;
import com.nexus.shared.iam.repository.UserDomainRepository;
import com.nexus.shared.iam.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Tenant Service Implementation
 * 
 * <p>Provides tenant operations for use by other modules in the modulith.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {
    
    private final TenantRepository tenantRepository;
    private final UserDomainRepository userDomainRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Tenant getTenantById(UUID tenantId) {
        return tenantRepository.findById(tenantId)
            .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTenantInfo(UUID tenantId) {
        Tenant tenant = getTenantById(tenantId);
        Map<String, Object> info = new HashMap<>();
        info.put("id", tenant.getId().toString());
        info.put("name", tenant.getName());
        info.put("type", tenant.getType().toString());
        info.put("domainId", tenant.getDomainId() != null ? tenant.getDomainId().toString() : null);
        info.put("parentTenantId", tenant.getParentTenantId() != null ? tenant.getParentTenantId().toString() : null);
        info.put("status", tenant.getStatus() != null ? tenant.getStatus().toString() : null);
        return info;
    }
    
    @Override
    @Transactional
    public void updateUserDomainMapping(UUID userId, UUID newTenantId, UUID domainId) {
        log.debug("Updating user domain mapping: userId={}, newTenantId={}, domainId={}", userId, newTenantId, domainId);
        
        // Find existing user domain entry
        Optional<UserDomain> existing = userDomainRepository.findByUserIdAndDomainIdAndTenantId(userId, domainId, newTenantId);
        
        if (existing.isEmpty()) {
            // Create new entry
            UserDomain userDomain = UserDomain.builder()
                .userId(userId)
                .domainId(domainId)
                .tenantId(newTenantId)
                .build();
            userDomainRepository.save(userDomain);
            log.debug("Created new UserDomain entry: userId={}, domainId={}, tenantId={}", userId, domainId, newTenantId);
        } else {
            log.debug("UserDomain entry already exists: userId={}, domainId={}, tenantId={}", userId, domainId, newTenantId);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public void validateBranchTransfer(UUID currentTenantId, UUID targetTenantId, UUID domainId) {
        log.debug("Validating branch transfer: currentTenantId={}, targetTenantId={}", currentTenantId, targetTenantId);
        
        // Get current tenant
        Tenant currentTenant = getTenantById(currentTenantId);
        String currentTenantType = currentTenant.getType().toString();
        UUID currentParentTenantId = currentTenant.getParentTenantId() != null 
            ? currentTenant.getParentTenantId() 
            : currentTenantId; // If APP tenant, parent is itself
        
        // Get target tenant
        Tenant targetTenant = getTenantById(targetTenantId);
        String targetTenantType = targetTenant.getType().toString();
        UUID targetParentTenantId = targetTenant.getParentTenantId();
        
        // Validate: target must be BRANCH tenant
        if (!"BRANCH".equals(targetTenantType)) {
            throw new RuntimeException("Target tenant must be a BRANCH tenant");
        }
        
        // Validate: both tenants must be under same APP tenant
        UUID currentAppTenantId = "APP".equals(currentTenantType) ? currentTenantId : currentParentTenantId;
        UUID targetAppTenantId = targetParentTenantId != null ? targetParentTenantId : targetTenantId;
        
        // If target is BRANCH, get its parent (should be APP)
        if (targetParentTenantId != null) {
            Tenant targetParent = tenantRepository.findById(targetParentTenantId).orElse(null);
            if (targetParent != null && "APP".equals(targetParent.getType().toString())) {
                targetAppTenantId = targetParentTenantId;
            }
        }
        
        if (!currentAppTenantId.equals(targetAppTenantId)) {
            throw new RuntimeException("Target tenant must be under the same APP tenant as current tenant");
        }
        
        log.debug("Branch transfer validation successful");
    }
}

