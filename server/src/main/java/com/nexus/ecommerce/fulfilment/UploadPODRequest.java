package com.nexus.ecommerce.fulfilment.model.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request to upload proof of delivery
 */
public record UploadPODRequest(
    @NotNull(message = "POD type is required")
    String podType, // PHOTO, SIGNATURE, OTP, VIDEO, COMBINATION
    
    // Photo POD
    List<String> photoUrls,
    
    // Signature POD
    String signatureUrl,
    String signatureData, // Base64 encoded
    String recipientName,
    
    // OTP POD
    String otpCode,
    String otpPhoneNumber,
    
    // Video POD
    String videoUrl,
    
    // Location
    BigDecimal podLatitude,
    BigDecimal podLongitude,
    String podLocationDescription,
    
    // Recipient details
    String receivedByName,
    String receivedByPhone,
    String receivedByRelation, // SELF, FAMILY_MEMBER, NEIGHBOR, etc.
    Boolean isAlternateRecipient,
    UUID alternateRecipientId
) {}

