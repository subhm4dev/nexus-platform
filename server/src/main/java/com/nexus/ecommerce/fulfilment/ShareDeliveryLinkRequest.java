package com.nexus.ecommerce.fulfilment.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

/**
 * Request to share delivery link with alternate recipient(s)
 * Customer can share with multiple alternate phone numbers/users (no limit)
 */
public record ShareDeliveryLinkRequest(
    @NotEmpty(message = "At least one alternate recipient is required")
    List<AlternateRecipientInfo> alternateRecipients,
    
    @NotBlank(message = "Share method is required")
    String shareMethod, // SMS, EMAIL, WHATSAPP, LINK
    
    Integer expiryHours // Link expiry in hours (default 24)
) {
    public record AlternateRecipientInfo(
        String name, // Name of alternate recipient
        
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        String phoneNumber, // Phone number (required)
        
        String email, // Email (optional)
        
        UUID userId // User ID if they have account (optional)
    ) {}
}

