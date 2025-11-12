package com.nexus.shared.iam.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Domain Entity
 * 
 * <p>Junction table tracking which domains a user belongs to.
 * A user can be part of multiple domains (e.g., ecommerce and hospital).
 * 
 * <p>This enables users to have separate profiles, addresses, and other
 * domain-specific data across different business domains.
 */
@Entity
@Table(name = "user_domains", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "domain_id", "tenant_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserDomain {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User ID from user_accounts table
     */
    @Column(nullable = false, name = "user_id")
    private UUID userId;

    /**
     * Domain identifier: References domains.id
     */
    @Column(nullable = false, name = "domain_id")
    private UUID domainId;
    
    /**
     * Domain relationship (optional, for convenience)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private Domain domain;

    /**
     * Tenant ID within the domain (business entity: seller, provider, etc.)
     */
    @Column(nullable = false, name = "tenant_id")
    private UUID tenantId;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;
}

