package com.nexus.healthcare.doctor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Award Entity
 * 
 * <p>Stores doctor awards and recognitions
 */
@Entity
@Table(name = "awards", indexes = {
    @Index(name = "idx_awards_doctor", columnList = "doctor_id"),
    @Index(name = "idx_awards_tenant", columnList = "tenant_id"),
    @Index(name = "idx_awards_domain", columnList = "domain_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Award {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "doctor_id")
    private UUID doctorId;

    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

    @Column(nullable = false, name = "domain_id")
    private UUID domainId;

    /**
     * Award name
     */
    @Column(nullable = false)
    private String name;

    /**
     * Awarding organization
     */
    @Column(name = "organization")
    private String organization;

    /**
     * Year received
     */
    @Column(name = "year")
    private Integer year;

    /**
     * Description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Certificate/document URL
     */
    @Column(name = "certificate_url")
    private String certificateUrl;

    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}

