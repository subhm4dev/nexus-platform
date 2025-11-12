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
 * Reservation Entity
 * 
 * <p>Stores inventory reservations for pending orders. Reservations expire
 * after a set time (e.g., 15 minutes) if not confirmed.
 */
@Entity
@Table(name = "reservations", indexes = {
    @Index(name = "idx_reservations_order", columnList = "order_id, tenant_id"),
    @Index(name = "idx_reservations_status_expires", columnList = "status, expires_at"),
    @Index(name = "idx_reservations_sku_location", columnList = "sku, location_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Order ID (for pending order)
     */
    @Column(nullable = false, name = "order_id")
    private UUID orderId;

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
     * Reserved quantity
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Reservation expiry time
     */
    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Reservation status (PENDING, CONFIRMED, CANCELLED, EXPIRED)
     */
    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";

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

