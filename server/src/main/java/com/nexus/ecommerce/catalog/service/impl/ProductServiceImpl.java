package com.nexus.ecommerce.catalog.service.impl;

import com.nexus.ecommerce.catalog.entity.Product;
import com.nexus.ecommerce.catalog.model.event.ProductCreatedEvent;
import com.nexus.ecommerce.catalog.model.request.ProductRequest;
import com.nexus.ecommerce.catalog.model.response.ProductResponse;
import com.nexus.ecommerce.catalog.model.response.ProductSearchResponse;
import com.nexus.ecommerce.catalog.repository.ProductRepository;
import com.nexus.ecommerce.catalog.service.ProductService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of ProductService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * Kafka topic for product creation events
     */
    private static final String PRODUCT_CREATED_TOPIC = "product-created";

    @Override
    @Transactional
    public ProductResponse createProduct(
            UUID sellerId,
            UUID tenantId,
            UUID currentUserId,
            List<String> roles,
            ProductRequest request) {
        
        log.debug("Creating product for seller: {}, tenant: {}", sellerId, tenantId);

        // 1. Authorization check: Only SELLER and ADMIN can create products
        if (!hasSellerOrAdminRole(roles)) {
            log.warn("Unauthorized: User {} attempted to create product without SELLER/ADMIN role", currentUserId);
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Only SELLER and ADMIN roles can create products"
            );
        }

        // 2. Check SKU uniqueness within tenant
        boolean skuExists = productRepository.findBySkuAndTenantId(request.sku(), tenantId).isPresent();
        if (skuExists) {
            log.warn("Duplicate SKU detected: {} for tenant: {}", request.sku(), tenantId);
            throw new BusinessException(
                ErrorCode.SKU_REQUIRED,
                "SKU already exists: " + request.sku()
            );
        }

        // 3. Validate category if provided
        if (request.categoryId() != null) {
            // Category validation would go here if CategoryRepository is injected
            // For now, we'll just check if category exists in the same tenant
        }

        // 4. Serialize images to JSON
        String imagesJson = null;
        if (request.images() != null && !request.images().isEmpty()) {
            try {
                imagesJson = objectMapper.writeValueAsString(request.images());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize images to JSON", e);
                throw new BusinessException(
                    ErrorCode.SKU_REQUIRED,
                    "Invalid images format"
                );
            }
        }

        // 5. Create new product
        // parent_tenant_id = seller's tenant_id (from JWT, which is the SELLER tenant)
        // tenant_id = availability location (BRANCH or SELLER tenant, currently same as seller's tenant)
        Product product = Product.builder()
            .name(request.name())
            .sku(request.sku())
            .description(request.description())
            .price(request.price())
            .currency(request.currency())
            .categoryId(request.categoryId())
            .sellerId(sellerId)
            .tenantId(tenantId)  // Availability location (BRANCH or SELLER tenant)
            .parentTenantId(tenantId)  // Seller ownership (SELLER tenant from JWT)
            .images(imagesJson)
            .status(request.status() != null ? request.status() : "ACTIVE")
            .deleted(false)
            .build();

        Product savedProduct = productRepository.save(product);
        log.info("Created product {} for seller: {}, tenant: {}", savedProduct.getId(), sellerId, tenantId);

        // 6. Publish ProductCreated event to Kafka
        try {
            ProductCreatedEvent event = ProductCreatedEvent.of(
                savedProduct.getId(),
                savedProduct.getSku(),
                savedProduct.getTenantId(),
                savedProduct.getSellerId()
            );
            kafkaTemplate.send(PRODUCT_CREATED_TOPIC, savedProduct.getId().toString(), event);
            log.info("Published ProductCreated event for product: {}", savedProduct.getId());
        } catch (Exception e) {
            log.error("Failed to publish ProductCreated event for product: {}", savedProduct.getId(), e);
            // Don't fail the request if Kafka publish fails - event can be retried
        }

        return toResponse(savedProduct);
    }

    @Override
    public ProductResponse getProductById(UUID productId, UUID tenantId) {
        log.debug("Getting product {} for tenant: {}", productId, tenantId);

        // 1. Find product (active only)
        Product product = productRepository.findByIdAndTenantIdAndDeletedFalse(productId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PRODUCT_NOT_FOUND,
                "Product not found: " + productId
            ));

        return toResponse(product);
    }

    @Override
    public ProductSearchResponse searchProducts(
            UUID tenantId,
            String query,
            UUID categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size) {
        
        log.debug("Searching products for tenant: {}, query: {}, category: {}, page: {}, size: {}", 
            tenantId, query, categoryId, page, size);

        // 1. Build search criteria
        Page<Product> productPage = productRepository.searchProducts(
            tenantId,
            categoryId,
            minPrice,
            maxPrice,
            query,
            org.springframework.data.domain.PageRequest.of(page, size)
        );

        // 2. Convert to response
        List<ProductResponse> products = productPage.getContent().stream()
            .map(this::toResponse)
            .toList();

        return new ProductSearchResponse(
            products,
            productPage.getNumber(),
            productPage.getSize(),
            productPage.getTotalElements(),
            productPage.getTotalPages(),
            productPage.isFirst(),
            productPage.isLast()
        );
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(
            UUID productId,
            UUID currentUserId,
            UUID tenantId,
            List<String> roles,
            ProductRequest request) {
        
        log.debug("Updating product {} for user: {}, tenant: {}", productId, currentUserId, tenantId);

        // 1. Find product (active only)
        Product product = productRepository.findByIdAndTenantIdAndDeletedFalse(productId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PRODUCT_NOT_FOUND,
                "Product not found: " + productId
            ));

        // 2. Authorization check: Only product owner or ADMIN can update
        if (!canAccessProduct(currentUserId, product.getSellerId(), roles)) {
            log.warn("Unauthorized: User {} attempted to update product {} owned by seller {}", 
                currentUserId, productId, product.getSellerId());
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "You do not have permission to update this product"
            );
        }

        // 3. Check SKU uniqueness if SKU is being changed
        if (!product.getSku().equals(request.sku())) {
            boolean skuExists = productRepository.findBySkuAndTenantId(request.sku(), tenantId).isPresent();
            if (skuExists) {
                log.warn("Duplicate SKU detected: {} for tenant: {}", request.sku(), tenantId);
                throw new BusinessException(
                    ErrorCode.SKU_REQUIRED,
                    "SKU already exists: " + request.sku()
                );
            }
        }

        // 4. Serialize images to JSON if provided
        if (request.images() != null && !request.images().isEmpty()) {
            try {
                String imagesJson = objectMapper.writeValueAsString(request.images());
                product.setImages(imagesJson);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize images to JSON", e);
                throw new BusinessException(
                    ErrorCode.SKU_REQUIRED,
                    "Invalid images format"
                );
            }
        }

        // 5. Update product fields
        product.setName(request.name());
        product.setSku(request.sku());
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        product.setPrice(request.price());
        product.setCurrency(request.currency());
        if (request.categoryId() != null) {
            product.setCategoryId(request.categoryId());
        }
        if (request.status() != null) {
            product.setStatus(request.status());
        }

        Product savedProduct = productRepository.save(product);
        log.info("Updated product {} for seller: {}", productId, product.getSellerId());

        return toResponse(savedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(
            UUID productId,
            UUID currentUserId,
            UUID tenantId,
            List<String> roles) {
        
        log.debug("Deleting product {} for user: {}, tenant: {}", productId, currentUserId, tenantId);

        // 1. Find product (active only)
        Product product = productRepository.findByIdAndTenantIdAndDeletedFalse(productId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.PRODUCT_NOT_FOUND,
                "Product not found: " + productId
            ));

        // 2. Authorization check: Only product owner or ADMIN can delete
        if (!canAccessProduct(currentUserId, product.getSellerId(), roles)) {
            log.warn("Unauthorized: User {} attempted to delete product {} owned by seller {}", 
                currentUserId, productId, product.getSellerId());
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "You do not have permission to delete this product"
            );
        }

        // 3. Soft delete
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
        
        log.info("Soft deleted product {} for seller: {}", productId, product.getSellerId());
    }

    @Override
    public boolean canAccessProduct(UUID currentUserId, UUID sellerId, List<String> roles) {
        // Users can always access/modify their own products
        if (currentUserId.equals(sellerId)) {
            return true;
        }

        // Admins can access/modify any product
        if (hasAdminRole(roles)) {
            log.debug("Admin access granted for product owned by seller: {}", sellerId);
            return true;
        }

        // Default: deny access
        log.warn("Access denied: user {} attempted to access product owned by seller {}", currentUserId, sellerId);
        return false;
    }

    /**
     * Check if user has SELLER or ADMIN role
     */
    private boolean hasSellerOrAdminRole(List<String> roles) {
        return roles != null && (
            roles.contains("SELLER") || 
            roles.contains("ADMIN")
        );
    }

    /**
     * Check if user has ADMIN role
     */
    private boolean hasAdminRole(List<String> roles) {
        return roles != null && roles.contains("ADMIN");
    }

    /**
     * Convert Product entity to ProductResponse DTO
     */
    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getSku(),
            product.getDescription(),
            product.getPrice(),
            product.getCurrency(),
            product.getCategoryId(),
            product.getSellerId(),
            ProductResponse.parseImages(product.getImages()),
            product.getStatus(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}

