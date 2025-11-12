package com.nexus.shared.iam.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.shared.iam.constants.TenantType;
import com.nexus.shared.iam.entity.RoleGrant;
import com.nexus.shared.iam.entity.Tenant;
import com.nexus.shared.iam.entity.UserAccount;
import com.nexus.shared.iam.entity.UserDomain;
import com.nexus.shared.iam.model.request.LoginRequest;
import com.nexus.shared.iam.model.request.RefreshRequest;
import com.nexus.shared.iam.model.request.RegisterRequest;
import com.nexus.shared.iam.model.response.LoginResponse;
import com.nexus.shared.iam.model.response.RefreshResponse;
import com.nexus.shared.iam.model.response.RegisterResponse;
import com.nexus.shared.iam.entity.RefreshToken;
import com.nexus.shared.iam.repository.DomainRepository;
import com.nexus.shared.iam.repository.RefreshTokenRepository;
import com.nexus.shared.iam.repository.RoleGrantRepository;
import com.nexus.shared.iam.repository.TenantRepository;
import com.nexus.shared.iam.repository.UserAccountRepository;
import com.nexus.shared.iam.repository.UserDomainRepository;
import com.nexus.shared.iam.service.AuthService;
import com.nexus.shared.iam.service.JwtService;
import com.nexus.shared.iam.service.PasswordService;
import com.nexus.shared.iam.service.SessionService;
import com.nexus.shared.iam.model.request.LogoutRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Authentication service implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final TenantRepository tenantRepository;
    private final DomainRepository domainRepository;
    private final RoleGrantRepository roleGrantRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDomainRepository userDomainRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final SessionService sessionService;

    @Value("${jwt.refresh-token.expiry-days:30}")
    private int refreshTokenExpiryDays;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // 1. Resolve domain from domainCode (defaults to "ecommerce")
        String domainCode = request.domainCode();
        com.nexus.shared.iam.entity.Domain domain = domainRepository.findByCode(domainCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.SKU_REQUIRED,
                "Domain not found: " + domainCode + ". Please ensure domain exists in database."));
        UUID domainId = domain.getId();
        
        // 2. Determine tenant based on role and domain
        Tenant tenant;
        UUID tenantId = request.tenantId();

        // Tenant ID is REQUIRED for all roles
        if (tenantId == null) {
            throw new BusinessException(ErrorCode.SKU_REQUIRED,
                "Tenant ID is required. Please specify which app/tenant you want to register for (e.g., Namaste Fab or Kalakosh for customers, APP tenant for sellers).");
        }

        // Validate tenant exists and belongs to the specified domain
        final UUID requestedTenantId = tenantId;  // Final for lambda
        tenant = tenantRepository.findById(requestedTenantId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SKU_REQUIRED, 
                "Invalid tenant ID: " + requestedTenantId));
            
            // Validate tenant belongs to the specified domain
            if (!tenant.getDomainId().equals(domainId)) {
                throw new BusinessException(ErrorCode.SKU_REQUIRED,
                    "Tenant does not belong to domain: " + domainCode);
            }

        // 3. Handle role-specific tenant assignment
        if (request.role() == com.nexus.shared.iam.constants.Role.SELLER) {
            // Seller registration: Create SELLER tenant with parent = provided APP tenant
            // Validate provided tenant is APP type
            if (tenant.getType() != TenantType.APP) {
                throw new BusinessException(ErrorCode.SKU_REQUIRED,
                    "Sellers must register with an APP tenant (e.g., Namaste Fab or Kalakosh). Provided tenant is not an APP tenant.");
            }
            
            // Create new SELLER tenant with parent = APP tenant
            Tenant sellerTenant = Tenant.builder()
                    .name("Seller: " + (request.email() != null ? request.email() : request.phone()))
                    .status(com.nexus.shared.iam.constants.TenantStatus.ACTIVE)
                    .domainId(domainId)
                .type(TenantType.SELLER)
                .parentTenantId(tenantId)  // Parent = APP tenant
                    .build();
            sellerTenant = tenantRepository.save(sellerTenant);
            log.info("Created SELLER tenant {} with parent APP tenant {} for seller registration", 
                sellerTenant.getId(), tenantId);
            
            // Use the newly created SELLER tenant for user assignment
            tenant = sellerTenant;
            tenantId = sellerTenant.getId();
        } else if (request.role() == com.nexus.shared.iam.constants.Role.STAFF) {
            // Staff registration: Assign directly to provided SELLER or BRANCH tenant
            // Validate provided tenant is SELLER or BRANCH type
            if (tenant.getType() != TenantType.SELLER && tenant.getType() != TenantType.BRANCH) {
                throw new BusinessException(ErrorCode.SKU_REQUIRED,
                    "Staff must register with a SELLER or BRANCH tenant. Provided tenant is not a SELLER or BRANCH tenant.");
            }
            
            // Staff is assigned directly to the provided tenant (no new tenant creation)
            log.info("Assigning staff to existing tenant {} (type: {})", tenantId, tenant.getType());
        } else if (request.role() == com.nexus.shared.iam.constants.Role.CUSTOMER) {
            // Customer registration: Must register with APP tenant
            // Validate provided tenant is APP type
            if (tenant.getType() != TenantType.APP) {
                throw new BusinessException(ErrorCode.SKU_REQUIRED,
                    "Customers must register with an APP tenant (e.g., Namaste Fab or Kalakosh). Provided tenant is not an APP tenant.");
            }
            
            // Customer is assigned directly to the APP tenant
            log.info("Assigning customer to APP tenant {}", tenantId);
        }
        // Other roles (ADMIN, DRIVER) can be assigned to any tenant type as needed

        // 2. Check email uniqueness within tenant scope
        if (request.email() != null && !request.email().isBlank()) {
            UUID finalTenantId = tenantId;
            userAccountRepository.findByEmail(request.email())
                .ifPresent(existing -> {
                    // Check if same tenant (tenant-scoped uniqueness)
                    if (existing.getTenant().getId().equals(finalTenantId)) {
                        throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Email already registered");
                    }
                });
        }

        // 3. Check phone uniqueness within tenant scope
        if (request.phone() != null && !request.phone().isBlank()) {
            UUID finalTenantId1 = tenantId;
            userAccountRepository.findByPhone(request.phone())
                .ifPresent(existing -> {
                    // Check if same tenant (tenant-scoped uniqueness)
                    if (existing.getTenant().getId().equals(finalTenantId1)) {
                        throw new BusinessException(ErrorCode.PHONE_TAKEN, "Phone already registered");
                    }
                });
        }

        // 4. Generate salt and hash password
        String salt = passwordService.generateSalt();
        String passwordHash = passwordService.hashPassword(request.password(), salt);

        // 5. Create UserAccount entity
        UserAccount userAccount = UserAccount.builder()
            .email(request.email())
            .phone(request.phone())
            .passwordHash(passwordHash)
            .salt(salt)
            .tenant(tenant)
            .enabled(true)
            .emailVerified(false)
            .phoneVerified(false)
            .build();

        // 6. Persist UserAccount
        userAccount = userAccountRepository.save(userAccount);

        // 6a. Create UserDomain entry to track user's domain membership
        // This enables users to be part of multiple domains
        UUID tenantDomainId = tenant.getDomainId();
        UserDomain userDomain = UserDomain.builder()
            .userId(userAccount.getId())
            .domainId(tenantDomainId)
            .tenantId(tenant.getId())
            .build();
        
        // Only create if it doesn't already exist (idempotent)
        if (userDomainRepository.findByUserIdAndDomainIdAndTenantId(
                userAccount.getId(), tenantDomainId, tenant.getId()).isEmpty()) {
            userDomainRepository.save(userDomain);
            log.debug("Created UserDomain entry: userId={}, domainId={}, tenantId={}", 
                userAccount.getId(), tenantDomainId, tenant.getId());
        }

        // 7. Create and persist RoleGrant
        RoleGrant roleGrant = RoleGrant.builder()
            .user(userAccount)
            .role(request.role())
            .build();
        roleGrantRepository.save(roleGrant);

        // 8. Generate tokens for auto-login
        List<String> roles = List.of(roleGrant.getRole().name());
        String accessToken = jwtService.generateAccessToken(userAccount, roles);

        // 9. Generate and store refresh token
        String refreshTokenString = jwtService.generateRefreshTokenString();
        String refreshTokenHash = passwordService.hashTokenDeterministically(refreshTokenString);
        LocalDateTime refreshTokenExpiresAt = LocalDateTime.now().plusDays(refreshTokenExpiryDays);

        RefreshToken refreshToken = RefreshToken.builder()
            .user(userAccount)
            .tokenHash(refreshTokenHash)
            .expiresAt(refreshTokenExpiresAt)
            .revoked(false)
            .build();
        refreshTokenRepository.save(refreshToken);

        // 10. Return response with access token
        return new RegisterResponse(
            accessToken,
            refreshTokenString, // Return plain refresh token (client stores this), not the hash
            userAccount.getId().toString(),
            roles,
            userAccount.getTenant().getId().toString()
        );
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. Resolve domain from domainCode (defaults to "ecommerce")
        String domainCode = request.domainCode();
        com.nexus.shared.iam.entity.Domain domain = domainRepository.findByCode(domainCode)
            .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS,
                "Domain not found: " + domainCode));
        UUID domainId = domain.getId();
        
        // 2. Find user by email or phone
        UserAccount userAccount = null;
        if (request.email() != null && !request.email().isBlank()) {
            userAccount = userAccountRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS, "Invalid email or password"));
        } else if (request.phone() != null && !request.phone().isBlank()) {
            userAccount = userAccountRepository.findByPhone(request.phone())
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS, "Invalid phone or password"));
        } else {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "Email or phone is required");
        }

        // 3. Check if user is enabled
        if (!userAccount.isEnabled()) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "User account is disabled");
        }

        // 4. Verify password
        boolean passwordMatches = passwordService.verifyPassword(
            request.password(),
            userAccount.getPasswordHash(),
            userAccount.getSalt()
        );

        if (!passwordMatches) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "Invalid email or password");
        }

        // 5. Validate user belongs to the specified domain and get their tenant in that domain
        List<UserDomain> userDomains = userDomainRepository.findByUserId(userAccount.getId());
        UserDomain userDomain = userDomains.stream()
            .filter(ud -> ud.getDomainId().equals(domainId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS,
                "User does not belong to domain: " + domainCode + ". Please register for this domain first."));
        
        // Get the tenant for this domain context
        Tenant tenant = tenantRepository.findById(userDomain.getTenantId())
            .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS,
                "Tenant not found for user in domain: " + domainCode));
        
        // Temporarily update userAccount's tenant for JWT generation (domain-specific context)
        // Note: We don't persist this - it's just for generating the correct JWT
        Tenant originalTenant = userAccount.getTenant();
        userAccount.setTenant(tenant);

        // 6. Load user roles from RoleGrant repository
        List<RoleGrant> roleGrants = roleGrantRepository.findAllByUser(userAccount);
        List<String> roles = roleGrants.stream()
            .map(roleGrant -> roleGrant.getRole().name())
            .toList();

        // 7. Generate access token (2 hours expiry) with domain-specific tenant
        String accessToken = jwtService.generateAccessToken(userAccount, roles);
        
        // Restore original tenant (don't persist the temporary change)
        userAccount.setTenant(originalTenant);

        // 6. Generate and store refresh token
        String refreshTokenString = jwtService.generateRefreshTokenString();
        String refreshTokenHash = passwordService.hashTokenDeterministically(refreshTokenString);
        LocalDateTime refreshTokenExpiresAt = LocalDateTime.now().plusDays(refreshTokenExpiryDays);

        RefreshToken refreshToken = RefreshToken.builder()
            .user(userAccount)
            .tokenHash(refreshTokenHash)
            .expiresAt(refreshTokenExpiresAt)
            .revoked(false)
            .build();
        refreshTokenRepository.save(refreshToken);

        // 7. Calculate access token expiry in seconds (get from JwtService config)
        // Note: JwtService uses accessTokenExpiryHours config value
        long expiresInSeconds = 2L * 3600L; // 2 hours (should match JwtService config)

        // 8. Return LoginResponse with tokens (using domain-specific tenant ID)
        return new LoginResponse(
            accessToken,
            refreshTokenString, // Return plain refresh token (client stores this)
            expiresInSeconds,
            userAccount.getId().toString(),
            roles,
            tenant.getId().toString() // Use domain-specific tenant ID
        );
    }

    @Override
    @Transactional
    public RefreshResponse refresh(RefreshRequest request, String accessToken) {
        // 1. Hash the refresh token to lookup in database
        String refreshTokenHash = passwordService.hashTokenDeterministically(request.refreshToken());

        // 2. Find refresh token in database
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(refreshTokenHash)
            .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS, "Invalid refresh token"));

        // 3. Check if token is revoked
        if (refreshToken.isRevoked()) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "Refresh token has been revoked");
        }

        // 4. Check if token is expired
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "Refresh token has expired");
        }

        // 5. Get user account and check if enabled
        UserAccount userAccount = refreshToken.getUser();
        if (!userAccount.isEnabled()) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "User account is disabled");
        }

        // 6. If access token is provided, validate it belongs to the same user
        // This adds an extra security layer: even if refresh token is valid,
        // the access token must belong to the same user
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                UUID accessTokenUserId = jwtService.extractUserId(accessToken);
                if (!accessTokenUserId.equals(userAccount.getId())) {
                    throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "Refresh token and access token belong to different users");
                }
            } catch (Exception e) {
                // Access token may be expired or invalid - that's okay for refresh
                // We'll proceed with refresh token validation only
                log.debug("Could not validate access token during refresh (may be expired): {}", e.getMessage());
            }
        }

        // 7. Load user roles
        List<RoleGrant> roleGrants = roleGrantRepository.findAllByUser(userAccount);
        List<String> roles = roleGrants.stream()
            .map(roleGrant -> roleGrant.getRole().name())
            .toList();

        // 8. Generate new access token
        String newAccessToken = jwtService.generateAccessToken(userAccount, roles);

        // 9. Calculate access token expiry in seconds (2 hours = 7200 seconds)
        long expiresInSeconds = 2L * 3600L; // 2 hours

        // 10. Return RefreshResponse with new access token
        return new RefreshResponse(newAccessToken, expiresInSeconds);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest logoutRequest, String accessToken) {
        // 1. Validate access token is provided (required for authentication)
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "Access token is required for logout");
        }

        // 2. Extract user ID from access token
        UUID authenticatedUserId;
        try {
            authenticatedUserId = jwtService.extractUserId(accessToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "Invalid or expired access token");
        }

        // 3. Hash the refresh token to lookup in database
        String refreshTokenHash = passwordService.hashTokenDeterministically(logoutRequest.refreshToken());

        // 4. Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(refreshTokenHash)
            .orElseThrow(() -> new BusinessException(ErrorCode.BAD_CREDENTIALS, "Invalid refresh token"));

        // 5. Validate refresh token belongs to the authenticated user (security check)
        UUID refreshTokenUserId = refreshToken.getUser().getId();
        if (!refreshTokenUserId.equals(authenticatedUserId)) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "Refresh token does not belong to authenticated user");
        }

        // 6. Check if refresh token is already revoked
        if (refreshToken.isRevoked()) {
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS, "Refresh token already revoked");
        }

        // 7. Revoke the refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // 8. Blacklist the access token
        String tokenId = jwtService.extractTokenId(accessToken);
        long expiresInSeconds = jwtService.getTokenExpirySeconds(accessToken);
        sessionService.blacklistToken(tokenId, expiresInSeconds);

        log.info("User logged out: userId={}", authenticatedUserId);
    }

    @Override
    @Transactional
    public void logoutAll(UUID userId, String accessToken) {
        // 1. Revoke all refresh tokens for this user
        List<RefreshToken> userRefreshTokens = refreshTokenRepository.findByUser_IdAndRevokedFalse(userId);
        userRefreshTokens.forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });

        // 2. Revoke all sessions in Redis (blacklist all access tokens)
        sessionService.revokeAllUserSessions(userId);

        // 3. Also blacklist the current access token if provided
        if (accessToken != null && !accessToken.isBlank()) {
            String tokenId = jwtService.extractTokenId(accessToken);
            long expiresInSeconds = jwtService.getTokenExpirySeconds(accessToken);
            sessionService.blacklistToken(tokenId, expiresInSeconds);
        }

        log.info("User logged out from all devices: userId={}, sessions={}", userId, userRefreshTokens.size());
    }
}
