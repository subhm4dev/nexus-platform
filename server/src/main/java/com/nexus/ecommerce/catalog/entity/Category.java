package com.nexus.ecommerce.catalog.entity;

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
 * Category Entity
 * 
 * <p>Stores product categories for organizing products. Categories can have a hierarchical
 * structure via parentId. Categories are tenant-scoped for multi-tenant isolation.
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_categories_tenant", columnList = "tenant_id"),
    @Index(name = "idx_categories_parent", columnList = "parent_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Category name
     */
    @Column(nullable = false)
    private String name;

    /**
     * Category description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Parent category ID (for hierarchical categories)
     * Null if this is a top-level category
     */
    @Column(name = "parent_id")
    private UUID parentId;

    /**
     * Tenant ID for multi-tenant isolation
     */
    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

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

