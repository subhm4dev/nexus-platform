package com.nexus.ecommerce.fulfilment.model.response;

import java.util.List;
import java.util.UUID;

/**
 * Response when sharing delivery link
 */
public record ShareLinkResponse(
    UUID deliveryId,
    List<SharedLinkInfo> sharedLinks,
    Integer totalShared,
    String message
) {
    public record SharedLinkInfo(
        UUID recipientId,
        String recipientName,
        String recipientPhone,
        String shareToken,
        String shareLink,
        String shareMethod, // SMS, EMAIL, WHATSAPP, LINK
        String status // PENDING, ACTIVE
    ) {}
}

