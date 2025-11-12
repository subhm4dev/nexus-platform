package com.nexus.ecommerce.catalog.controller;

import com.nexus.ecommerce.catalog.model.request.ProductRequest;
import com.nexus.ecommerce.catalog.model.response.ProductResponse;
import com.nexus.ecommerce.catalog.model.response.ProductSearchResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.catalog.service.ProductService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Product Catalog Controller
 * 
 * <p>This controller manages product information including details like name, SKU,
 * price, description, and images. Products form the core of the e-commerce catalog
 * that customers browse and purchase.
 * 
 * <p>Why we need these APIs:
 * <ul>
 *   <li><b>Product Management:</b> Sellers need to create and manage product listings.
 *       Essential for marketplace functionality where multiple sellers list products.</li>
 *   <li><b>Product Discovery:</b> Customers browse products by category, search terms,
 *       or filters. Frontend relies on this service to display product catalogs.</li>
 *   <li><b>Pricing Information:</b> Provides base product prices that the Promotion
 *       service applies discounts to. Checkout uses final prices for order calculation.</li>
 *   <li><b>Event-Driven Integration:</b> Product creation triggers ProductCreated Kafka
 *       event, enabling Inventory service to auto-create stock records and Search
 *       service to index products.</li>
 *   <li><b>Multi-Variant Support:</b> Products can have variants (size, color, etc.),
 *       each with separate pricing and inventory tracking.</li>
 * </ul>
 * 
 * <p>Products are tenant-scoped, enabling marketplace scenarios where different sellers
 * (tenants) manage their own product catalogs.
 * 
 * <p><b>Security:</b> JWT tokens are validated by JwtAuthenticationFilter.
 * User context comes from validated JWT claims (source of truth).
 * Gateway headers (X-User-Id, X-Roles) are hints only, not trusted for security.
 */
