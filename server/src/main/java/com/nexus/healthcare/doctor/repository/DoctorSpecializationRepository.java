package com.nexus.healthcare.doctor.repository;

import com.nexus.healthcare.doctor.entity.DoctorSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecialization, UUID> {
    
    List<DoctorSpecialization> findByDoctorId(UUID doctorId);
    
    void deleteByDoctorId(UUID doctorId);
    
    boolean existsByDoctorIdAndSpecializationId(UUID doctorId, UUID specializationId);
}

