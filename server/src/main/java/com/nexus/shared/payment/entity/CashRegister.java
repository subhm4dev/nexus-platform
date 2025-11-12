package com.nexus.shared.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cash Register Entity
 * 
 * <p>Manages cash register (opening/closing balance per day)
 */
@Entity
@Table(name = "cash_register", indexes = {
    @Index(name = "idx_cash_register_tenant", columnList = "tenant_id, domain_id"),
    @Index(name = "idx_cash_register_date", columnList = "register_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashRegister {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "domain_id", nullable = false)
    private UUID domainId;
    
    @Column(name = "register_date", nullable = false)
    private LocalDate registerDate;
    
    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;
    
    @Column(name = "cash_received", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal cashReceived = BigDecimal.ZERO;
    
    @Column(name = "cash_disbursed", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal cashDisbursed = BigDecimal.ZERO;
    
    @Column(name = "closing_balance", precision = 19, scale = 2)
    private BigDecimal closingBalance;
    
    @Column(name = "opened_by", nullable = false)
    private UUID openedBy;
    
    @Column(name = "closed_by")
    private UUID closedBy;
    
    @Column(name = "opened_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime openedAt = LocalDateTime.now();
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}

