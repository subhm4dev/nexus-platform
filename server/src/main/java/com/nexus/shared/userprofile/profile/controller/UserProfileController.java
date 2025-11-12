package com.nexus.shared.userprofile.controller;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.shared.userprofile.model.request.ProfileRequest;
import com.nexus.shared.userprofile.model.response.ProfileResponse;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.shared.userprofile.service.UserProfileService;
import com.nexus.libs.response.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User Profile Controller
 * 
 * <p>This controller manages user profile information including personal details
 * like full name, phone number, and avatar. Profiles enhance user experience
 * by personalizing interactions across the platform.
 * 
 * <p>Why we need these APIs:
 * <ul>
 *   <li><b>Profile Creation/Update:</b> Allows users to complete their profile after
 *       registration, storing additional information not captured during sign-up.
 *       Essential for personalized UX and order fulfillment.</li>
 *   <li><b>Profile Retrieval:</b> Enables other services (e.g., checkout, orders)
 *       to fetch user information for displaying names, avatars, and contact details
 *       without duplicating data.</li>
 *   <li><b>Event-Driven Updates:</b> Profile changes are published to Kafka,
 *       enabling other services to react to profile updates (e.g., update order
 *       display, notification preferences).</li>
 * </ul>
 * 
 * <p>This service integrates with the Identity service (via user_id) but maintains
 * separate profile data, following microservice separation of concerns.
 * 
 * <p><b>Security:</b> JWT tokens are validated by JwtAuthenticationFilter.
 * User context comes from validated JWT claims (source of truth).
 * Gateway headers (X-User-Id, X-Roles) are hints only, not trusted for security.
 */
@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "User Profile", description = "User profile management endpoints")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Create or update user profile
     * 
     * <p>This endpoint creates a new profile or updates an existing one for the
     * authenticated user. The user ID is extracted from validated JWT claims
     * (via JwtAuthenticationToken).
     * 
     * <p>Profile updates trigger a Kafka event (ProfileUpdated) that other services
     * can consume to sync profile changes.
     * 
     * <p>This endpoint is protected and requires authentication. Users can only
     * update their own profile.
     */
    @PostMapping
    @Operation(
        summary = "Create or update user profile",
        description = "Creates a new user profile or updates existing profile. Publishes ProfileUpdated event to Kafka."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ProfileResponse> createOrUpdateProfile(
            @Valid @RequestBody ProfileRequest profileRequest,
            Authentication authentication) {
        
        // Extract userId and domainId from validated JWT (source of truth)
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        log.info("Creating or updating profile for user: {}, domain: {}", userId, domainId);
        
        ProfileResponse response = userProfileService.createOrUpdateProfile(userId, domainId, profileRequest);
        return ApiResponse.success(response, "Profile saved successfully");
    }

    /**
     * Get user profile by user ID
     * 
     * <p>This endpoint retrieves profile information for a specified user. It's used
     * by other services and the frontend to display user information.
     * 
     * <p>Access control (via @PreAuthorize):
     * <ul>
     *   <li>Users can view their own profile</li>
     *   <li>Admins/Sellers can view any profile (for order management)</li>
     * </ul>
     * 
     * <p>This endpoint is protected and requires authentication.
     */
    @GetMapping("/{userId}")
    @Operation(
        summary = "Get user profile by ID",
        description = "Retrieves profile information for the specified user ID. Users can view own profile, admins/sellers can view any profile."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER') or hasRole('STAFF') or #userId.toString() == authentication.principal.toString()")
    public ApiResponse<ProfileResponse> getProfile(
            @PathVariable UUID userId,
            Authentication authentication) {
        UUID domainId = getDomainIdFromAuthentication(authentication);
        log.info("Retrieving profile for user: {}, domain: {}", userId, domainId);
        
        ProfileResponse response = userProfileService.getProfileByUserId(userId, domainId);
        return ApiResponse.success(response, "Profile retrieved successfully");
    }

    /**
     * Get current user's profile
     * 
     * <p>Convenience endpoint that returns the profile of the authenticated user,
     * eliminating the need to extract userId from token on the client side.
     * 
     * <p>This endpoint is protected and requires authentication.
     */
    @GetMapping("/me")
    @Operation(
        summary = "Get current user's profile",
        description = "Retrieves the profile of the currently authenticated user"
    )
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<ProfileResponse> getMyProfile(Authentication authentication) {
        // Extract userId and domainId from validated JWT (source of truth)
        UUID userId = getUserIdFromAuthentication(authentication);
        UUID domainId = getDomainIdFromAuthentication(authentication);
        
        log.info("Getting profile for current user: {}, domain: {}", userId, domainId);
        
        ProfileResponse response = userProfileService.getProfileByUserId(userId, domainId);
        return ApiResponse.success(response, "Profile retrieved successfully");
    }

    /**
     * Extract user ID from Spring Security Authentication
     * 
     * <p>The Authentication object is populated by JwtAuthenticationFilter
     * from validated JWT claims (source of truth).
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "User ID is required. Please ensure you are authenticated."
            );
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        String userIdStr = jwtAuth.getUserId();
        
        try {
            return UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format in JWT: {}", userIdStr);
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Invalid user ID format"
            );
        }
    }

    /**
     * Extract domain ID (UUID) from Spring Security Authentication
     * JWT stores domainId as string (UUID.toString()), we parse it to UUID
     */
    private UUID getDomainIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication instanceof JwtAuthenticationToken)) {
            log.error("Domain ID not found in authentication - cannot proceed without domain");
            throw new com.nexus.libs.error.exception.BusinessException(
                com.nexus.libs.error.model.ErrorCode.UNAUTHORIZED, 
                "Domain ID not found in authentication");
        }

        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        String domainIdStr = jwtAuth.getDomainId();
        
        if (domainIdStr == null || domainIdStr.isBlank()) {
            log.error("Domain ID is blank in JWT - cannot proceed without domain");
            throw new com.nexus.libs.error.exception.BusinessException(
                com.nexus.libs.error.model.ErrorCode.UNAUTHORIZED, 
                "Domain ID is blank in JWT");
        }
        
        try {
            return UUID.fromString(domainIdStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid domain ID format in JWT: {}", domainIdStr, e);
            throw new com.nexus.libs.error.exception.BusinessException(
                com.nexus.libs.error.model.ErrorCode.UNAUTHORIZED, 
                "Invalid domain ID format: " + domainIdStr);
        }
    }
}
