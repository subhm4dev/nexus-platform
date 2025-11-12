package com.nexus.healthcare.doctor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Doctor Specialization Join Entity
 * 
 * <p>Many-to-many relationship between doctors and specializations.
 */
@Entity
@Table(name = "doctor_specializations", indexes = {
    @Index(name = "idx_doctor_spec_doctor", columnList = "doctor_id"),
    @Index(name = "idx_doctor_spec_specialization", columnList = "specialization_id"),
    @Index(name = "idx_doctor_spec_unique", columnList = "doctor_id, specialization_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class DoctorSpecialization {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "doctor_id")
    private UUID doctorId;

    @Column(nullable = false, name = "specialization_id")
    private UUID specializationId;

    /**
     * Is primary specialization
     */
    @Column(nullable = false, name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    /**
     * Years of experience in this specialization
     */
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

