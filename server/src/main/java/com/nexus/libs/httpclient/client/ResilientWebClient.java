package com.nexus.libs.httpclient.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Resilient WebClient Builder
 * 
 * <p>Provides a WebClient for service-to-service communication.
 * In modulith, this is mainly for external services or can be replaced
 * with direct service calls.
 */
@Component
public class ResilientWebClient {
    
    /**
     * Create a WebClient for a specific service
     */
    public WebClient create(String serviceName, String baseUrl) {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }
}

