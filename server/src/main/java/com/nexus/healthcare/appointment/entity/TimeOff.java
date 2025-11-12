package com.nexus.healthcare.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TimeOff Entity
 * 
 * <p>Doctor's time-off periods (holidays, leaves, etc.)
 */
@Entity
@Table(name = "time_offs", indexes = {
    @Index(name = "idx_timeoffs_doctor", columnList = "doctor_id"),
    @Index(name = "idx_timeoffs_doctor_date", columnList = "doctor_id, start_time, end_time"),
    @Index(name = "idx_timeoffs_tenant", columnList = "tenant_id, domain_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TimeOff {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "doctor_id")
    private UUID doctorId;

    @Column(nullable = false, name = "start_time")
    private LocalDateTime startTime;

    @Column(nullable = false, name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "reason")
    private String reason;

    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

    @Column(nullable = false, name = "domain_id")
    private UUID domainId;

    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}

