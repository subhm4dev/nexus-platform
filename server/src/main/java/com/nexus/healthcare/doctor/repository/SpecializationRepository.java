package com.nexus.healthcare.doctor.repository;

import com.nexus.healthcare.doctor.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {
    
    Optional<Specialization> findByCode(String code);
    
    List<Specialization> findByActiveTrue();
    
    Optional<Specialization> findByIdAndActiveTrue(UUID id);
}

