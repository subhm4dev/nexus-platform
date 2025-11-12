package com.nexus.ecommerce.fulfilment.service.impl;

import com.nexus.ecommerce.fulfilment.entity.DeliveryProvider;
import com.nexus.ecommerce.fulfilment.provider.DeliveryProviderService;
import com.nexus.ecommerce.fulfilment.provider.impl.*;
import com.nexus.ecommerce.fulfilment.repository.DeliveryProviderRepository;
import com.nexus.ecommerce.fulfilment.service.ProviderSelectionService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Provider Selection Service Implementation
 */
@Service
@Slf4j
public class ProviderSelectionServiceImpl implements ProviderSelectionService {
    
    private final DeliveryProviderRepository providerRepository;
    private final Map<String, DeliveryProviderService> providerServices;
    
    public ProviderSelectionServiceImpl(
        DeliveryProviderRepository providerRepository,
        OwnFleetProviderService ownFleetProviderService,
        BlueDartProviderService blueDartProviderService,
        DelhiveryProviderService delhiveryProviderService,
        ShiprocketProviderService shiprocketProviderService,
        DunzoProviderService dunzoProviderService,
        RapidoProviderService rapidoProviderService
    ) {
        this.providerRepository = providerRepository;
        this.providerServices = Map.of(
            "OWN_FLEET", ownFleetProviderService,
            "BLUEDART", blueDartProviderService,
            "DELHIVERY", delhiveryProviderService,
            "SHIPROCKET", shiprocketProviderService,
            "DUNZO", dunzoProviderService,
            "RAPIDO", rapidoProviderService
        );
    }
    
    @Override
    public DeliveryProvider selectProvider(UUID tenantId, boolean isIntercity, String preferredProviderCode) {
        log.debug("Selecting provider: tenantId={}, isIntercity={}, preferred={}", 
            tenantId, isIntercity, preferredProviderCode);
        
        // If preferred provider is specified, try to use it
        if (preferredProviderCode != null && !preferredProviderCode.isEmpty()) {
            DeliveryProvider.ProviderCode providerCode;
            try {
                providerCode = DeliveryProvider.ProviderCode.valueOf(preferredProviderCode.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid provider code: {}", preferredProviderCode);
                // Fall through to default selection
            }
            
            Optional<DeliveryProvider> preferred = providerRepository.findByProviderCodeAndTenantId(
                DeliveryProvider.ProviderCode.valueOf(preferredProviderCode.toUpperCase()),
                tenantId
            );
            
            if (preferred.isPresent() && preferred.get().getIsActive()) {
                DeliveryProviderService service = getProviderService(preferred.get().getProviderCode().name());
                if (service != null && service.supportsDeliveryType(isIntercity)) {
                    log.info("Using preferred provider: {}", preferredProviderCode);
                    return preferred.get();
                }
            }
        }
        
        // Get all active providers for this delivery type
        List<DeliveryProvider> availableProviders = providerRepository.findActiveProvidersByType(
            tenantId, isIntercity
        );
        
        if (availableProviders.isEmpty()) {
            // Fallback to OWN_FLEET if no providers configured
            log.warn("No active providers found, defaulting to OWN_FLEET");
            return providerRepository.findByProviderCodeAndTenantId(
                DeliveryProvider.ProviderCode.OWN_FLEET, tenantId
            ).orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "No delivery provider configured for tenant: " + tenantId
            ));
        }
        
        // Select provider based on priority/cost (for now, select first available)
        // TODO: Implement intelligent selection based on cost, SLA, reliability
        DeliveryProvider selected = availableProviders.get(0);
        log.info("Selected provider: {} for delivery type: {}", 
            selected.getProviderCode(), isIntercity ? "INTERCITY" : "INTRACITY");
        
        return selected;
    }
    
    @Override
    public DeliveryProviderService getProviderService(String providerCode) {
        DeliveryProviderService service = providerServices.get(providerCode.toUpperCase());
        if (service == null) {
            log.warn("Provider service not found for code: {}", providerCode);
            // Fallback to OWN_FLEET
            return providerServices.get("OWN_FLEET");
        }
        return service;
    }
}

