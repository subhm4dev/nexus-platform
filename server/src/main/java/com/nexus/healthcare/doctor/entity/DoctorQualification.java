package com.nexus.healthcare.doctor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Doctor Qualification Entity
 * 
 * <p>Stores doctor-specific qualifications (e.g., MBBS from XYZ University, 2010)
 */
@Entity
@Table(name = "qualifications", indexes = {
    @Index(name = "idx_qualifications_doctor", columnList = "doctor_id"),
    @Index(name = "idx_qualifications_tenant", columnList = "tenant_id"),
    @Index(name = "idx_qualifications_domain", columnList = "domain_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DoctorQualification {
    
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
     * Qualification name (e.g., "MBBS", "MD")
     */
    @Column(nullable = false)
    private String name;

    /**
     * Institution where qualification was obtained
     */
    @Column(name = "institution")
    private String institution;

    /**
     * Year obtained
     */
    @Column(name = "year")
    private Integer year;

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

