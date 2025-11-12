package com.nexus.healthcare.consultation.repository;

import com.nexus.healthcare.consultation.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
    
    Optional<Consultation> findByAppointmentId(UUID appointmentId);
    
    List<Consultation> findByPatientIdAndTenantIdAndDomainId(UUID patientId, UUID tenantId, UUID domainId);
    
    List<Consultation> findByDoctorIdAndTenantIdAndDomainId(UUID doctorId, UUID tenantId, UUID domainId);
    
    boolean existsByAppointmentId(UUID appointmentId);
}

