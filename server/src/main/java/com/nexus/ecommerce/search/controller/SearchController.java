package com.nexus.ecommerce.search.controller;

import com.nexus.ecommerce.search.model.response.AutocompleteResponse;
import com.nexus.ecommerce.search.model.response.SearchResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.search.service.AutocompleteService;
import com.nexus.ecommerce.search.service.SearchService;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Search Controller
 */
@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Fast live search endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class SearchController {
    
    private final SearchService searchService;
    private final AutocompleteService autocompleteService;
    
    @GetMapping
    @Operation(
        summary = "Fast live search",
        description = "Searches across products, categories, and subcategories. Returns results in <200ms."
    )
    public ResponseEntity<ApiResponse<SearchResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID subcategoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        SearchResponse response = searchService.search(tenantId, q, categoryId, subcategoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/autocomplete")
    @Operation(
        summary = "Autocomplete suggestions",
        description = "Returns autocomplete suggestions for products, categories, and subcategories. Returns in <5ms."
    )
    public ResponseEntity<ApiResponse<AutocompleteResponse>> autocomplete(
            @RequestParam String q,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        AutocompleteResponse response = autocompleteService.getSuggestions(tenantId, q);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(
        summary = "Search within category",
        description = "Searches products within a specific category"
    )
    public ResponseEntity<ApiResponse<SearchResponse>> searchByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        SearchResponse response = searchService.searchByCategory(categoryId, tenantId, q, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/subcategory/{subcategoryId}")
    @Operation(
        summary = "Search within subcategory",
        description = "Searches products within a specific subcategory"
    )
    public ResponseEntity<ApiResponse<SearchResponse>> searchBySubcategory(
            @PathVariable UUID subcategoryId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        
        SearchResponse response = searchService.searchBySubcategory(subcategoryId, tenantId, q, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return UUID.fromString(jwtToken.getTenantId());
        }
        // For public search, use default tenant (0) or extract from request header
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }
}

