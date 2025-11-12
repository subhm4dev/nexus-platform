package com.nexus.ecommerce.inventory.repository;

import com.nexus.ecommerce.inventory.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByOrderIdAndTenantId(UUID orderId, UUID tenantId);
    
    @Query("SELECT r FROM Reservation r WHERE r.status = :status AND r.expiresAt < :now")
    List<Reservation> findByStatusAndExpiresAtBefore(@Param("status") String status, @Param("now") LocalDateTime now);
}

