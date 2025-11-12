package com.nexus.healthcare.patient.repository;

import com.nexus.healthcare.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
    
    Optional<Patient> findByIdAndTenantIdAndDomainIdAndDeletedFalse(UUID id, UUID tenantId, UUID domainId);
    
    List<Patient> findByTenantIdAndDomainIdAndDeletedFalse(UUID tenantId, UUID domainId);
    
    Page<Patient> findByTenantIdAndDomainIdAndDeletedFalse(UUID tenantId, UUID domainId, Pageable pageable);
    
    Optional<Patient> findByUserIdAndTenantIdAndDomainIdAndDeletedFalse(UUID userId, UUID tenantId, UUID domainId);
    
    @Query(value = "SELECT p.* FROM patients p WHERE " +
           "p.tenant_id IN :tenantIds AND " +
           "p.domain_id = :domainId AND " +
           "p.deleted = false AND " +
           "(:query IS NULL OR LOWER(CAST(p.id AS VARCHAR)) LIKE LOWER(CONCAT('%', :query, '%')))",
           nativeQuery = true,
           countQuery = "SELECT COUNT(*) FROM patients p WHERE " +
           "p.tenant_id IN :tenantIds AND " +
           "p.domain_id = :domainId AND " +
           "p.deleted = false AND " +
           "(:query IS NULL OR LOWER(CAST(p.id AS VARCHAR)) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Patient> searchPatients(
        @Param("tenantIds") List<UUID> tenantIds,
        @Param("domainId") UUID domainId,
        @Param("query") String query,
        Pageable pageable
    );
}

