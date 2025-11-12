package com.nexus.ecommerce.inventory.repository;

import com.nexus.ecommerce.inventory.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    Optional<Stock> findBySkuAndLocationIdAndTenantId(String sku, UUID locationId, UUID tenantId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.sku = :sku AND s.locationId = :locationId AND s.tenantId = :tenantId")
    Optional<Stock> findBySkuAndLocationIdAndTenantIdForUpdate(@Param("sku") String sku, @Param("locationId") UUID locationId, @Param("tenantId") UUID tenantId);
    
    List<Stock> findBySkuAndTenantIdAndQtyOnHandGreaterThan(String sku, UUID tenantId, Integer qty);
    List<Stock> findBySkuAndTenantId(String sku, UUID tenantId);
    
    /**
     * Find stock by SKU and multiple tenant IDs (for tenant hierarchy)
     * 
     * @param sku SKU
     * @param tenantIds List of tenant IDs (parent + children)
     * @return List of stock entries
     */
    List<Stock> findBySkuAndTenantIdIn(String sku, List<UUID> tenantIds);
    
    /**
     * Find stock with available quantity for multiple tenants
     * 
     * @param sku SKU
     * @param tenantIds List of tenant IDs
     * @param qty Minimum quantity
     * @return List of stock entries
     */
    List<Stock> findBySkuAndTenantIdInAndQtyOnHandGreaterThan(String sku, List<UUID> tenantIds, Integer qty);
}

