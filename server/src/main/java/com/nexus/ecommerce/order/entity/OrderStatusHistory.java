package com.nexus.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Order Status History Entity (Audit Trail)
 */
@Entity
@Table(name = "order_status_history", indexes = {
    @Index(name = "idx_status_history_order_id", columnList = "order_id"),
    @Index(name = "idx_status_history_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Order.OrderStatus status;
    
    @Column(name = "previous_status")
    private String previousStatus;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "changed_by")
    private UUID changedBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