@RestController
@RequestMapping("/api/v1/product")
@Tag(name = "Product Catalog", description = "Product management and catalog endpoints")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Create a new product
     * 
     * <p>This endpoint allows sellers to create new product listings. Product creation
     * triggers a ProductCreated event to Kafka, which Inventory service consumes to
     * initialize stock records.
     * 
     * <p>Business rules:
     * <ul>
     *   <li>SKU must be unique within tenant scope</li>
     *   <li>Price and currency are required</li>
     *   <li>Products can belong to categories for organization</li>
     *   <li>Products can have variants (size, color, etc.)</li>
     * </ul>
     * 
     * <p>Access control: Only SELLER and ADMIN roles can create products.
     * 
     * <p>This endpoint is protected and requires authentication.
     */
    @PostMapping
    @Operation(
        summary = "Create a new product",
        description = "Creates a new product listing. Triggers ProductCreated event to Kafka for inventory initialization."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest productRequest,
            Authentication authentication) {
        
        // Extract user context from validated JWT (source of truth)
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        log.info("Creating product for seller: {}, tenant: {}", currentUserId, tenantId);
        
        ProductResponse response = productService.createProduct(
            currentUserId,
            tenantId,
            currentUserId,
            roles,
            productRequest
        );
        
        return ApiResponse.success(response, "Product created successfully");
    }

    /**
     * Get product by ID
     * 
     * <p>Retrieves detailed product information including variants, pricing, and images.
     * Used by frontend to display product detail pages, and by other services (checkout,
     * cart) to fetch product information.
     * 
     * <p>This endpoint is public (for customer browsing). Authentication is optional
     * but tenant context is required for multi-tenant filtering.
     */
    @GetMapping("/{productId}")
    @Operation(
        summary = "Get product by ID",
        description = "Retrieves detailed product information including variants, pricing, and images"
    )
    public ApiResponse<ProductResponse> getProduct(
            @PathVariable UUID productId,
            @RequestParam(required = false) UUID tenantId, // Optional tenant ID for public access
            Authentication authentication) {
        
        // Extract tenant ID: priority: query param > JWT > default
        if (tenantId == null && authentication != null) {
            tenantId = getTenantIdFromAuthentication(authentication);
        }
        
        // For public access without authentication, use default tenant if configured
        if (tenantId == null) {
            tenantId = getDefaultTenantId();
            if (tenantId == null) {
                throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    "Tenant ID is required. Please provide 'tenantId' as a query parameter or authenticate."
                );
            }
        }
        
        log.info("Getting product {} for tenant: {}", productId, tenantId);
        
        ProductResponse response = productService.getProductById(productId, tenantId);
        
        return ApiResponse.success(response, "Product retrieved successfully");
    }

    /**
     * Search products with filters
     * 
     * <p>Enables product discovery through search and filtering. Supports filtering by
     * category, price range, availability, and search terms. Essential for customer
     * browsing experience.
     * 
     * <p>Returns paginated results for performance. Can integrate with dedicated Search
     * service for advanced full-text search capabilities.
     * 
     * <p>This endpoint is public (for customer browsing). Authentication is optional
     * but tenant context is required for multi-tenant filtering.
     */
    @GetMapping("/search")
    @Operation(
        summary = "Search products with filters",
        description = "Searches products by category, price range, availability, and search terms. Returns paginated results."
    )
    public ApiResponse<ProductSearchResponse> searchProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock, // Not implemented yet - would require inventory service integration
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID tenantId, // Optional tenant ID for public access
            Authentication authentication) {
        
        // Extract tenant ID: priority: query param > JWT > default
        if (tenantId == null && authentication != null) {
            tenantId = getTenantIdFromAuthentication(authentication);
        }
        
        // For public access without authentication, use default tenant if configured
        // Otherwise, require tenantId as query parameter
        if (tenantId == null) {
            tenantId = getDefaultTenantId();
            if (tenantId == null) {
                throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    "Tenant ID is required. Please provide 'tenantId' as a query parameter or authenticate."
                );
            }
        }
        
        log.info("Searching products for tenant: {}, query: {}, page: {}, size: {}", tenantId, query, page, size);
        
        BigDecimal minPriceDecimal = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal maxPriceDecimal = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
        
        ProductSearchResponse response = productService.searchProducts(
            tenantId,
            query,
            categoryId,
            minPriceDecimal,
            maxPriceDecimal,
            page,
            size
        );
        
        return ApiResponse.success(response, "Products retrieved successfully");
    }

    /**
     * Update product
     * 
     * <p>Allows sellers to update product information (price changes, description updates,
     * new images, etc.). Updates may trigger events for services that cache product data.
     * 
     * <p>Access control: Only product owner (seller) or ADMIN can update.
     * 
     * <p>This endpoint is protected and requires authentication.
     */
    @PutMapping("/{productId}")
    @Operation(
        summary = "Update product",
        description = "Updates product information. Only product owner or admin can modify products."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductRequest productRequest,
            Authentication authentication) {
        
        // Extract user context from validated JWT (source of truth)
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        log.info("Updating product {} for user: {}, tenant: {}", productId, currentUserId, tenantId);
        
        ProductResponse response = productService.updateProduct(
            productId,
            currentUserId,
            tenantId,
            roles,
            productRequest
        );
        
        return ApiResponse.success(response, "Product updated successfully");
    }

    /**
     * Delete product
     * 
     * <p>Soft deletes or hard deletes a product. Soft delete is preferred to maintain
     * order history references. Deletion may trigger events for inventory cleanup.
     * 
     * <p>Access control: Only product owner (seller) or ADMIN can delete.
     * 
     * <p>This endpoint is protected and requires authentication.
     */
    @DeleteMapping("/{productId}")
    @Operation(
        summary = "Delete product",
        description = "Removes a product from catalog. Soft delete recommended to maintain order history."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable UUID productId,
            Authentication authentication) {
        
        // Extract user context from validated JWT (source of truth)
        UUID currentUserId = getUserIdFromAuthentication(authentication);
        UUID tenantId = getTenantIdFromAuthentication(authentication);
        List<String> roles = getRolesFromAuthentication(authentication);
        
        log.info("Deleting product {} for user: {}, tenant: {}", productId, currentUserId, tenantId);
        
        productService.deleteProduct(
            productId,
            currentUserId,
            tenantId,
            roles
        );
        
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Extract user ID from Spring Security Authentication
     * 
     * <p>The Authentication object is populated by JwtAuthenticationFilter
     * from validated JWT claims (source of truth).
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "User ID is required. Please ensure you are authenticated."
            );
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        String userIdStr = jwtAuth.getUserId();
        
        try {
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format in JWT: {}", userIdStr);
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Invalid user ID format"
            );
        }
    }

    /**
     * Extract tenant ID from Spring Security Authentication
     */
    private UUID getTenantIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Tenant ID is required. Please ensure you are authenticated."
            );
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        String tenantIdStr = jwtAuth.getTenantId();
        
        try {
            return UUID.fromString(tenantIdStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid tenant ID format in JWT: {}", tenantIdStr);
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Invalid tenant ID format"
            );
        }
    }

    /**
     * Get default tenant ID for public browsing
     * 
     * <p>For public access without authentication, we use the default marketplace tenant.
     * This is the same tenant ID used for customers in the Identity service.
     */
    private UUID getDefaultTenantId() {
        // Default marketplace tenant ID (same as in Identity service)
        // This is the tenant where all customers belong by default
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    /**
     * Extract roles from Spring Security Authentication
     */
    private List<String> getRolesFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            return List.of();
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        return jwtAuth.getRoles();
    }
}

