package com.nexus.shared.payment.gateway.dto;

import com.nexus.shared.payment.entity.PaymentRefund;

/**
 * Refund Response DTO from gateway
 */
public record RefundResponse(
    String gatewayRefundId,
    PaymentRefund.RefundStatus status,
    String failureReason
) {}

