package com.nexus.ecommerce.search.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Search History Entity
 */
@Entity
@Table(name = "search_history", indexes = {
    @Index(name = "idx_search_history_user_id", columnList = "user_id"),
    @Index(name = "idx_search_history_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_search_history_created_at", columnList = "created_at"),
    @Index(name = "idx_search_history_query", columnList = "query")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "query", nullable = false, length = 500)
    private String query;
    
    @Column(name = "results_count")
    @Builder.Default
    private Integer resultsCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

