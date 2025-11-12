package com.nexus.ecommerce.fulfilment.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response for alternate recipient
 */
public record AlternateRecipientResponse(
    UUID id,
    UUID deliveryId,
    UUID tenantId,
    UUID customerUserId,
    
    // Alternate recipient details
    UUID alternateUserId,
    String alternatePhoneNumber,
    String alternateName,
    String alternateEmail,
    
    // Sharing details
    String shareToken,
    String shareLink,
    LocalDateTime sharedAt,
    UUID sharedByUserId,
    String sharedVia,
    
    // Status
    String status, // PENDING, ACTIVE, CONFIRMED, EXPIRED, REVOKED
    LocalDateTime confirmedAt,
    UUID confirmedByUserId,
    
    // Confirmation details
    BigDecimal confirmedLatitude,
    BigDecimal confirmedLongitude,
    BigDecimal confirmedLocationAccuracy,
    Boolean proximityVerified,
    BigDecimal distanceToAgent,
    
    // Expiry
    LocalDateTime expiresAt,
    LocalDateTime revokedAt,
    UUID revokedByUserId,
    String revokeReason,
    
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

