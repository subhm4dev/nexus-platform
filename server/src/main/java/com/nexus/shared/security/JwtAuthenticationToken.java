package com.nexus.shared.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Token - Shared across all modules
 * 
 * <p>Custom Spring Security Authentication object that holds JWT claims.
 * Used for @PreAuthorize method-level security and tenant/user context.
 * 
 * <p>All modules use this shared implementation to ensure consistency.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String userId;
    private final String tenantId;
    private final String domainId;
    private final List<String> roles;
    private final String token;

    public JwtAuthenticationToken(String userId, String tenantId, String domainId, List<String> roles, String token) {
        super(roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList()));
        this.userId = userId;
        this.tenantId = tenantId;
        this.domainId = domainId;
        this.roles = roles;
        this.token = token;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        // Return userId as principal for @PreAuthorize expressions
        return userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getDomainId() {
        return domainId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getToken() {
        return token;
    }
}

