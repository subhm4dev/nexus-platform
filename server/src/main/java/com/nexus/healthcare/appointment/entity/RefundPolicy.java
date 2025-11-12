package com.nexus.healthcare.appointment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * RefundPolicy Entity
 * 
 * <p>Configurable refund policies for cancellations and no-shows
 */
@Entity
@Table(name = "refund_policies", indexes = {
    @Index(name = "idx_refund_policies_tenant", columnList = "tenant_id, domain_id"),
    @Index(name = "idx_refund_policies_type", columnList = "policy_type, is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RefundPolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

    @Column(nullable = false, name = "domain_id")
    private UUID domainId;

    /**
     * Policy type: CANCELLATION, NO_SHOW
     */
    @Column(nullable = false, name = "policy_type")
    private String policyType;

    /**
     * Refund percentage: 100.00 for full, 50.00 for partial
     */
    @Column(nullable = false, name = "refund_percentage", precision = 5, scale = 2)
    private BigDecimal refundPercentage;

    /**
     * Hours before appointment (for cancellation policy)
     */
    @Column(name = "applicable_hours_before")
    private Integer applicableHoursBefore;

    @Column(nullable = false, name = "is_automatic")
    @Builder.Default
    private Boolean isAutomatic = false;

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

