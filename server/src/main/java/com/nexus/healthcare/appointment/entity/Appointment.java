package com.nexus.healthcare.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Appointment Entity
 * 
 * <p>Stores appointment bookings with version for optimistic locking to prevent double-booking.
 */
@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_appointments_doctor_date", columnList = "doctor_id, start_time"),
    @Index(name = "idx_appointments_patient", columnList = "patient_id"),
    @Index(name = "idx_appointments_status", columnList = "status"),
    @Index(name = "idx_appointments_payment_status", columnList = "payment_status"),
    @Index(name = "idx_appointments_tenant", columnList = "tenant_id, domain_id"),
    @Index(name = "idx_appointments_payment_id", columnList = "payment_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "doctor_id")
    private UUID doctorId;

    @Column(nullable = false, name = "patient_id")
    private UUID patientId;

    @Column(nullable = false, name = "session_type_id")
    private UUID sessionTypeId;

    @Column(nullable = false, name = "start_time")
    private LocalDateTime startTime;

    @Column(nullable = false, name = "end_time")
    private LocalDateTime endTime;

    @Column(nullable = false)
    @Builder.Default
    private String status = "SCHEDULED"; // SCHEDULED, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW

    @Column(name = "payment_status")
    private String paymentStatus; // PAYMENT_PENDING, PAYMENT_PARTIAL, PAYMENT_COMPLETED, PAYMENT_REFUNDED

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

    @Column(nullable = false, name = "domain_id")
    private UUID domainId;

    /**
     * Version for optimistic locking (prevents concurrent booking conflicts)
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @CreatedDate
    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}

