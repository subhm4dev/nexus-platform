package com.nexus.ecommerce.fulfilment.model.request;

import com.nexus.ecommerce.fulfilment.entity.DeliveryProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request DTO for creating a delivery provider
 */
public record CreateProviderRequest(
    @NotBlank
    @JsonProperty("provider_code")
    String providerCode,  // BLUEDART, DELHIVERY, SHIPROCKET, DUNZO, RAPIDO, OWN_FLEET
    
    @NotBlank
    @JsonProperty("provider_name")
    String providerName,
    
    @NotNull
    @JsonProperty("provider_type")
    DeliveryProvider.ProviderType providerType,  // INTERCITY, INTRACITY, OWN_FLEET
    
    @JsonProperty("api_key")
    String apiKey,
    
    @JsonProperty("api_secret")
    String apiSecret,
    
    @JsonProperty("webhook_secret")
    String webhookSecret,
    
    @JsonProperty("base_url")
    String baseUrl,
    
    @JsonProperty("config")
    Map<String, Object> config  // Provider-specific configuration
) {}

