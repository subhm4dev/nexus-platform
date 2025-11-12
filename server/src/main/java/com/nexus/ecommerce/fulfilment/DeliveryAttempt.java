package com.nexus.ecommerce.fulfilment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Delivery Attempt Entity
 * Tracks each delivery attempt with failure reasons and photos
 */
@Entity
@Table(name = "delivery_attempts", indexes = {
    @Index(name = "idx_delivery_attempt_delivery_id", columnList = "delivery_id"),
    @Index(name = "idx_delivery_attempt_fulfillment_id", columnList = "fulfillment_id"),
    @Index(name = "idx_delivery_attempt_status", columnList = "attempt_status"),
    @Index(name = "idx_delivery_attempt_tenant", columnList = "tenant_id"),
    @Index(name = "idx_delivery_attempt_next_attempt", columnList = "next_attempt_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;
    
    @Column(name = "delivery_id", insertable = false, updatable = false)
    private UUID deliveryId;
    
    @Column(name = "fulfillment_id")
    private UUID fulfillmentId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    // Attempt details
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;
    
    @Column(name = "attempted_at", nullable = false)
    @Builder.Default
    private LocalDateTime attemptedAt = LocalDateTime.now();
    
    @Column(name = "attempted_by_user_id")
    private UUID attemptedByUserId; // Driver/agent user ID
    
    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_status", nullable = false, length = 50)
    private AttemptStatus attemptStatus;
    
    // Failure details
    @Column(name = "failure_reason", length = 200)
    private String failureReason;
    
    @Column(name = "failure_code", length = 50)
    private String failureCode;
    // CUSTOMER_NOT_AVAILABLE, WRONG_ADDRESS, DAMAGED_GOODS, REFUSED, etc.
    
    // Location
    @Column(name = "attempt_latitude", precision = 10, scale = 8)
    private BigDecimal attemptLatitude;
    
    @Column(name = "attempt_longitude", precision = 11, scale = 8)
    private BigDecimal attemptLongitude;
    
    @Column(name = "attempt_location_description", length = 500)
    private String attemptLocationDescription;
    
    // Photos/Evidence
    @ElementCollection
    @CollectionTable(name = "delivery_attempt_photos", joinColumns = @JoinColumn(name = "attempt_id"))
    @Column(name = "photo_url")
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Next attempt
    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;
    
    @Column(name = "next_attempt_scheduled")
    @Builder.Default
    private Boolean nextAttemptScheduled = false;
    
    // Metadata
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AttemptStatus {
        SUCCESSFUL,
        FAILED,
        CANCELLED
    }
}

