package com.nexus.shared.userprofile.repository;

import com.nexus.shared.userprofile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserProfile entity
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    
    /**
     * Find profile by user ID and domain ID
     * 
     * @param userId User ID from Identity service
     * @param domainId Domain ID (UUID)
     * @return Optional UserProfile
     */
    Optional<UserProfile> findByUserIdAndDomainId(UUID userId, UUID domainId);
    
    /**
     * Check if profile exists for user ID and domain ID
     * 
     * @param userId User ID from Identity service
     * @param domainId Domain ID (UUID)
     * @return true if profile exists
     */
    boolean existsByUserIdAndDomainId(UUID userId, UUID domainId);
}

