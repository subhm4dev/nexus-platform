package com.nexus.ecommerce.fulfilment.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Request DTO for updating a delivery provider
 */
public record UpdateProviderRequest(
    @JsonProperty("provider_name")
    String providerName,
    
    @JsonProperty("is_active")
    Boolean isActive,
    
    @JsonProperty("api_key")
    String apiKey,
    
    @JsonProperty("api_secret")
    String apiSecret,
    
    @JsonProperty("webhook_secret")
    String webhookSecret,
    
    @JsonProperty("base_url")
    String baseUrl,
    
    @JsonProperty("config")
    Map<String, Object> config
) {}

