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
 * Delivery Confirmation Entity
 * Implements dual-confirmation delivery system with proximity verification
 */
@Entity
@Table(name = "delivery_confirmations", indexes = {
    @Index(name = "idx_delivery_confirmation_delivery_id", columnList = "delivery_id"),
    @Index(name = "idx_delivery_confirmation_status", columnList = "confirmation_status"),
    @Index(name = "idx_delivery_confirmation_next_attempt", columnList = "next_attempt_at"),
    @Index(name = "idx_delivery_confirmation_tenant_id", columnList = "tenant_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryConfirmation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;
    
    @Column(name = "delivery_id", insertable = false, updatable = false)
    private UUID deliveryId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    // Agent confirmation
    @Column(name = "agent_confirmed")
    @Builder.Default
    private Boolean agentConfirmed = false;
    
    @Column(name = "agent_confirmed_at")
    private LocalDateTime agentConfirmedAt;
    
    @Column(name = "agent_latitude", precision = 10, scale = 8)
    private BigDecimal agentLatitude;
    
    @Column(name = "agent_longitude", precision = 11, scale = 8)
    private BigDecimal agentLongitude;
    
    @Column(name = "agent_location_accuracy", precision = 5, scale = 2)
    private BigDecimal agentLocationAccuracy; // in meters
    
    @Column(name = "agent_user_id")
    private UUID agentUserId;
    
    // Customer confirmation
    @Column(name = "customer_confirmed")
    @Builder.Default
    private Boolean customerConfirmed = false;
    
    @Column(name = "customer_confirmed_at")
    private LocalDateTime customerConfirmedAt;
    
    @Column(name = "customer_latitude", precision = 10, scale = 8)
    private BigDecimal customerLatitude;
    
    @Column(name = "customer_longitude", precision = 11, scale = 8)
    private BigDecimal customerLongitude;
    
    @Column(name = "customer_location_accuracy", precision = 5, scale = 2)
    private BigDecimal customerLocationAccuracy; // in meters
    
    @Column(name = "customer_user_id")
    private UUID customerUserId;
    
    // Proximity check (flexible location - parties can meet anywhere)
    @Column(name = "proximity_verified")
    @Builder.Default
    private Boolean proximityVerified = false;
    
    @Column(name = "distance_between_parties", precision = 8, scale = 2)
    private BigDecimal distanceBetweenParties; // in meters (KEY CHECK - parties must be close)
    
    @Column(name = "distance_to_delivery_address", precision = 8, scale = 2)
    private BigDecimal distanceToDeliveryAddress; // in meters (for records)
    
    @Column(name = "proximity_verified_at")
    private LocalDateTime proximityVerifiedAt;
    
    // Actual delivery location (where they actually met)
    @Column(name = "actual_delivery_latitude", precision = 10, scale = 8)
    private BigDecimal actualDeliveryLatitude;
    
    @Column(name = "actual_delivery_longitude", precision = 11, scale = 8)
    private BigDecimal actualDeliveryLongitude;
    
    @Column(name = "actual_delivery_address", columnDefinition = "TEXT")
    private String actualDeliveryAddress; // Optional description
    
    @Column(name = "delivery_location_type", length = 50)
    @Builder.Default
    private String deliveryLocationType = "SCHEDULED_ADDRESS";
    // SCHEDULED_ADDRESS, ALTERNATE_LOCATION, CUSTOMER_LOCATION
    
    // Not available tracking
    @Column(name = "agent_marked_unavailable")
    @Builder.Default
    private Boolean agentMarkedUnavailable = false;
    
    @Column(name = "agent_unavailable_at")
    private LocalDateTime agentUnavailableAt;
    
    @Column(name = "agent_unavailable_reason", length = 200)
    private String agentUnavailableReason;
    
    @Column(name = "customer_marked_unavailable")
    @Builder.Default
    private Boolean customerMarkedUnavailable = false;
    
    @Column(name = "customer_unavailable_at")
    private LocalDateTime customerUnavailableAt;
    
    @Column(name = "customer_unavailable_reason", length = 200)
    private String customerUnavailableReason;
    
    // Reschedule tracking
    @Column(name = "reschedule_count")
    @Builder.Default
    private Integer rescheduleCount = 0;
    
    @Column(name = "last_reschedule_at")
    private LocalDateTime lastRescheduleAt;
    
    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;
    
    @Column(name = "auto_return_initiated")
    @Builder.Default
    private Boolean autoReturnInitiated = false;
    
    // Age verification (for restricted items like alcohol)
    @Column(name = "requires_age_verification")
    @Builder.Default
    private Boolean requiresAgeVerification = false;
    
    @Column(name = "minimum_age_required")
    private Integer minimumAgeRequired; // 18 or 21
    
    @Column(name = "age_verified")
    @Builder.Default
    private Boolean ageVerified = false;
    
    @Column(name = "age_verification_method", length = 50)
    private String ageVerificationMethod;
    // PHOTO_VERIFICATION, AADHAAR_FACE_RD, ID_VERIFICATION, VIDEO_KYC
    
    @Column(name = "age_verification_status", length = 50)
    private String ageVerificationStatus;
    // PENDING, VERIFIED, FAILED, REJECTED
    
    @Column(name = "age_verification_at")
    private LocalDateTime ageVerificationAt;
    
    // Alternate recipient support (when customer is not available)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternate_recipient_id")
    private AlternateRecipient alternateRecipient;
    
    @Column(name = "alternate_recipient_id", insertable = false, updatable = false)
    private UUID alternateRecipientId;
    
    @Column(name = "confirmed_by_alternate")
    @Builder.Default
    private Boolean confirmedByAlternate = false;
    
    @Column(name = "alternate_recipient_name", length = 200)
    private String alternateRecipientName;
    
    @Column(name = "alternate_recipient_phone", length = 20)
    private String alternateRecipientPhone;
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "confirmation_status", nullable = false, length = 50)
    @Builder.Default
    private ConfirmationStatus confirmationStatus = ConfirmationStatus.PENDING;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ConfirmationStatus {
        PENDING,                    // Initial state, waiting for confirmations
        AGENT_CONFIRMED,            // Agent confirmed, waiting for customer
        CUSTOMER_CONFIRMED,         // Customer confirmed, waiting for agent
        BOTH_CONFIRMED,             // Both confirmed → DELIVERED
        AGENT_UNAVAILABLE,          // Agent marked customer unavailable
        CUSTOMER_UNAVAILABLE,       // Customer marked agent unavailable
        BOTH_UNAVAILABLE,           // Both marked unavailable → Reschedule
        RETURNED,                   // Auto-returned after 3 reschedules
        CONFLICT                    // Mismatch (requires manual review)
    }
}

