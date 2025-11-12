package com.nexus.shared.payment.gateway.dto;

import com.nexus.shared.payment.entity.Payment;

/**
 * Payment Response DTO from gateway
 */
public record PaymentResponse(
    String gatewayTransactionId,
    String gatewayPaymentId,
    Payment.PaymentStatus status,
    String paymentLink, // For UPI/Payment Links
    String qrCode, // For UPI QR codes
    String failureReason
) {}

