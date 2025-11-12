package com.nexus.libs.jwt.blocking;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.UUID;

/**
 * Blocking JWT Validation Service
 * 
 * <p>Validates JWT tokens for Spring MVC (blocking) services.
 */
@Service
public class BlockingJwtValidationService {
    
    /**
     * Validate token and return claims
     */
    public JWTClaimsSet validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            // Basic validation - in production, verify signature with JWKS
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
    
    /**
     * Extract user ID from claims
     */
    public String extractUserId(JWTClaimsSet claims) {
        Object userId = claims.getClaim("userId");
        return userId != null ? userId.toString() : null;
    }
    
    /**
     * Extract tenant ID from claims
     */
    public String extractTenantId(JWTClaimsSet claims) {
        Object tenantId = claims.getClaim("tenantId");
        return tenantId != null ? tenantId.toString() : null;
    }
    
    /**
     * Extract domain ID from claims
     */
    public String extractDomainId(JWTClaimsSet claims) {
        Object domainId = claims.getClaim("domainId");
        return domainId != null ? domainId.toString() : null;
    }
    
    /**
     * Extract roles from claims
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(JWTClaimsSet claims) {
        Object roles = claims.getClaim("roles");
        if (roles instanceof List) {
            return (List<String>) roles;
        }
        return List.of();
    }
}

