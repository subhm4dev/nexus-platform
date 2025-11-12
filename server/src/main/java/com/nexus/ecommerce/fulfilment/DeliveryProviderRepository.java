package com.nexus.ecommerce.fulfilment.repository;

import com.nexus.ecommerce.fulfilment.entity.DeliveryProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Delivery Provider Repository
 */
@Repository
public interface DeliveryProviderRepository extends JpaRepository<DeliveryProvider, UUID> {
    
    Optional<DeliveryProvider> findByProviderCodeAndTenantId(
        DeliveryProvider.ProviderCode providerCode, 
        UUID tenantId
    );
    
    List<DeliveryProvider> findByTenantIdAndIsActiveTrue(UUID tenantId);
    
    @Query("SELECT dp FROM DeliveryProvider dp WHERE " +
           "dp.tenantId = :tenantId AND dp.isActive = true AND " +
           "(:isIntercity = true AND dp.providerType = 'INTERCITY') OR " +
           "(:isIntercity = false AND dp.providerType = 'INTRACITY') OR " +
           "dp.providerType = 'OWN_FLEET'")
    List<DeliveryProvider> findActiveProvidersByType(
        @Param("tenantId") UUID tenantId,
        @Param("isIntercity") boolean isIntercity
    );
}

