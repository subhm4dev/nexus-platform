package com.nexus.ecommerce.search.controller;

import com.nexus.ecommerce.search.model.response.RecommendationResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.search.service.RecommendationService;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Recommendation Controller
 */
@RestController
@RequestMapping("/api/v1/recommendations")
@Tag(name = "Recommendations", description = "Product recommendation endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @GetMapping
    @Operation(
        summary = "Get personalized recommendations",
        description = "Returns personalized product recommendations for the authenticated user"
    )
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        RecommendationResponse response = recommendationService.getRecommendations(userId, tenantId, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/trending")
    @Operation(
        summary = "Get trending products",
        description = "Returns trending products (public endpoint)"
    )
    public ResponseEntity<ApiResponse<RecommendationResponse>> getTrendingProducts(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        RecommendationResponse response = recommendationService.getTrendingProducts(tenantId, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return UUID.fromString(jwtToken.getUserId());
        }
        throw new IllegalStateException("Invalid authentication token");
    }
    
    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return UUID.fromString(jwtToken.getTenantId());
        }
        // For public endpoints, use default tenant
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}

