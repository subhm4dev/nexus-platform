package com.nexus.ecommerce.fulfilment.model.response;

import com.nexus.ecommerce.fulfilment.entity.DeliveryProvider;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for delivery provider
 */
public record ProviderResponse(
    UUID id,
    
    @JsonProperty("tenant_id")
    UUID tenantId,
    
    @JsonProperty("provider_code")
    String providerCode,
    
    @JsonProperty("provider_name")
    String providerName,
    
    @JsonProperty("provider_type")
    DeliveryProvider.ProviderType providerType,
    
    @JsonProperty("is_active")
    Boolean isActive,
    
    @JsonProperty("base_url")
    String baseUrl,
    
    @JsonProperty("config")
    Map<String, Object> config,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {}

