package com.nexus.libs.jwt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * JWT Validation Properties
 * 
 * <p>Configuration properties for JWT validation.
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtValidationProperties {
    
    /**
     * JWKS URL for fetching public keys
     */
    private String jwksUrl = "http://localhost:8080/.well-known/jwks.json";
    
    /**
     * Cache refresh interval in milliseconds
     */
    private Long jwksCacheRefreshIntervalMs = 300000L; // 5 minutes
    
    /**
     * Public paths that don't require JWT validation
     */
    private List<String> publicPaths = new ArrayList<>();
    
    /**
     * Whether JWT validation is enabled
     */
    private Boolean enabled = true;
}

