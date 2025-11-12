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
 * Alternate Recipient Entity
 * Allows customers to share delivery links with alternate users who can receive orders
 */
@Entity
@Table(name = "alternate_recipients", indexes = {
    @Index(name = "idx_alternate_recipient_delivery_id", columnList = "delivery_id"),
    @Index(name = "idx_alternate_recipient_confirmation_id", columnList = "delivery_confirmation_id"),
    @Index(name = "idx_alternate_recipient_token", columnList = "share_token"),
    @Index(name = "idx_alternate_recipient_phone", columnList = "alternate_phone_number"),
    @Index(name = "idx_alternate_recipient_user", columnList = "alternate_user_id"),
    @Index(name = "idx_alternate_recipient_status", columnList = "status"),
    @Index(name = "idx_alternate_recipient_customer", columnList = "customer_user_id"),
    @Index(name = "idx_alternate_recipient_expires", columnList = "expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlternateRecipient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;
    
    @Column(name = "delivery_id", insertable = false, updatable = false)
    private UUID deliveryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_confirmation_id")
    private DeliveryConfirmation deliveryConfirmation;
    
    @Column(name = "delivery_confirmation_id", insertable = false, updatable = false)
    private UUID deliveryConfirmationId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "customer_user_id", nullable = false)
    private UUID customerUserId; // Original customer who shared the link
    
    // Alternate recipient details
    @Column(name = "alternate_user_id")
    private UUID alternateUserId; // User ID if they have account
    
    @Column(name = "alternate_phone_number", length = 20)
    private String alternatePhoneNumber; // Phone number (can be used without account)
    
    @Column(name = "alternate_name", length = 200)
    private String alternateName; // Name of alternate recipient
    
    @Column(name = "alternate_email", length = 200)
    private String alternateEmail; // Email (optional)
    
    // Sharing details
    @Column(name = "share_token", length = 100, unique = true, nullable = false)
    private String shareToken; // Unique token for sharing link
    
    @Column(name = "share_link", length = 500)
    private String shareLink; // Full shareable link
    
    @Column(name = "shared_at", nullable = false)
    @Builder.Default
    private LocalDateTime sharedAt = LocalDateTime.now();
    
    @Column(name = "shared_by_user_id", nullable = false)
    private UUID sharedByUserId; // User who shared (customer or admin)
    
    @Column(name = "shared_via", length = 50)
    @Builder.Default
    private String sharedVia = "SMS"; // SMS, EMAIL, WHATSAPP, LINK
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private RecipientStatus status = RecipientStatus.PENDING;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "confirmed_by_user_id")
    private UUID confirmedByUserId; // Which alternate user confirmed
    
    // Confirmation details (when alternate user confirms)
    @Column(name = "confirmed_latitude", precision = 10, scale = 8)
    private BigDecimal confirmedLatitude;
    
    @Column(name = "confirmed_longitude", precision = 11, scale = 8)
    private BigDecimal confirmedLongitude;
    
    @Column(name = "confirmed_location_accuracy", precision = 5, scale = 2)
    private BigDecimal confirmedLocationAccuracy;
    
    @Column(name = "proximity_verified")
    @Builder.Default
    private Boolean proximityVerified = false;
    
    @Column(name = "distance_to_agent", precision = 8, scale = 2)
    private BigDecimal distanceToAgent; // Distance to agent when confirmed
    
    // Expiry
    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Link expiry (default 24 hours)
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    @Column(name = "revoked_by_user_id")
    private UUID revokedByUserId;
    
    @Column(name = "revoke_reason", length = 200)
    private String revokeReason;
    
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
    
    public enum RecipientStatus {
        PENDING,    // Link shared, waiting for alternate user
        ACTIVE,     // Link active, alternate user can confirm
        CONFIRMED,  // Alternate user confirmed delivery
        EXPIRED,    // Link expired
        REVOKED     // Link revoked by customer/admin
    }
}

