package com.nexus.ecommerce.fulfilment.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response for proof of delivery
 */
public record ProofOfDeliveryResponse(
    UUID id,
    UUID deliveryId,
    UUID fulfillmentId,
    UUID deliveryConfirmationId,
    UUID tenantId,
    String podType,
    List<String> photoUrls,
    LocalDateTime photoTakenAt,
    UUID photoTakenByUserId,
    String signatureUrl,
    String signatureData,
    LocalDateTime signatureTakenAt,
    UUID signatureTakenByUserId,
    String recipientName,
    Boolean otpVerified,
    String otpCode,
    LocalDateTime otpVerifiedAt,
    String otpPhoneNumber,
    String videoUrl,
    LocalDateTime videoTakenAt,
    BigDecimal podLatitude,
    BigDecimal podLongitude,
    String podLocationDescription,
    String receivedByName,
    String receivedByPhone,
    String receivedByRelation,
    Boolean isAlternateRecipient,
    UUID alternateRecipientId,
    String podStatus,
    LocalDateTime verifiedAt,
    UUID verifiedByUserId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

