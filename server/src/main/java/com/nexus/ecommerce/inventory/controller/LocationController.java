package com.nexus.ecommerce.inventory.controller;

import com.nexus.ecommerce.inventory.model.request.LocationRequest;
import com.nexus.ecommerce.inventory.model.response.LocationResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.inventory.service.LocationService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
 * Location Controller
 * 
 * <p>This controller manages warehouse/store locations where inventory is stored.
 * Locations are tenant-scoped for multi-tenant isolation.
 */
@RestController
@RequestMapping("/api/v1/inventory/location")
@Tag(name = "Location Management", description = "Location CRUD endpoints for inventory management")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    /**
     * Create a new location
     * 
     * <p>Access control: Only SELLER and ADMIN can create locations.
     */
    @PostMapping
    @Operation(
        summary = "Create a new location",
        description = "Creates a new warehouse/store location for inventory management"
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<LocationResponse> createLocation(
            @Valid @RequestBody LocationRequest locationRequest,
            Authentication authentication) {
        
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        log.info("Creating location for tenant: {}", tenantId);
        
        LocationResponse response = locationService.createLocation(
            tenantId,
            currentUserId,
            roles,
            locationRequest
        );
        
        return ApiResponse.success(response, "Location created successfully");
    }

    /**
     * Get location by ID
     */
    @GetMapping("/{locationId}")
    @Operation(
        summary = "Get location by ID",
        description = "Retrieves location details"
    )
    public ApiResponse<LocationResponse> getLocation(
            @PathVariable UUID locationId,
            Authentication authentication) {
        
        UUID tenantId = authentication != null ? getTenantIdFromAuthentication(authentication) : null;
        if (tenantId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Tenant ID is required");
        }
        
        log.info("Getting location {} for tenant: {}", locationId, tenantId);
        
        LocationResponse response = locationService.getLocationById(locationId, tenantId);
        
        return ApiResponse.success(response, "Location retrieved successfully");
    }

    /**
     * Get all locations for tenant
     */
    @GetMapping
    @Operation(
        summary = "Get all locations",
        description = "Returns all locations for the tenant. Use activeOnly=true to filter only active locations."
    )
    public ApiResponse<List<LocationResponse>> getAllLocations(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            Authentication authentication) {
        
        UUID tenantId = authentication != null ? getTenantIdFromAuthentication(authentication) : null;
        if (tenantId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Tenant ID is required");
        }
        
        log.info("Getting all locations (activeOnly={}) for tenant: {}", activeOnly, tenantId);
        
        List<LocationResponse> response = locationService.getAllLocations(tenantId, activeOnly);
        
        return ApiResponse.success(response, "Locations retrieved successfully");
    }

    /**
     * Update location
     * 
     * <p>Access control: Only SELLER and ADMIN can update locations.
     */
    @PutMapping("/{locationId}")
    @Operation(
        summary = "Update location",
        description = "Updates location information. Only SELLER and ADMIN can update."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<LocationResponse> updateLocation(
            @PathVariable UUID locationId,
            @Valid @RequestBody LocationRequest locationRequest,
            Authentication authentication) {
        
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        log.info("Updating location {} for tenant: {}", locationId, tenantId);
        
        LocationResponse response = locationService.updateLocation(
            locationId,
            tenantId,
            currentUserId,
            roles,
            locationRequest
        );
        
        return ApiResponse.success(response, "Location updated successfully");
    }

    /**
     * Deactivate location (soft delete)
     * 
     * <p>Access control: Only SELLER and ADMIN can deactivate locations.
     * Deactivated locations cannot be used for new stock operations.
     */
    @DeleteMapping("/{locationId}")
    @Operation(
        summary = "Deactivate location",
        description = "Deactivates a location (soft delete). Location cannot be used for new stock operations."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deactivateLocation(
            @PathVariable UUID locationId,
            Authentication authentication) {
        
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        log.info("Deactivating location {} for tenant: {}", locationId, tenantId);
        
        locationService.deactivateLocation(
            locationId,
            tenantId,
            currentUserId,
            roles
        );
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "User ID is required. Please ensure you are authenticated."
            );
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        try {
            return UUID.fromString(jwtAuth.getUserId());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid user ID format");
        }
    }

    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Tenant ID is required. Please ensure you are authenticated."
            );
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        try {
            return UUID.fromString(jwtAuth.getTenantId());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid tenant ID format");
        }
    }

    private List<String> getRolesFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            return List.of();
        }
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        return jwtAuth.getRoles();
    }
}

