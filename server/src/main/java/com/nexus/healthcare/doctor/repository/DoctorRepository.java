package com.nexus.healthcare.doctor.repository;

import com.nexus.healthcare.doctor.entity.Doctor;
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
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    
    Optional<Doctor> findByIdAndTenantIdAndDomainIdAndDeletedFalse(UUID id, UUID tenantId, UUID domainId);
    
    List<Doctor> findByTenantIdAndDomainIdAndDeletedFalse(UUID tenantId, UUID domainId);
    
    Page<Doctor> findByTenantIdAndDomainIdAndDeletedFalse(UUID tenantId, UUID domainId, Pageable pageable);
    
    List<Doctor> findByTenantIdInAndDomainIdAndDeletedFalse(List<UUID> tenantIds, UUID domainId);
    
    Optional<Doctor> findByUserIdAndTenantIdAndDomainIdAndDeletedFalse(UUID userId, UUID tenantId, UUID domainId);
    
    @Query(value = "SELECT d.* FROM doctors d WHERE " +
           "d.tenant_id IN :tenantIds AND " +
           "d.domain_id = :domainId AND " +
           "d.deleted = false AND " +
           "(:verificationStatus IS NULL OR d.verification_status = :verificationStatus) AND " +
           "(:query IS NULL OR d.registration_number IS NULL OR LOWER(CAST(d.registration_number AS VARCHAR)) LIKE LOWER(CONCAT('%', :query, '%')))",
           nativeQuery = true,
           countQuery = "SELECT COUNT(*) FROM doctors d WHERE " +
           "d.tenant_id IN :tenantIds AND " +
           "d.domain_id = :domainId AND " +
           "d.deleted = false AND " +
           "(:verificationStatus IS NULL OR d.verification_status = :verificationStatus) AND " +
           "(:query IS NULL OR d.registration_number IS NULL OR LOWER(CAST(d.registration_number AS VARCHAR)) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Doctor> searchDoctors(
        @Param("tenantIds") List<UUID> tenantIds,
        @Param("domainId") UUID domainId,
        @Param("verificationStatus") String verificationStatus,
        @Param("query") String query,
        Pageable pageable
    );
}

