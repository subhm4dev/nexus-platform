package com.nexus.shared.payment.gateway.dto;

import com.nexus.shared.payment.entity.Payment;

/**
 * Payment Method Tokenize Request DTO
 */
public record PaymentMethodTokenizeRequest(
    Payment.PaymentMethodType type,
    String cardNumber, // For cards
    String expiryMonth,
    String expiryYear,
    String cvv,
    String upiId, // For UPI
    String phoneNumber // For wallets/UPI
) {}

