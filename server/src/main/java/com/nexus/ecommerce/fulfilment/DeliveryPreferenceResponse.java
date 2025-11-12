package com.nexus.ecommerce.fulfilment.model.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Response for delivery preferences
 */
public record DeliveryPreferenceResponse(
    UUID id,
    UUID fulfillmentId,
    UUID deliveryId,
    UUID tenantId,
    UUID customerUserId,
    LocalDate scheduledDeliveryDate,
    LocalTime scheduledDeliveryTimeStart,
    LocalTime scheduledDeliveryTimeEnd,
    String deliveryTimeWindow,
    String deliveryInstructions,
    String specialHandlingNotes,
    Boolean leaveAtDoor,
    Boolean handToCustomer,
    Boolean requireSignature,
    String preferredContactMethod,
    String preferredContactTime,
    Boolean doNotDisturb,
    String preferredDeliveryLocation,
    String gateCode,
    String buildingName,
    String floorNumber,
    String apartmentNumber,
    Boolean isActive,
    LocalDateTime appliedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

