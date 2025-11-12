package com.nexus.healthcare.appointment.repository;

import com.nexus.healthcare.appointment.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {
    
    List<Availability> findByDoctorIdAndDayOfWeekAndIsActiveTrue(UUID doctorId, Integer dayOfWeek);
    
    List<Availability> findByDoctorIdAndTenantIdAndDomainId(UUID doctorId, UUID tenantId, UUID domainId);
}

