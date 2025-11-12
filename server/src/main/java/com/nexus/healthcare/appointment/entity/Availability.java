package com.nexus.healthcare.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Availability Entity
 * 
 * <p>Doctor's regular schedule (day of week, time ranges)
 */
@Entity
@Table(name = "availabilities", indexes = {
    @Index(name = "idx_availabilities_doctor", columnList = "doctor_id"),
    @Index(name = "idx_availabilities_doctor_day", columnList = "doctor_id, day_of_week"),
    @Index(name = "idx_availabilities_tenant", columnList = "tenant_id, domain_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Availability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "doctor_id")
    private UUID doctorId;

    /**
     * Day of week: 1=Monday, 7=Sunday
     */
    @Column(nullable = false, name = "day_of_week")
    private Integer dayOfWeek;

    @Column(nullable = false, name = "start_time")
    private LocalTime startTime;

    @Column(nullable = false, name = "end_time")
    private LocalTime endTime;

    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

    @Column(nullable = false, name = "domain_id")
    private UUID domainId;

    @Column(nullable = false, name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}

