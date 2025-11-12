package com.nexus.shared.payment.service;

import com.nexus.shared.payment.model.request.ProcessPaymentRequest;
import com.nexus.shared.payment.model.request.RefundPaymentRequest;
import com.nexus.shared.payment.model.request.SavePaymentMethodRequest;
import com.nexus.shared.payment.model.response.PaymentMethodResponse;
import com.nexus.shared.payment.model.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Payment Service Interface
 */
public interface PaymentService {
    
    /**
     * Process a payment
     */
    PaymentResponse processPayment(UUID userId, UUID domainId, UUID tenantId, ProcessPaymentRequest request);
    
    /**
     * Refund a payment
     */
    PaymentResponse refundPayment(UUID userId, UUID domainId, UUID tenantId, List<String> userRoles, RefundPaymentRequest request);
    
    /**
     * Save payment method
     */
    PaymentMethodResponse savePaymentMethod(UUID userId, UUID domainId, UUID tenantId, SavePaymentMethodRequest request);
    
    /**
     * Get saved payment methods
     */
    List<PaymentMethodResponse> getPaymentMethods(UUID userId, UUID domainId, UUID tenantId);
    
    /**
     * Delete payment method
     */
    void deletePaymentMethod(UUID userId, UUID domainId, UUID tenantId, UUID paymentMethodId);
    
    /**
     * Get payment history
     */
    Page<PaymentResponse> getPaymentHistory(UUID userId, UUID domainId, UUID tenantId, Pageable pageable);
    
    /**
     * Get payment status
     */
    PaymentResponse getPaymentStatus(UUID userId, UUID domainId, UUID tenantId, List<String> userRoles, UUID paymentId);
    
    /**
     * Handle webhook from payment gateway
     */
    void handleWebhook(String payload, String signature);
    
    /**
     * Create a Razorpay order for client-side checkout
     * Returns order_id that can be used to open Razorpay checkout modal
     */
    com.nexus.shared.payment.model.response.CreateOrderResponse createOrder(UUID userId, UUID domainId, UUID tenantId, com.nexus.shared.payment.model.request.CreateOrderRequest request);
}

