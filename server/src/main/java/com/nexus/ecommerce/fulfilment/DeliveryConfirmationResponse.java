package com.nexus.ecommerce.fulfilment.model.response;

import com.nexus.ecommerce.fulfilment.entity.DeliveryConfirmation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response for delivery confirmation status
 */
public record DeliveryConfirmationResponse(
    UUID id,
    UUID deliveryId,
    UUID tenantId,
    
    // Agent confirmation
    Boolean agentConfirmed,
    LocalDateTime agentConfirmedAt,
    BigDecimal agentLatitude,
    BigDecimal agentLongitude,
    BigDecimal agentLocationAccuracy,
    
    // Customer confirmation
    Boolean customerConfirmed,
    LocalDateTime customerConfirmedAt,
    BigDecimal customerLatitude,
    BigDecimal customerLongitude,
    BigDecimal customerLocationAccuracy,
    
    // Proximity
    Boolean proximityVerified,
    BigDecimal distanceBetweenParties,
    BigDecimal distanceToDeliveryAddress,
    LocalDateTime proximityVerifiedAt,
    
    // Not available
    Boolean agentMarkedUnavailable,
    LocalDateTime agentUnavailableAt,
    String agentUnavailableReason,
    Boolean customerMarkedUnavailable,
    LocalDateTime customerUnavailableAt,
    String customerUnavailableReason,
    
    // Reschedule
    Integer rescheduleCount,
    LocalDateTime lastRescheduleAt,
    LocalDateTime nextAttemptAt,
    Boolean autoReturnInitiated,
    
    // Status
    DeliveryConfirmation.ConfirmationStatus confirmationStatus,
    
    // Computed fields
    Boolean canConfirm, // Can current user confirm
    Integer timeRemaining, // Seconds remaining for confirmation
    Boolean isInProximity, // Is current user in proximity
    
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static DeliveryConfirmationResponse from(
        DeliveryConfirmation confirmation,
        Boolean canConfirm,
        Integer timeRemaining,
        Boolean isInProximity
    ) {
        return new DeliveryConfirmationResponse(
            confirmation.getId(),
            confirmation.getDeliveryId(),
            confirmation.getTenantId(),
            confirmation.getAgentConfirmed(),
            confirmation.getAgentConfirmedAt(),
            confirmation.getAgentLatitude(),
            confirmation.getAgentLongitude(),
            confirmation.getAgentLocationAccuracy(),
            confirmation.getCustomerConfirmed(),
            confirmation.getCustomerConfirmedAt(),
            confirmation.getCustomerLatitude(),
            confirmation.getCustomerLongitude(),
            confirmation.getCustomerLocationAccuracy(),
            confirmation.getProximityVerified(),
            confirmation.getDistanceBetweenParties(),
            confirmation.getDistanceToDeliveryAddress(),
            confirmation.getProximityVerifiedAt(),
            confirmation.getAgentMarkedUnavailable(),
            confirmation.getAgentUnavailableAt(),
            confirmation.getAgentUnavailableReason(),
            confirmation.getCustomerMarkedUnavailable(),
            confirmation.getCustomerUnavailableAt(),
            confirmation.getCustomerUnavailableReason(),
            confirmation.getRescheduleCount(),
            confirmation.getLastRescheduleAt(),
            confirmation.getNextAttemptAt(),
            confirmation.getAutoReturnInitiated(),
            confirmation.getConfirmationStatus(),
            canConfirm,
            timeRemaining,
            isInProximity,
            confirmation.getCreatedAt(),
            confirmation.getUpdatedAt()
        );
    }
}

