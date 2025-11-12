package com.nexus.ecommerce.fulfilment.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response for delivery attempt
 */
public record DeliveryAttemptResponse(
    UUID id,
    UUID deliveryId,
    UUID fulfillmentId,
    UUID tenantId,
    Integer attemptNumber,
    LocalDateTime attemptedAt,
    UUID attemptedByUserId,
    String attemptStatus,
    String failureReason,
    String failureCode,
    BigDecimal attemptLatitude,
    BigDecimal attemptLongitude,
    String attemptLocationDescription,
    List<String> photoUrls,
    String notes,
    LocalDateTime nextAttemptAt,
    Boolean nextAttemptScheduled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

