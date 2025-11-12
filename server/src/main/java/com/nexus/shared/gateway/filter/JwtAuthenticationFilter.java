package com.nexus.shared.gateway.filter;

import com.nexus.libs.jwt.blocking.BlockingJwtValidationService;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter
 * 
 * <p>Spring Security filter that extracts JWT from Authorization header,
 * validates it, and creates JwtAuthenticationToken for Spring Security context.
 * 
 * <p>This filter runs before Spring Security's authentication chain.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final BlockingJwtValidationService jwtValidationService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.debug("No Authorization header found, skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authorizationHeader.substring(7);
            log.debug("Validating JWT token for request: {} {} (token length: {})", 
                request.getMethod(), request.getRequestURI(), token.length());
            JWTClaimsSet claims = jwtValidationService.validateToken(token);
            
            // Extract user context from validated JWT claims (source of truth)
            String userId = jwtValidationService.extractUserId(claims);
            String tenantId = jwtValidationService.extractTenantId(claims);
            List<String> roles = jwtValidationService.extractRoles(claims);
            String domainId = jwtValidationService.extractDomainId(claims);

            log.debug("JWT validated successfully: userId={}, tenantId={}, domainId={}, roles={}", userId, tenantId, domainId, roles);

            // Create authentication token for Spring Security
            JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                userId, tenantId, domainId != null ? domainId : "", roles, token
            );

            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (IllegalArgumentException e) {
            log.error("JWT validation failed for request: {} {} - {}", 
                request.getMethod(), request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
            // Set 403 response with error message
            try {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                String errorMessage = "{\"error\":\"JWT validation failed: " + e.getMessage().replace("\"", "\\\"") + "\"}";
                response.getWriter().write(errorMessage);
                response.getWriter().flush();
            } catch (IOException ioException) {
                log.error("Failed to write error response", ioException);
            }
            return;
        } catch (RuntimeException e) {
            log.error("JWT validation failed for request: {} {} - {}", 
                request.getMethod(), request.getRequestURI(), e.getMessage(), e);
            SecurityContextHolder.clearContext();
            // Set 403 response with error message
            try {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                String errorMessage = "{\"error\":\"JWT validation failed: " + e.getMessage().replace("\"", "\\\"") + "\"}";
                response.getWriter().write(errorMessage);
                response.getWriter().flush();
            } catch (IOException ioException) {
                log.error("Failed to write error response", ioException);
            }
            return;
        } catch (Exception e) {
            log.error("Unexpected error during JWT authentication for request: {} {}", 
                request.getMethod(), request.getRequestURI(), e);
            SecurityContextHolder.clearContext();
            // Set 403 response with error message
            try {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                String errorMessage = "{\"error\":\"JWT authentication error: " + e.getMessage().replace("\"", "\\\"") + "\"}";
                response.getWriter().write(errorMessage);
                response.getWriter().flush();
            } catch (IOException ioException) {
                log.error("Failed to write error response", ioException);
            }
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Optional: Clear SecurityContext after request completes
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip JWT validation for public endpoints
        return path.startsWith("/api/v1/auth/") || 
               path.startsWith("/.well-known/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/");
    }
}
