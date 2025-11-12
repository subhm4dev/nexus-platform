package com.nexus.healthcare.appointment.repository;

import com.nexus.healthcare.appointment.entity.TimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TimeOffRepository extends JpaRepository<TimeOff, UUID> {
    
    @Query("SELECT t FROM TimeOff t WHERE " +
           "t.doctorId = :doctorId AND " +
           "((t.startTime BETWEEN :start AND :end) OR (t.endTime BETWEEN :start AND :end) OR " +
           "(t.startTime <= :start AND t.endTime >= :end))")
    List<TimeOff> findByDoctorIdAndStartTimeBetweenOrEndTimeBetween(
        @Param("doctorId") UUID doctorId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
    
    List<TimeOff> findByDoctorIdAndTenantIdAndDomainId(UUID doctorId, UUID tenantId, UUID domainId);
}

