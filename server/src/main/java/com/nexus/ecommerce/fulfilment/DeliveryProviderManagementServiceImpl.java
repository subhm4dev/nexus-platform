package com.nexus.ecommerce.fulfilment.service.impl;

import com.nexus.ecommerce.fulfilment.entity.DeliveryProvider;
import com.nexus.ecommerce.fulfilment.model.request.CreateProviderRequest;
import com.nexus.ecommerce.fulfilment.model.request.UpdateProviderRequest;
import com.nexus.ecommerce.fulfilment.model.response.ProviderResponse;
import com.nexus.ecommerce.fulfilment.repository.DeliveryProviderRepository;
import com.nexus.ecommerce.fulfilment.service.DeliveryProviderManagementService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Delivery Provider Management Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryProviderManagementServiceImpl implements DeliveryProviderManagementService {
    
    private final DeliveryProviderRepository providerRepository;
    
    @Override
    @Transactional
    public ProviderResponse createProvider(UUID tenantId, CreateProviderRequest request) {
        log.info("Creating delivery provider: providerCode={}, tenantId={}", request.providerCode(), tenantId);
        
        // Check if provider already exists
        DeliveryProvider.ProviderCode providerCode;
        try {
            providerCode = DeliveryProvider.ProviderCode.valueOf(request.providerCode().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                ErrorCode.INVALID_OPERATION,
                "Invalid provider code: " + request.providerCode()
            );
        }
        
        providerRepository.findByProviderCodeAndTenantId(providerCode, tenantId)
            .ifPresent(p -> {
                throw new BusinessException(
                    ErrorCode.INVALID_OPERATION,
                    "Provider already exists: " + request.providerCode()
                );
            });
        
        DeliveryProvider provider = DeliveryProvider.builder()
            .tenantId(tenantId)
            .providerCode(providerCode)
            .providerName(request.providerName())
            .providerType(request.providerType())
            .isActive(true)
            .apiKey(request.apiKey())
            .apiSecret(request.apiSecret())
            .webhookSecret(request.webhookSecret())
            .baseUrl(request.baseUrl())
            .config(request.config())
            .createdAt(LocalDateTime.now())
            .build();
        
        DeliveryProvider saved = providerRepository.save(provider);
        log.info("Delivery provider created: id={}, providerCode={}", saved.getId(), saved.getProviderCode());
        
        return toResponse(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProviderResponse> getAllProviders(UUID tenantId) {
        return providerRepository.findByTenantIdAndIsActiveTrue(tenantId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ProviderResponse getProviderById(UUID id, UUID tenantId) {
        DeliveryProvider provider = providerRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Provider not found: " + id
            ));
        
        if (!provider.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Provider belongs to different tenant"
            );
        }
        
        return toResponse(provider);
    }
    
    @Override
    @Transactional
    public ProviderResponse updateProvider(UUID id, UUID tenantId, UpdateProviderRequest request) {
        log.info("Updating provider: id={}, tenantId={}", id, tenantId);
        
        DeliveryProvider provider = providerRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Provider not found: " + id
            ));
        
        if (!provider.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Provider belongs to different tenant"
            );
        }
        
        if (request.providerName() != null) {
            provider.setProviderName(request.providerName());
        }
        if (request.isActive() != null) {
            provider.setIsActive(request.isActive());
        }
        if (request.apiKey() != null) {
            provider.setApiKey(request.apiKey());
        }
        if (request.apiSecret() != null) {
            provider.setApiSecret(request.apiSecret());
        }
        if (request.webhookSecret() != null) {
            provider.setWebhookSecret(request.webhookSecret());
        }
        if (request.baseUrl() != null) {
            provider.setBaseUrl(request.baseUrl());
        }
        if (request.config() != null) {
            provider.setConfig(request.config());
        }
        
        provider.setUpdatedAt(LocalDateTime.now());
        DeliveryProvider saved = providerRepository.save(provider);
        
        log.info("Provider updated: id={}", saved.getId());
        return toResponse(saved);
    }
    
    @Override
    @Transactional
    public void deleteProvider(UUID id, UUID tenantId) {
        log.info("Deleting provider: id={}, tenantId={}", id, tenantId);
        
        DeliveryProvider provider = providerRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Provider not found: " + id
            ));
        
        if (!provider.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Provider belongs to different tenant"
            );
        }
        
        // Soft delete
        provider.setIsActive(false);
        provider.setUpdatedAt(LocalDateTime.now());
        providerRepository.save(provider);
        
        log.info("Provider deleted: id={}", id);
    }
    
    private ProviderResponse toResponse(DeliveryProvider provider) {
        return new ProviderResponse(
            provider.getId(),
            provider.getTenantId(),
            provider.getProviderCode().name(),
            provider.getProviderName(),
            provider.getProviderType(),
            provider.getIsActive(),
            provider.getBaseUrl(),
            provider.getConfig(),
            provider.getCreatedAt(),
            provider.getUpdatedAt()
        );
    }
}

