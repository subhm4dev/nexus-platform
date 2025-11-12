package com.nexus.ecommerce.fulfilment.controller;

import com.nexus.ecommerce.fulfilment.model.request.SearchFulfillmentsRequest;
import com.nexus.ecommerce.fulfilment.model.response.DashboardMetricsResponse;
import com.nexus.ecommerce.fulfilment.model.response.DriverPerformanceResponse;
import com.nexus.ecommerce.fulfilment.model.response.FulfillmentResponse;
import com.nexus.ecommerce.fulfilment.model.response.ProviderPerformanceResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.fulfilment.service.AnalyticsService;
import com.nexus.ecommerce.fulfilment.service.FulfillmentService;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fulfillment")
@Tag(name = "Admin Dashboard", description = "Admin dashboard and analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {
    
    private final AnalyticsService analyticsService;
    private final FulfillmentService fulfillmentService;
    
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard metrics", description = "Gets admin dashboard metrics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<DashboardMetricsResponse>> getDashboard(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        DashboardMetricsResponse response = analyticsService.getDashboardMetrics(tenantId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search fulfillments", description = "Search and filter fulfillments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<FulfillmentResponse>>> searchFulfillments(
        @ModelAttribute SearchFulfillmentsRequest request,
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        List<String> roles = getRoles(authentication);
        // Implementation would use fulfillmentService.search() method
        // For now, returning empty list - needs implementation
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
    
    @GetMapping("/drivers/performance")
    @Operation(summary = "Get driver performance", description = "Gets driver performance metrics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<DriverPerformanceResponse>>> getDriverPerformance(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        List<DriverPerformanceResponse> response = analyticsService.getDriverPerformance(tenantId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/providers/performance")
    @Operation(summary = "Get provider performance", description = "Gets provider performance metrics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<ApiResponse<List<ProviderPerformanceResponse>>> getProviderPerformance(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        Authentication authentication
    ) {
        UUID tenantId = getTenantId(authentication);
        if (startDate == null) startDate = LocalDate.now().minusDays(30);
        if (endDate == null) endDate = LocalDate.now();
        
        List<ProviderPerformanceResponse> response = analyticsService.getProviderPerformance(tenantId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    private UUID getTenantId(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken token) {
            return UUID.fromString(token.getTenantId());
        }
        throw new IllegalStateException("Invalid authentication");
    }
    
    private List<String> getRoles(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken token) {
            return token.getRoles();
        }
        return List.of();
    }
}

