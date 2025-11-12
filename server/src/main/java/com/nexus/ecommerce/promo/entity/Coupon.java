package com.nexus.ecommerce.promo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Coupon Entity
 */
@Entity
@Table(name = "coupons", indexes = {
    @Index(name = "idx_coupons_tenant", columnList = "tenant_id"),
    @Index(name = "idx_coupons_code", columnList = "code"),
    @Index(name = "idx_coupons_active", columnList = "active"),
    @Index(name = "idx_coupons_expiry", columnList = "expiry_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Coupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(name = "discount_type", nullable = false, length = 50)
    private String discountType; // PERCENTAGE, FIXED
    
    @Column(name = "discount_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "usage_limit")
    private Integer usageLimit; // NULL means unlimited
    
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(name = "min_order_value", precision = 19, scale = 2)
    private BigDecimal minOrderValue;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

