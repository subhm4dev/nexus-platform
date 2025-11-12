package com.nexus.ecommerce.catalog.config;

import com.nexus.ecommerce.catalog.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration
 * 
 * <p>Configures security for the Catalog service:
 * <ul>
 *   <li>JWT validation via JwtAuthenticationFilter</li>
 *   <li>Method-level security enabled (@PreAuthorize)</li>
 *   <li>Stateless: No session creation (JWT-based authentication)</li>
 *   <li>CSRF disabled: Stateless JWT auth doesn't need CSRF protection</li>
 * </ul>
 * 
 * <p>Security Flow:
 * <ol>
 *   <li>JwtAuthenticationFilter extracts JWT from Authorization header</li>
 *   <li>JWT is validated (signature, expiry, blacklist)</li>
 *   <li>JwtAuthenticationToken is created from validated claims</li>
 *   <li>Spring Security uses this for @PreAuthorize authorization</li>
 * </ol>
 * 
 * <p>Note: Gateway headers (X-User-Id, X-Roles) are hints only.
 * Security comes from validated JWT claims.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (stateless JWT authentication)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Stateless session management (JWT-based, no sessions)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Add JWT authentication filter before Spring Security's authentication chain
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Configure endpoint access
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (no authentication required)
                // Only 3 catalog endpoints are public for browsing:
                // 1. GET /api/v1/category - Get all categories
                // 2. GET /api/v1/product/search - Search/get all products
                // 3. GET /api/v1/product/{id} - Get product by ID
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/product/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/product/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/category").permitAll()
                
                // All other endpoints require authentication (validated by JwtAuthenticationFilter)
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}

