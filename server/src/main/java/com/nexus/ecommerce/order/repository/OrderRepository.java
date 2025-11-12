package com.nexus.ecommerce.order.repository;

import com.nexus.ecommerce.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Order Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    Page<Order> findByUserIdAndTenantId(UUID userId, UUID tenantId, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.tenantId = :tenantId " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<Order> findByUserIdAndTenantIdAndStatus(
        @Param("userId") UUID userId,
        @Param("tenantId") UUID tenantId,
        @Param("status") Order.OrderStatus status,
        Pageable pageable
    );
    
    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<Order> findByTenantIdAndStatus(
        @Param("tenantId") UUID tenantId,
        @Param("status") Order.OrderStatus status,
        Pageable pageable
    );
    
    /**
     * Find order by payment ID, user ID, and tenant ID
     * Used for idempotency checks in checkout service
     */
    Optional<Order> findByPaymentIdAndUserIdAndTenantId(
        UUID paymentId,
        UUID userId,
        UUID tenantId
    );
    
    /**
     * Find orders for multiple tenants (for tenant hierarchy)
     * 
     * @param tenantIds List of tenant IDs (parent + children)
     * @param pageable Pagination
     * @return Page of orders
     */
    Page<Order> findByTenantIdIn(List<UUID> tenantIds, Pageable pageable);
    
    /**
     * Find orders by user and multiple tenant IDs
     * 
     * @param userId User ID
     * @param tenantIds List of tenant IDs
     * @param pageable Pagination
     * @return Page of orders
     */
    Page<Order> findByUserIdAndTenantIdIn(UUID userId, List<UUID> tenantIds, Pageable pageable);
    
    /**
     * Find orders by tenant hierarchy and status
     * 
     * @param tenantIds List of tenant IDs
     * @param status Optional order status
     * @param pageable Pagination
     * @return Page of orders
     */
    @Query("SELECT o FROM Order o WHERE o.tenantId IN :tenantIds " +
           "AND (:status IS NULL OR o.status = :status)")
    Page<Order> findByTenantIdInAndStatus(
        @Param("tenantIds") List<UUID> tenantIds,
        @Param("status") Order.OrderStatus status,
        Pageable pageable
    );
}

