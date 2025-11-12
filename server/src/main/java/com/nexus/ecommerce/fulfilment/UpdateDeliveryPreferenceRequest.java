package com.nexus.ecommerce.fulfilment.model.request;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request to update delivery preferences
 */
public record UpdateDeliveryPreferenceRequest(
    LocalDate scheduledDeliveryDate,
    LocalTime scheduledDeliveryTimeStart,
    LocalTime scheduledDeliveryTimeEnd,
    String deliveryTimeWindow, // "MORNING", "AFTERNOON", "9AM-12PM", etc.
    
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
    String apartmentNumber
) {}

