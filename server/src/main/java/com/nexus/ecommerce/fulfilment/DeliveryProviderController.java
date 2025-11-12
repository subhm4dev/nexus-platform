package com.nexus.ecommerce.fulfilment.controller;

import com.nexus.ecommerce.fulfilment.model.request.CreateProviderRequest;
import com.nexus.ecommerce.fulfilment.model.request.UpdateProviderRequest;
import com.nexus.ecommerce.fulfilment.model.response.ProviderResponse;
import com.nexus.ecommerce.fulfilment.service.DeliveryProviderManagementService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Delivery Provider Management Controller
 * Manages third-party delivery providers and own fleet configuration
 */
@RestController
@RequestMapping("/api/v1/delivery-providers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Delivery Provider Management", description = "Manage delivery providers (BlueDart, Delhivery, Shiprocket, Dunzo, Rapido, OWN_FLEET)")
public class DeliveryProviderController {
    
    private final DeliveryProviderManagementService providerService;
    
    @PostMapping
    @Operation(summary = "Create delivery provider", description = "Create a new delivery provider configuration (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderResponse> createProvider(
        @Valid @RequestBody CreateProviderRequest request,
        Authentication authentication
    ) {
        UUID tenantId = UUID.fromString(authentication.getName());
        log.info("Creating delivery provider: providerCode={}, tenantId={}", request.providerCode(), tenantId);
        
        ProviderResponse response = providerService.createProvider(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "Get all providers", description = "Get all delivery providers for tenant (ADMIN/STAFF)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<ProviderResponse>> getAllProviders(Authentication authentication) {
        UUID tenantId = UUID.fromString(authentication.getName());
        log.debug("Getting all providers for tenant: {}", tenantId);
        
        List<ProviderResponse> providers = providerService.getAllProviders(tenantId);
        return ResponseEntity.ok(providers);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get provider by ID", description = "Get delivery provider by ID (ADMIN/STAFF)")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ProviderResponse> getProviderById(
        @PathVariable UUID id,
        Authentication authentication
    ) {
        UUID tenantId = UUID.fromString(authentication.getName());
        log.debug("Getting provider: id={}, tenantId={}", id, tenantId);
        
        ProviderResponse provider = providerService.getProviderById(id, tenantId);
        return ResponseEntity.ok(provider);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update provider", description = "Update delivery provider configuration (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProviderResponse> updateProvider(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateProviderRequest request,
        Authentication authentication
    ) {
        UUID tenantId = UUID.fromString(authentication.getName());
        log.info("Updating provider: id={}, tenantId={}", id, tenantId);
        
        ProviderResponse response = providerService.updateProvider(id, tenantId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete provider", description = "Soft delete delivery provider (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProvider(
        @PathVariable UUID id,
        Authentication authentication
    ) {
        UUID tenantId = UUID.fromString(authentication.getName());
        log.info("Deleting provider: id={}, tenantId={}", id, tenantId);
        
        providerService.deleteProvider(id, tenantId);
        return ResponseEntity.noContent().build();
    }
}

