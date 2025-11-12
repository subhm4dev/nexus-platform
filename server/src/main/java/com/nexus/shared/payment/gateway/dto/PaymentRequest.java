package com.nexus.shared.payment.gateway.dto;

import com.nexus.shared.payment.entity.Payment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payment Request DTO for gateway
 */
public record PaymentRequest(
    UUID userId,
    UUID tenantId,
    UUID orderId,
    UUID paymentMethodId,
    Payment.PaymentMethodType methodType,
    BigDecimal amount,
    String currency,
    String token, // Tokenized payment method reference
    String upiId, // For UPI
    String phoneNumber, // For UPI/Wallet
    String description,
    String callbackUrl
) {}

