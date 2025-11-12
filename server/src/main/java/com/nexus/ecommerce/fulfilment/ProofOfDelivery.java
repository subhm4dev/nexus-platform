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
 * Proof of Delivery Entity
 * Stores POD photos, signatures, and OTP verification
 */
@Entity
@Table(name = "proof_of_delivery", indexes = {
    @Index(name = "idx_pod_delivery_id", columnList = "delivery_id"),
    @Index(name = "idx_pod_fulfillment_id", columnList = "fulfillment_id"),
    @Index(name = "idx_pod_confirmation_id", columnList = "delivery_confirmation_id"),
    @Index(name = "idx_pod_status", columnList = "pod_status"),
    @Index(name = "idx_pod_tenant", columnList = "tenant_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProofOfDelivery {
    
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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_confirmation_id")
    private DeliveryConfirmation deliveryConfirmation;
    
    @Column(name = "delivery_confirmation_id", insertable = false, updatable = false)
    private UUID deliveryConfirmationId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    // POD type
    @Enumerated(EnumType.STRING)
    @Column(name = "pod_type", nullable = false, length = 50)
    @Builder.Default
    private PODType podType = PODType.PHOTO;
    
    // Photo POD
    @ElementCollection
    @CollectionTable(name = "pod_photos", joinColumns = @JoinColumn(name = "pod_id"))
    @Column(name = "photo_url")
    @Builder.Default
    private List<String> photoUrls = new ArrayList<>();
    
    @Column(name = "photo_taken_at")
    private LocalDateTime photoTakenAt;
    
    @Column(name = "photo_taken_by_user_id")
    private UUID photoTakenByUserId;
    
    // Signature POD
    @Column(name = "signature_url", length = 500)
    private String signatureUrl;
    
    @Column(name = "signature_data", columnDefinition = "TEXT")
    private String signatureData; // Base64 signature data
    
    @Column(name = "signature_taken_at")
    private LocalDateTime signatureTakenAt;
    
    @Column(name = "signature_taken_by_user_id")
    private UUID signatureTakenByUserId;
    
    @Column(name = "recipient_name", length = 200)
    private String recipientName;
    
    // OTP POD
    @Column(name = "otp_verified")
    @Builder.Default
    private Boolean otpVerified = false;
    
    @Column(name = "otp_code", length = 10)
    private String otpCode;
    
    @Column(name = "otp_verified_at")
    private LocalDateTime otpVerifiedAt;
    
    @Column(name = "otp_phone_number", length = 20)
    private String otpPhoneNumber;
    
    // Video POD
    @Column(name = "video_url", length = 500)
    private String videoUrl;
    
    @Column(name = "video_taken_at")
    private LocalDateTime videoTakenAt;
    
    // Location
    @Column(name = "pod_latitude", precision = 10, scale = 8)
    private BigDecimal podLatitude;
    
    @Column(name = "pod_longitude", precision = 11, scale = 8)
    private BigDecimal podLongitude;
    
    @Column(name = "pod_location_description", length = 500)
    private String podLocationDescription;
    
    // Recipient details
    @Column(name = "received_by_name", length = 200)
    private String receivedByName;
    
    @Column(name = "received_by_phone", length = 20)
    private String receivedByPhone;
    
    @Column(name = "received_by_relation", length = 50)
    private String receivedByRelation;
    // SELF, FAMILY_MEMBER, NEIGHBOR, SECURITY, etc.
    
    @Column(name = "is_alternate_recipient")
    @Builder.Default
    private Boolean isAlternateRecipient = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternate_recipient_id")
    private AlternateRecipient alternateRecipient;
    
    @Column(name = "alternate_recipient_id", insertable = false, updatable = false)
    private UUID alternateRecipientId;
    
    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "pod_status", nullable = false, length = 50)
    @Builder.Default
    private PODStatus podStatus = PODStatus.PENDING;
    
    // Metadata
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @Column(name = "verified_by_user_id")
    private UUID verifiedByUserId; // Admin who verified
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum PODType {
        PHOTO,
        SIGNATURE,
        OTP,
        VIDEO,
        COMBINATION
    }
    
    public enum PODStatus {
        PENDING,
        COMPLETED,
        REJECTED,
        MANUAL_REVIEW
    }
}

