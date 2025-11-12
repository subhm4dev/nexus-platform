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
 * Location Entity
 * 
 * <p>Stores warehouse/store locations where inventory is stored.
 * Locations are tenant-scoped for multi-tenant isolation.
 */
@Entity
@Table(name = "locations", indexes = {
    @Index(name = "idx_locations_tenant", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Location {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Location name (e.g., "Main Warehouse", "Store #1")
     */
    @Column(nullable = false)
    private String name;

    /**
     * Location type (WAREHOUSE, STORE, etc.)
     */
    @Column(nullable = false)
    private String type;

    /**
     * Location address (optional)
     */
    @Column(columnDefinition = "TEXT")
    private String address;

    /**
     * City name for location-based filtering
     * Examples: "Mumbai", "Delhi", "Bangalore"
     */
    @Column(length = 100)
    private String city;

    /**
     * State or province name
     */
    @Column(length = 100)
    private String state;

    /**
     * Country code (ISO 3166-1 alpha-2)
     * Examples: "IN", "US", "UK"
     * Defaults to "IN"
     */
    @Column(length = 2)
    @Builder.Default
    private String country = "IN";

    /**
     * Tenant ID for multi-tenant isolation
     */
    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

    /**
     * Whether location is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

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

