package com.nexus.shared.address.repository;

import com.nexus.shared.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Address entity
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    
    /**
     * Find all active (non-deleted) addresses for a user within a domain and tenant
     * 
     * @param userId User ID
     * @param domainId Domain ID
     * @param tenantId Tenant ID
     * @return List of active addresses
     */
    List<Address> findByUserIdAndDomainIdAndTenantIdAndDeletedFalse(UUID userId, UUID domainId, UUID tenantId);
    
    /**
     * Find all addresses for a user within a domain and tenant (including deleted)
     * Used by admins/staff for audit purposes
     * 
     * @param userId User ID
     * @param domainId Domain ID (UUID)
     * @param tenantId Tenant ID
     * @return List of all addresses (active and deleted)
     */
    List<Address> findByUserIdAndDomainIdAndTenantId(UUID userId, UUID domainId, UUID tenantId);
    
    /**
     * Find active address by ID
     * 
     * @param id Address ID
     * @return Optional Address (only if not deleted)
     */
    Optional<Address> findByIdAndDeletedFalse(UUID id);
    
    /**
     * Find address by ID (including deleted)
     * Used by admins/staff for recovery/audit
     * 
     * @param id Address ID
     * @return Optional Address (may be deleted)
     */
    @Override
    @NonNull
    Optional<Address> findById(@NonNull UUID id);
    
    /**
     * Check if a duplicate active address exists for a user
     * Duplicate is defined as same user_id, domain_id, tenant_id, line1, city, postcode, and country
     * 
     * @param userId User ID
     * @param domainId Domain ID
     * @param tenantId Tenant ID
     * @param line1 First line of address
     * @param city City name
     * @param postcode Postal code
     * @param country Country code
     * @return true if duplicate exists (active address only)
     */
    boolean existsByUserIdAndDomainIdAndTenantIdAndLine1AndCityAndPostcodeAndCountryAndDeletedFalse(
        UUID userId,
        UUID domainId,
        UUID tenantId,
        String line1,
        String city,
        String postcode,
        String country
    );
}

