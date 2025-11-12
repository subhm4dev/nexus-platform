package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.entity.DeliveryProvider;
import com.nexus.ecommerce.fulfilment.provider.DeliveryProviderService;

import java.util.UUID;

/**
 * Provider Selection Service
 * Selects the appropriate delivery provider based on delivery type and configuration
 */
public interface ProviderSelectionService {
    
    /**
     * Select the best provider for a delivery
     * 
     * @param tenantId Tenant ID
     * @param isIntercity true for intercity, false for intracity
     * @param preferredProviderCode Preferred provider code (optional)
     * @return Selected delivery provider
     */
    DeliveryProvider selectProvider(UUID tenantId, boolean isIntercity, String preferredProviderCode);
    
    /**
     * Get provider service implementation for a provider
     * 
     * @param providerCode Provider code
     * @return Provider service implementation
     */
    DeliveryProviderService getProviderService(String providerCode);
}

