package com.nexus.ecommerce.fulfilment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Delivery Provider Entity
 * Supports third-party providers (BlueDart, Delhivery, Shiprocket, etc.) and own fleet
 */
@Entity
@Table(name = "delivery_providers", indexes = {
    @Index(name = "idx_delivery_provider_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_delivery_provider_code", columnList = "provider_code"),
    @Index(name = "idx_delivery_provider_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryProvider {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_code", nullable = false, length = 50)
    private ProviderCode providerCode;
    
    @Column(name = "provider_name", nullable = false, length = 200)
    private String providerName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 50)
    private ProviderType providerType;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "api_key", length = 500)
    private String apiKey;
    
    @Column(name = "api_secret", length = 500)
    private String apiSecret;
    
    @Column(name = "webhook_secret", length = 500)
    private String webhookSecret;
    
    @Column(name = "base_url", length = 500)
    private String baseUrl;
    
    @Column(name = "config", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> config;  // Provider-specific configuration
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum ProviderCode {
        BLUEDART,      // Intercity courier
        DELHIVERY,     // Intercity courier
        SHIPROCKET,    // Intercity courier aggregator
        DUNZO,         // Intracity hyperlocal
        RAPIDO,        // Intracity hyperlocal
        OWN_FLEET      // Own delivery fleet (like Ekart, Flipkart logistics)
    }
    
    public enum ProviderType {
        INTERCITY,     // Long-distance deliveries (BlueDart, Delhivery, Shiprocket)
        INTRACITY,     // Same-city deliveries (Dunzo, Rapido)
        OWN_FLEET      // Own delivery fleet
    }
}

