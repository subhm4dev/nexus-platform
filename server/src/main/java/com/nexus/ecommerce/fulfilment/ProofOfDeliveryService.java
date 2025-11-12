package com.nexus.ecommerce.fulfilment.service;

import com.nexus.ecommerce.fulfilment.model.request.UploadPODRequest;
import com.nexus.ecommerce.fulfilment.model.response.ProofOfDeliveryResponse;

import java.util.UUID;

/**
 * Service for proof of delivery management
 */
public interface ProofOfDeliveryService {
    
    /**
     * Upload proof of delivery (photo, signature, OTP)
     */
    ProofOfDeliveryResponse uploadPOD(
        UUID deliveryId,
        UUID driverId,
        UUID tenantId,
        UploadPODRequest request
    );
    
    /**
     * Get POD for a delivery
     */
    ProofOfDeliveryResponse getPODByDeliveryId(UUID deliveryId, UUID tenantId);
    
    /**
     * Verify OTP for POD
     */
    ProofOfDeliveryResponse verifyOTP(
        UUID deliveryId,
        String otpCode,
        UUID tenantId
    );
}

