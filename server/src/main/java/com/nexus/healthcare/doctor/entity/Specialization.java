package com.nexus.healthcare.doctor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Specialization Entity (Master Data)
 * 
 * <p>Master data for medical specializations (e.g., Cardiology, Neurology).
 * Shared across all tenants and domains.
 */
@Entity
@Table(name = "specializations", indexes = {
    @Index(name = "idx_specializations_code", columnList = "code", unique = true),
    @Index(name = "idx_specializations_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Specialization {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Specialization code (unique identifier, e.g., "CARDIOLOGY")
     */
    @Column(nullable = false, unique = true)
    private String code;

    /**
     * Specialization name (e.g., "Cardiology")
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

