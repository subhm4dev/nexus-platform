package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.model.request.CreateProviderRequest;
import com.nexus.ecommerce.fulfilment.model.request.UpdateProviderRequest;
import com.nexus.ecommerce.fulfilment.model.response.ProviderResponse;

import java.util.List;
import java.util.UUID;

/**
 * Delivery Provider Management Service Interface
 * Manages delivery provider configurations (CRUD operations)
 */
public interface DeliveryProviderManagementService {
    
    ProviderResponse createProvider(UUID tenantId, CreateProviderRequest request);
    
    List<ProviderResponse> getAllProviders(UUID tenantId);
    
    ProviderResponse getProviderById(UUID id, UUID tenantId);
    
    ProviderResponse updateProvider(UUID id, UUID tenantId, UpdateProviderRequest request);
    
    void deleteProvider(UUID id, UUID tenantId);
}

