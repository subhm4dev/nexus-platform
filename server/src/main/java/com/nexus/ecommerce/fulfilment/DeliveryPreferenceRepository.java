package com.nexus.ecommerce.fulfilment.repository;

import com.nexus.ecommerce.fulfilment.entity.DeliveryPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryPreferenceRepository extends JpaRepository<DeliveryPreference, UUID> {
    
    Optional<DeliveryPreference> findByFulfillmentId(UUID fulfillmentId);
    
    Optional<DeliveryPreference> findByDeliveryId(UUID deliveryId);
    
    List<DeliveryPreference> findByCustomerUserIdAndIsActive(
        UUID customerUserId,
        Boolean isActive
    );
    
    @Query("SELECT dp FROM DeliveryPreference dp WHERE dp.tenantId = :tenantId " +
           "AND dp.scheduledDeliveryDate = :date AND dp.isActive = true")
    List<DeliveryPreference> findScheduledForDate(
        @Param("tenantId") UUID tenantId,
        @Param("date") LocalDate date
    );
}

