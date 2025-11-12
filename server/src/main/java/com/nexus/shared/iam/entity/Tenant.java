package com.nexus.shared.iam.entity;

import com.nexus.shared.iam.constants.TenantStatus;
import com.nexus.shared.iam.constants.TenantType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TenantStatus status;

    /**
     * Domain identifier: References domains.id
     * Used to distinguish between different business domains in the platform.
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
     * Parent tenant ID for branch/child tenants
     * NULL for top-level tenants (APP type)
     * References another tenant in the same domain
     */
    @Column(name = "parent_tenant_id")
    private UUID parentTenantId;
    
    /**
     * Parent tenant relationship (optional, for convenience)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_tenant_id", insertable = false, updatable = false)
    private Tenant parentTenant;
    
    /**
     * Child tenants (branches) - inverse relationship
     */
    @OneToMany(mappedBy = "parentTenant", fetch = FetchType.LAZY)
    private List<Tenant> childTenants;

    /**
     * Tenant type: APP, BRANCH, MARKETPLACE, SELLER
     * Defaults to APP for new tenants
     */
    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TenantType type = TenantType.APP;

    @CreatedDate
    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;
}
