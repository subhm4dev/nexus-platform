package com.nexus.ecommerce.fulfilment.model.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request to record a delivery attempt
 */
public record RecordDeliveryAttemptRequest(
    @NotNull(message = "Attempt status is required")
    String attemptStatus, // SUCCESSFUL, FAILED, CANCELLED
    
    String failureReason,
    String failureCode, // CUSTOMER_NOT_AVAILABLE, WRONG_ADDRESS, etc.
    
    BigDecimal attemptLatitude,
    BigDecimal attemptLongitude,
    String attemptLocationDescription,
    
    List<String> photoUrls,
    String notes,
    
    LocalDateTime nextAttemptAt // If scheduling next attempt
) {}

