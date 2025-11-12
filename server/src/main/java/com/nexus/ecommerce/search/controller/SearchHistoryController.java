package com.nexus.ecommerce.search.controller;

import com.nexus.ecommerce.search.entity.SearchHistory;
import com.nexus.ecommerce.search.repository.SearchHistoryRepository;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Search History Controller
 */
@RestController
@RequestMapping("/api/v1/search/history")
@Tag(name = "Search History", description = "Search history endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryController {
    
    private final SearchHistoryRepository searchHistoryRepository;
    
    @GetMapping
    @Operation(
        summary = "Get search history",
        description = "Returns search history for the authenticated user"
    )
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<SearchHistory>>> getSearchHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        Page<SearchHistory> history = searchHistoryRepository.findByUserIdAndTenantIdOrderByCreatedAtDesc(
            userId, tenantId, PageRequest.of(page, size)
        );
        
        return ResponseEntity.ok(ApiResponse.success(history));
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
        throw new IllegalStateException("Invalid authentication token");
    }
}

