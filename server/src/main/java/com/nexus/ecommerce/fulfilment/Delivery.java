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
 * Delivery Entity
 */
@Entity
@Table(name = "deliveries", indexes = {
    @Index(name = "idx_delivery_fulfillment_id", columnList = "fulfillment_id"),
    @Index(name = "idx_delivery_driver_id", columnList = "driver_id"),
    @Index(name = "idx_delivery_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_delivery_status", columnList = "status"),
    @Index(name = "idx_delivery_tracking_number", columnList = "tracking_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfillment_id", nullable = false)
    private Fulfillment fulfillment;
    
    @Column(name = "fulfillment_id", insertable = false, updatable = false)
    private UUID fulfillmentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, length = 50)
    @Builder.Default
    private DeliveryType deliveryType = DeliveryType.OWN_FLEET;
    
    @Column(name = "driver_id")
    private UUID driverId;  // NULL for third-party providers
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private DeliveryProvider provider;  // NULL for own fleet
    
    @Column(name = "provider_id", insertable = false, updatable = false)
    private UUID providerId;
    
    @Column(name = "provider_tracking_id", length = 200)
    private String providerTrackingId;  // Provider's tracking ID
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "current_location", length = 500)
    private String currentLocation;
    
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;
    
    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.ASSIGNED;
    
    @Column(name = "tracking_number", unique = true, length = 100)
    private String trackingNumber;
    
    @Column(name = "provider_status", length = 100)
    private String providerStatus;  // Provider's status (e.g., "In Transit", "Out for Delivery")
    
    // Proximity fields for dual-confirmation system
    @Column(name = "delivery_address_latitude", precision = 10, scale = 8)
    private BigDecimal deliveryAddressLatitude;
    
    @Column(name = "delivery_address_longitude", precision = 11, scale = 8)
    private BigDecimal deliveryAddressLongitude;
    
    @Column(name = "proximity_radius_meters")
    private Integer proximityRadiusMeters;
    
    @Column(name = "requires_dual_confirmation")
    @Builder.Default
    private Boolean requiresDualConfirmation = true;
    
    @Column(name = "confirmation_timeout_minutes")
    private Integer confirmationTimeoutMinutes;
    
    // Additional fields from requirements
    @Column(name = "attempt_count")
    @Builder.Default
    private Integer attemptCount = 0;
    
    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;
    
    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;
    
    @Column(name = "failure_reason", length = 200)
    private String failureReason;
    
    @Column(name = "cod_amount", precision = 10, scale = 2)
    private BigDecimal codAmount;
    
    @Column(name = "cod_collected")
    @Builder.Default
    private Boolean codCollected = false;
    
    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;
    
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TrackingHistory> trackingHistory = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum DeliveryType {
        OWN_FLEET,      // Own delivery fleet (uses driver_id)
        THIRD_PARTY     // Third-party provider (uses provider_id)
    }
    
    public enum DeliveryStatus {
        ASSIGNED,
        PICKED_UP,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        FAILED,
        RETURNED
    }
}

