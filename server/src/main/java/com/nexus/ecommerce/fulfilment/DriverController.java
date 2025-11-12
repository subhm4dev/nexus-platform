package com.nexus.ecommerce.fulfilment.controller;

import com.nexus.ecommerce.fulfilment.model.request.CreateDriverRequest;
import com.nexus.ecommerce.fulfilment.model.request.UpdateDriverRequest;
import com.nexus.ecommerce.fulfilment.model.response.DriverResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.fulfilment.service.DriverService;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Driver Controller
 */
@RestController
@RequestMapping("/api/v1/driver")
@Tag(name = "Driver", description = "Driver management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class DriverController {
    
    private final DriverService driverService;
    
    @PostMapping
    @Operation(summary = "Create driver", description = "Creates a new driver (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(
            @Valid @RequestBody CreateDriverRequest request,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        DriverResponse response = driverService.createDriver(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Driver created successfully"));
    }
    
    @GetMapping("/{driverId}")
    @Operation(summary = "Get driver by ID", description = "Retrieves driver details")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriver(
            @PathVariable UUID driverId,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        DriverResponse response = driverService.getDriverById(driverId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    @Operation(summary = "Get all drivers", description = "Retrieves all drivers for tenant")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAllDrivers(
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<DriverResponse> response = driverService.getAllDrivers(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/available")
    @Operation(summary = "Get available drivers", description = "Retrieves available drivers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getAvailableDrivers(
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<DriverResponse> response = driverService.getAvailableDrivers(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PutMapping("/{driverId}")
    @Operation(summary = "Update driver", description = "Updates driver information (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriver(
            @PathVariable UUID driverId,
            @Valid @RequestBody UpdateDriverRequest request,
            Authentication authentication) {
        
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        DriverResponse response = driverService.updateDriver(driverId, tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Driver updated successfully"));
    }
    
    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return UUID.fromString(jwtToken.getTenantId());
        }
        throw new IllegalStateException("Invalid authentication token");
    }
}

