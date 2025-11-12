package com.nexus.ecommerce.fulfilment.controller;

import com.nexus.ecommerce.fulfilment.provider.dto.TrackingResponse;
import com.nexus.ecommerce.fulfilment.service.DeliveryService;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Public Tracking Controller
 * No authentication required - for customer tracking
 */
@RestController
@RequestMapping("/api/v1/public/tracking")
@Tag(name = "Public Tracking", description = "Public tracking endpoints (no auth required)")
@RequiredArgsConstructor
@Slf4j
public class PublicTrackingController {
    
    private final DeliveryService deliveryService;
    
    @GetMapping("/{trackingNumber}")
    @Operation(summary = "Track delivery", description = "Public tracking endpoint (no authentication required)")
    public ResponseEntity<ApiResponse<TrackingResponse>> trackDelivery(
        @PathVariable String trackingNumber
    ) {
        // For public tracking, we need tenantId - in real implementation,
        // tracking number should encode tenant info or we query all tenants
        // For now, we'll need to find tenant from tracking number or allow null
        // This is a simplified implementation
        // For public tracking, we need tenantId - in real implementation,
        // tracking number should encode tenant info or we query all tenants
        // This is a simplified implementation - would need proper tenant lookup
        TrackingResponse response = deliveryService.getTracking(trackingNumber, null);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

