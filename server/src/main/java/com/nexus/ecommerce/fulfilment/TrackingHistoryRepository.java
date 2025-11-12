package com.nexus.ecommerce.fulfilment.repository;

import com.nexus.ecommerce.fulfilment.entity.TrackingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Tracking History Repository
 */
@Repository
public interface TrackingHistoryRepository extends JpaRepository<TrackingHistory, UUID> {
    
    @Query("SELECT th FROM TrackingHistory th WHERE th.deliveryId = :deliveryId " +
           "ORDER BY th.createdAt DESC")
    List<TrackingHistory> findByDeliveryIdOrderByCreatedAtDesc(
        @Param("deliveryId") UUID deliveryId
    );
}

