package com.nexus.shared.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Receipt Entity
 * 
 * <p>Stores payment receipts (PDF, thermal print, digital)
 */
@Entity
@Table(name = "payment_receipts", indexes = {
    @Index(name = "idx_receipts_payment", columnList = "payment_id"),
    @Index(name = "idx_receipts_number", columnList = "receipt_number"),
    @Index(name = "idx_receipts_tenant", columnList = "tenant_id, domain_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReceipt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;
    
    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    private String receiptNumber; // Format: RCP-YYYY-MMDD-####
    
    @Column(name = "receipt_type", nullable = false, length = 50)
    private String receiptType; // INVOICE, RECEIPT, REFUND
    
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
    
    @Column(name = "generated_by", nullable = false)
    private UUID generatedBy;
    
    @Column(name = "file_path", length = 500)
    private String filePath; // PDF file path
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "domain_id", nullable = false)
    private UUID domainId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

