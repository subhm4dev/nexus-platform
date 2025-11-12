package com.nexus.ecommerce.fulfilment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Age Verification Entity
 * Tracks age verification for restricted items (alcohol, etc.)
 * Supports both customer and alternate recipient verification
 */
@Entity
@Table(name = "age_verifications", indexes = {
    @Index(name = "idx_age_verification_delivery_id", columnList = "delivery_id"),
    @Index(name = "idx_age_verification_confirmation_id", columnList = "delivery_confirmation_id"),
    @Index(name = "idx_age_verification_status", columnList = "verification_status"),
    @Index(name = "idx_age_verification_customer", columnList = "customer_user_id"),
    @Index(name = "idx_age_verification_alternate", columnList = "alternate_recipient_id"),
    @Index(name = "idx_age_verification_verified_user", columnList = "verified_user_id"),
    @Index(name = "idx_age_verification_tenant", columnList = "tenant_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgeVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_confirmation_id", nullable = false)
    private DeliveryConfirmation deliveryConfirmation;
    
    @Column(name = "delivery_confirmation_id", insertable = false, updatable = false)
    private UUID deliveryConfirmationId;
    
    @Column(name = "delivery_id", nullable = false)
    private UUID deliveryId;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "customer_user_id", nullable = false)
    private UUID customerUserId; // Original customer
    
    // Age requirement
    @Column(name = "minimum_age_required", nullable = false)
    private Integer minimumAgeRequired; // 18 or 21
    
    @Column(name = "customer_date_of_birth")
    private LocalDate customerDateOfBirth;
    
    // Verification method
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method", nullable = false, length = 50)
    private VerificationMethod verificationMethod;
    
    // Photo verification
    @Column(name = "customer_photo_url", length = 500)
    private String customerPhotoUrl;
    
    @Column(name = "id_photo_url", length = 500)
    private String idPhotoUrl;
    
    @Column(name = "id_type", length = 50)
    private String idType; // AADHAAR, PAN, DRIVING_LICENSE, PASSPORT
    
    @Column(name = "id_number", length = 100)
    private String idNumber;
    
    // Aadhaar Face RD verification
    @Column(name = "aadhaar_number", length = 12)
    private String aadhaarNumber;
    
    @Column(name = "aadhaar_reference_id", length = 100)
    private String aadhaarReferenceId;
    
    @Column(name = "aadhaar_transaction_id", length = 100)
    private String aadhaarTransactionId;
    
    @Column(name = "aadhaar_otp_reference_id", length = 100)
    private String aadhaarOtpReferenceId;
    
    @Column(name = "biometric_match_score", precision = 5, scale = 2)
    private java.math.BigDecimal biometricMatchScore;
    
    @Column(name = "aadhaar_verified")
    @Builder.Default
    private Boolean aadhaarVerified = false;
    
    @Column(name = "aadhaar_demographic_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> aadhaarDemographicData;
    
    // Verification result
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 50)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    @Column(name = "age_verified")
    @Builder.Default
    private Boolean ageVerified = false;
    
    @Column(name = "verified_age")
    private Integer verifiedAge;
    
    @Column(name = "verification_confidence", precision = 5, scale = 2)
    private java.math.BigDecimal verificationConfidence;
    
    // Alternate recipient support
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alternate_recipient_id")
    private AlternateRecipient alternateRecipient;
    
    @Column(name = "alternate_recipient_id", insertable = false, updatable = false)
    private UUID alternateRecipientId;
    
    @Column(name = "verified_by_alternate")
    @Builder.Default
    private Boolean verifiedByAlternate = false;
    
    @Column(name = "verified_user_id")
    private UUID verifiedUserId; // The actual user who was verified (customer OR alternate)
    
    @Column(name = "verified_user_phone", length = 20)
    private String verifiedUserPhone;
    
    @Column(name = "verified_user_name", length = 200)
    private String verifiedUserName;
    
    // Metadata
    @Column(name = "verified_by_system")
    @Builder.Default
    private Boolean verifiedBySystem = true;
    
    @Column(name = "verified_by_admin")
    private UUID verifiedByAdmin;
    
    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    public enum VerificationMethod {
        PHOTO_VERIFICATION,
        AADHAAR_FACE_RD,
        ID_VERIFICATION,
        VIDEO_KYC
    }
    
    public enum VerificationStatus {
        PENDING,
        VERIFICATION_IN_PROGRESS,
        VERIFIED,
        FAILED,
        REJECTED,
        MANUAL_REVIEW
    }
}

