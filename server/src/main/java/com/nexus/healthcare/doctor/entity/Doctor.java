package com.nexus.healthcare.doctor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Doctor Entity
 * 
 * <p>Stores doctor profile information including basic details, verification status,
 * and links to specializations, qualifications, and awards.
 */
@Entity
@Table(name = "doctors", indexes = {
    @Index(name = "idx_doctors_user_id", columnList = "user_id"),
    @Index(name = "idx_doctors_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_doctors_domain_id", columnList = "domain_id"),
    @Index(name = "idx_doctors_domain_tenant", columnList = "domain_id, tenant_id"),
    @Index(name = "idx_doctors_verification_status", columnList = "verification_status"),
    @Index(name = "idx_doctors_deleted", columnList = "deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User ID from Identity service (references user_accounts.id)
     */
    @Column(nullable = false, name = "user_id")
    private UUID userId;

    /**
     * Tenant ID (BRANCH tenant for healthcare)
     */
    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

    /**
     * Domain ID (references domains.id - always 'healthcare' domain)
     */
    @Column(nullable = false, name = "domain_id")
    private UUID domainId;

    /**
     * Doctor registration number (e.g., medical license number)
     */
    @Column(name = "registration_number", unique = true)
    private String registrationNumber;

    /**
     * Verification status (PENDING, VERIFIED, REJECTED)
     */
    @Column(nullable = false, name = "verification_status")
    @Builder.Default
    private String verificationStatus = "PENDING";

    /**
     * Years of experience
     */
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    /**
     * Consultation fee
     */
    @Column(name = "consultation_fee", precision = 19, scale = 2)
    private java.math.BigDecimal consultationFee;

    /**
     * Bio/description
     */
    @Column(columnDefinition = "TEXT")
    private String bio;

    /**
     * Profile image URL
     */
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    /**
     * Soft delete flag
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * Timestamp when doctor was soft deleted
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Created timestamp (auto-populated by JPA auditing)
     */
    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last updated timestamp (auto-populated by JPA auditing)
     */
    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}

