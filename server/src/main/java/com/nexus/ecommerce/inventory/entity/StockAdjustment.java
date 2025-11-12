package com.nexus.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stock Adjustment Entity
 * 
 * <p>Audit trail for stock adjustments. Records all changes to stock levels
 * including the reason (RESTOCK, ORDER_RESERVE, RETURN, DAMAGE, etc.).
 */
@Entity
@Table(name = "stock_adjustments", indexes = {
    @Index(name = "idx_adjustments_stock", columnList = "stock_id"),
    @Index(name = "idx_adjustments_order", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class StockAdjustment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Stock ID (foreign key to stock table)
     */
    @Column(nullable = false, name = "stock_id")
    private UUID stockId;

    /**
     * Quantity delta (positive for increases, negative for decreases)
     */
    @Column(nullable = false)
    private Integer delta;

    /**
     * Reason for adjustment (RESTOCK, ORDER_RESERVE, RETURN, DAMAGE, etc.)
     */
    @Column(nullable = false)
    private String reason;

    /**
     * Order ID (if adjustment is related to an order)
     */
    @Column(name = "order_id")
    private UUID orderId;

    /**
     * User ID who made the adjustment
     */
    @Column(nullable = false, name = "user_id")
    private UUID userId;

    /**
     * Timestamp when adjustment was made
     */
    @CreatedDate
    @Column(nullable = false, name = "timestamp")
    private LocalDateTime timestamp;
}

