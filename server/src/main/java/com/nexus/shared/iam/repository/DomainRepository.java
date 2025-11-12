package com.nexus.shared.iam.repository;

import com.nexus.shared.iam.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Domain entity
 */
@Repository
public interface DomainRepository extends JpaRepository<Domain, UUID> {
    
    /**
     * Find domain by code (e.g., "ecommerce", "hospital")
     * 
     * @param code Domain code
     * @return Optional Domain
     */
    Optional<Domain> findByCode(String code);
    
    /**
     * Check if domain exists by code
     * 
     * @param code Domain code
     * @return true if domain exists
     */
    boolean existsByCode(String code);
    
    /**
     * Find enabled domains only
     * 
     * @param enabled Whether domain is enabled
     * @return List of domains
     */
    java.util.List<Domain> findByEnabled(Boolean enabled);
}

