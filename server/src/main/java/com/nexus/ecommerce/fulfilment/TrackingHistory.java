package com.nexus.ecommerce.fulfilment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracking History Entity
 */
@Entity
@Table(name = "tracking_history", indexes = {
    @Index(name = "idx_tracking_history_delivery_id", columnList = "delivery_id"),
    @Index(name = "idx_tracking_history_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;
    
    @Column(name = "delivery_id", insertable = false, updatable = false)
    private UUID deliveryId;
    
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Column(name = "location_description", length = 500)
    private String locationDescription;
    
    @Column(name = "status", length = 50)
    private String status;
    
    @Column(name = "updated_by")
    private UUID updatedBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

