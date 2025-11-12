package com.nexus.ecommerce.fulfilment.controller;

import com.nexus.ecommerce.fulfilment.model.response.DeliveryResponse;
import com.nexus.ecommerce.fulfilment.model.response.DriverDashboardResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.fulfilment.service.DeliveryService;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/driver")
@Tag(name = "Driver Dashboard", description = "Driver dashboard endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class DriverDashboardController {
    
    private final DeliveryService deliveryService;
    
    @GetMapping("/dashboard")
    @Operation(summary = "Get driver dashboard", description = "Gets driver dashboard with today's deliveries")
    public ResponseEntity<ApiResponse<DriverDashboardResponse>> getDashboard(
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        UUID driverId = getUserId(authentication);
        
        // Get today's deliveries
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByDriver(driverId, tenantId);
        
        // Build dashboard response (simplified - needs full implementation)
        DriverDashboardResponse response = new DriverDashboardResponse(
            driverId,
            "Driver Name", // Would come from driver service
            "AVAILABLE",
            deliveries.stream().map(d -> new DriverDashboardResponse.DeliverySummary(
                d.id(),
                d.fulfillmentId(),
                d.trackingNumber(),
                d.status().name(),
                d.currentLocation(),
                d.latitude(),
                d.longitude(),
                null, // estimated arrival - not in DeliveryResponse
                0, // attempt count
                "NORMAL" // priority
            )).toList(),
            deliveries.size(),
            (int) deliveries.stream().filter(d -> d.status().name().equals("DELIVERED")).count(),
            (int) deliveries.stream().filter(d -> !d.status().name().equals("DELIVERED")).count(),
            null, // today's earnings
            null, // weekly earnings
            null, // monthly earnings
            0, // total deliveries
            null, // avg delivery time
            null, // on-time rate
            null, // current lat
            null, // current lon
            null // last location update
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/deliveries/today")
    @Operation(summary = "Get today's deliveries", description = "Gets all deliveries assigned to driver for today")
    public ResponseEntity<ApiResponse<List<DeliveryResponse>>> getTodaysDeliveries(
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        UUID driverId = getUserId(authentication);
        
        List<DeliveryResponse> deliveries = deliveryService.getDeliveriesByDriver(driverId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(deliveries));
    }
    
    private UUID getTenantId(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken token) {
            return UUID.fromString(token.getTenantId());
        }
        throw new IllegalStateException("Invalid authentication");
    }
    
    private UUID getUserId(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken token) {
            return UUID.fromString(token.getUserId());
        }
        throw new IllegalStateException("Invalid authentication");
    }
}

