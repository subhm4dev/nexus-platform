package com.nexus.healthcare.doctor.repository;

import com.nexus.healthcare.doctor.entity.QualificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QualificationTypeRepository extends JpaRepository<QualificationType, UUID> {
    
    Optional<QualificationType> findByCode(String code);
    
    List<QualificationType> findByActiveTrue();
    
    Optional<QualificationType> findByIdAndActiveTrue(UUID id);
}
