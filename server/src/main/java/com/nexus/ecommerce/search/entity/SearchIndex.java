package com.nexus.ecommerce.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Search Index Entity
 * Denormalized table for fast full-text search
 */
@Entity
@Table(name = "search_index", indexes = {
    @Index(name = "idx_search_index_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_search_index_product_id", columnList = "product_id"),
    @Index(name = "idx_search_index_category_id", columnList = "category_id"),
    @Index(name = "idx_search_index_subcategory_id", columnList = "subcategory_id"),
    @Index(name = "idx_search_index_sku", columnList = "sku")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchIndex {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "name", nullable = false, length = 500)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "sku", length = 100)
    private String sku;
    
    @Column(name = "category_id")
    private UUID categoryId;
    
    @Column(name = "subcategory_id")
    private UUID subcategoryId;
    
    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;
    
    @Column(name = "search_vector", columnDefinition = "tsvector")
    @JdbcTypeCode(SqlTypes.OTHER)
    private Object searchVector;  // PostgreSQL tsvector type
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

