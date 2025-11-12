package com.nexus.ecommerce.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Recommendation Entity
 */
@Entity
@Table(name = "recommendations", indexes = {
    @Index(name = "idx_recommendations_user_id", columnList = "user_id"),
    @Index(name = "idx_recommendations_product_id", columnList = "product_id"),
    @Index(name = "idx_recommendations_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_recommendations_type", columnList = "type"),
    @Index(name = "idx_recommendations_score", columnList = "score")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "score", nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal score = BigDecimal.ZERO;
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;  // COLLABORATIVE, TRENDING, SIMILAR, etc.
    
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

