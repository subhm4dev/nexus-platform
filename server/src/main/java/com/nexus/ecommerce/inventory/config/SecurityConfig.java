package com.nexus.ecommerce.inventory.config;

import com.nexus.ecommerce.inventory.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * <p>Configures security for the Inventory service:
 * <ul>
 *   <li>JWT validation via JwtAuthenticationFilter</li>
 *   <li>Method-level security enabled (@PreAuthorize)</li>
 *   <li>Stateless: No session creation (JWT-based authentication)</li>
 *   <li>CSRF disabled: Stateless JWT auth doesn't need CSRF protection</li>
 * </ul>
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
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api/v1/inventory/stock"
                ).permitAll()
                
                // All other endpoints require authentication (validated by JwtAuthenticationFilter)
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}

