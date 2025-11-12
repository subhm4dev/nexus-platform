package com.nexus.ecommerce.order.repository;

import com.nexus.ecommerce.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Order Status History Repository
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {
    
    List<OrderStatusHistory> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}

