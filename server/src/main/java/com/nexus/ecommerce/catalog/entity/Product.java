package com.nexus.ecommerce.catalog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Product Entity
 * 
 * <p>Stores product information including details like name, SKU, price, description, and images.
 * Products are tenant-scoped and seller-scoped, enabling marketplace scenarios where different sellers
 * (tenants) manage their own product catalogs.
 * 
 * <p>Soft delete support: All deletions are soft deletes (deleted flag + deletedAt timestamp)
 * for audit trail and data recovery purposes.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_sku_tenant", columnList = "sku, tenant_id"),
    @Index(name = "idx_products_seller_tenant", columnList = "seller_id, tenant_id"),
    @Index(name = "idx_products_parent_tenant", columnList = "parent_tenant_id"),
    @Index(name = "idx_products_category", columnList = "category_id"),
    @Index(name = "idx_products_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Product name
     */
    @Column(nullable = false)
    private String name;

    /**
     * Stock Keeping Unit - must be unique within tenant
     */
    @Column(nullable = false, unique = false)
    private String sku;

    /**
     * Product description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Product price
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    /**
     * Currency code (ISO 4217, e.g., "USD", "EUR")
     */
    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Category ID (foreign key to categories table, optional)
     */
    @Column(name = "category_id")
    private UUID categoryId;

    /**
     * Seller ID (user ID from Identity service who created/owns this product)
     * Used for user-level operations, permissions, and audit trails.
     * Points to user_accounts.id of the seller user.
     */
    @Column(nullable = false, name = "seller_id")
    private UUID sellerId;

    /**
     * Tenant ID - Availability location
     * - BRANCH tenant: Product is branch-specific (e.g., only available at Mumbai branch)
     * - SELLER tenant: Product is available at all seller branches
     * Used for inventory management and location-based filtering.
     */
    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

    /**
     * Parent Tenant ID - Seller ownership
     * Points to SELLER tenant that owns this product.
     * Always set to the SELLER tenant (not APP or BRANCH).
     * Used for seller-level queries and tenant hierarchy operations.
     * Example: If seller has branches, parent_tenant_id = seller tenant, tenant_id = branch tenant (if branch-specific)
     */
    @Column(name = "parent_tenant_id")
    private UUID parentTenantId;

    /**
     * Product images (stored as JSON array of URLs)
     */
    @Column(columnDefinition = "TEXT")
    private String images; // JSON array of image URLs

    /**
     * Product status (ACTIVE, INACTIVE, DRAFT)
     */
    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * Soft delete flag
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * Timestamp when product was soft deleted
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Created timestamp (auto-populated by JPA auditing)
     */
    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last updated timestamp (auto-populated by JPA auditing)
     */
    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}

