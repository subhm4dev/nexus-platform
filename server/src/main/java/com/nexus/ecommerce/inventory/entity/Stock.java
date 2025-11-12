package com.nexus.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stock Entity
 * 
 * <p>Stores stock levels for products (SKUs) at specific locations.
 * Stock is tracked per SKU and location, with tenant isolation.
 */
@Entity
@Table(name = "stock", indexes = {
    @Index(name = "idx_stock_sku_location", columnList = "sku, location_id, tenant_id"),
    @Index(name = "idx_stock_sku_tenant", columnList = "sku, tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Stock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Product SKU
     */
    @Column(nullable = false)
    private String sku;

    /**
     * Location ID
     */
    @Column(nullable = false, name = "location_id")
    private UUID locationId;

    /**
     * Quantity on hand (available stock)
     */
    @Column(nullable = false, name = "qty_on_hand")
    @Builder.Default
    private Integer qtyOnHand = 0;

    /**
     * Reserved quantity (for pending orders)
     */
    @Column(nullable = false, name = "reserved_qty")
    @Builder.Default
    private Integer reservedQty = 0;

    /**
     * Tenant ID for multi-tenant isolation
     */
    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

    /**
     * Created timestamp
     */
    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last updated timestamp
     */
    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}

