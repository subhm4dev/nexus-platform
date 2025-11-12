package com.nexus.healthcare.appointment.repository;

import com.nexus.healthcare.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    
    Optional<Appointment> findByIdAndTenantIdAndDomainIdAndDeletedFalse(UUID id, UUID tenantId, UUID domainId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.id = :id AND a.tenantId = :tenantId AND a.domainId = :domainId AND a.deleted = false")
    Optional<Appointment> findByIdAndTenantIdAndDomainIdAndDeletedFalseWithLock(
        @Param("id") UUID id, 
        @Param("tenantId") UUID tenantId, 
        @Param("domainId") UUID domainId
    );
    
    List<Appointment> findByDoctorIdAndStartTimeBetweenAndDeletedFalseAndStatusNot(
        UUID doctorId, LocalDateTime start, LocalDateTime end, String status);
    
    List<Appointment> findByPatientIdAndTenantIdAndDomainIdAndDeletedFalse(UUID patientId, UUID tenantId, UUID domainId);
    
    List<Appointment> findByTenantIdAndDomainIdAndDeletedFalse(UUID tenantId, UUID domainId);
    
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.tenantId IN :tenantIds AND " +
           "a.domainId = :domainId AND " +
           "a.deleted = false AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:paymentStatus IS NULL OR a.paymentStatus = :paymentStatus)")
    List<Appointment> searchAppointments(
        @Param("tenantIds") List<UUID> tenantIds,
        @Param("domainId") UUID domainId,
        @Param("status") String status,
        @Param("paymentStatus") String paymentStatus);
}
