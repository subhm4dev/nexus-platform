package com.nexus.shared.iam.repository;

import com.nexus.shared.iam.entity.UserDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserDomain entity
 */
@Repository
public interface UserDomainRepository extends JpaRepository<UserDomain, UUID> {
    
    /**
     * Find all domains for a user
     * 
     * @param userId User ID
     * @return List of UserDomain entries
     */
    List<UserDomain> findByUserId(UUID userId);
    
    /**
     * Find all users in a specific domain
     * 
     * @param domainId Domain ID (UUID)
     * @return List of UserDomain entries
     */
    List<UserDomain> findByDomainId(UUID domainId);
    
    /**
     * Check if user belongs to a specific domain
     * 
     * @param userId User ID
     * @param domainId Domain ID (UUID)
     * @return true if user belongs to the domain
     */
    boolean existsByUserIdAndDomainId(UUID userId, UUID domainId);
    
    /**
     * Find specific user-domain-tenant combination
     * 
     * @param userId User ID
     * @param domainId Domain ID (UUID)
     * @param tenantId Tenant ID
     * @return Optional UserDomain
     */
    Optional<UserDomain> findByUserIdAndDomainIdAndTenantId(UUID userId, UUID domainId, UUID tenantId);
    
    /**
     * Find all user-domain entries for a specific tenant
     * 
     * @param tenantId Tenant ID
     * @return List of UserDomain entries
     */
    List<UserDomain> findByTenantId(UUID tenantId);
}

