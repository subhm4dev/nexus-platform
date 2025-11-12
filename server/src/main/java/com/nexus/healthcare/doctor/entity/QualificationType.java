package com.nexus.healthcare.doctor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Qualification Type Entity (Master Data)
 * 
 * <p>Master data for medical qualification types (e.g., MBBS, MD, PhD).
 * Shared across all tenants and domains.
 * This is the reference data that doctors can select from when adding their qualifications.
 */
@Entity
@Table(name = "qualification_types", indexes = {
    @Index(name = "idx_qualification_types_code", columnList = "code", unique = true),
    @Index(name = "idx_qualification_types_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class QualificationType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Qualification code (unique identifier, e.g., "MBBS")
     */
    @Column(nullable = false, unique = true)
    private String code;

    /**
     * Qualification name (e.g., "Bachelor of Medicine, Bachelor of Surgery")
     */
    @Column(nullable = false)
    private String name;

    /**
     * Description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Active status
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Created timestamp
     */
    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last updated timestamp
     */
    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}
