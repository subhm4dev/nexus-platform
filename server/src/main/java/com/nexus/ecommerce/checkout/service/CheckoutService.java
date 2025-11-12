package com.nexus.ecommerce.checkout.service;

import com.nexus.ecommerce.checkout.model.request.AddressValidationRequest;
import com.nexus.ecommerce.checkout.model.request.CheckoutRequest;
import com.nexus.ecommerce.checkout.model.request.ShippingCalculationRequest;
import com.nexus.ecommerce.checkout.model.response.AddressValidationResponse;
import com.nexus.ecommerce.checkout.model.response.CheckoutCompleteResponse;
import com.nexus.ecommerce.checkout.model.response.CheckoutSummaryResponse;
import com.nexus.ecommerce.checkout.model.response.ShippingCalculationResponse;

import java.util.UUID;

/**
 * Service interface for checkout operations
 */
public interface CheckoutService {
    
    /**
     * Initiate checkout (validate and prepare order summary)
     */
    CheckoutSummaryResponse initiateCheckout(UUID userId, UUID tenantId, CheckoutRequest request);
    
    /**
     * Complete checkout and create order (Saga pattern)
     */
    CheckoutCompleteResponse completeCheckout(UUID userId, UUID tenantId, CheckoutRequest request, String jwtToken);
    
    /**
     * Cancel checkout and release resources
     */
    void cancelCheckout(UUID userId, UUID tenantId, UUID reservationId);
    
    /**
     * Validate shipping address
     */
    AddressValidationResponse validateAddress(UUID userId, UUID tenantId, AddressValidationRequest request);
    
    /**
     * Calculate shipping cost
     */
    ShippingCalculationResponse calculateShipping(UUID userId, UUID tenantId, ShippingCalculationRequest request);
}

