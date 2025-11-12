package com.nexus.healthcare.appointment.service;

import com.nexus.healthcare.appointment.controller.PaymentController.CashRegisterBalanceResponse;
import com.nexus.healthcare.appointment.controller.PaymentController.PaymentReconciliationResponse;
import com.nexus.healthcare.appointment.model.request.PosPaymentRequest;
import com.nexus.healthcare.appointment.model.response.PaymentResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface PaymentService {
    
    PaymentResponse processPosPayment(UUID userId, UUID tenantId, UUID domainId, PosPaymentRequest request);
    
    PaymentResponse processManualPayment(UUID userId, UUID tenantId, UUID domainId, PosPaymentRequest request);
    
    String generateReceipt(UUID paymentId, UUID tenantId, UUID domainId);
    
    void openCashRegister(UUID userId, UUID tenantId, UUID domainId, BigDecimal openingBalance, String notes);
    
    void closeCashRegister(UUID userId, UUID tenantId, UUID domainId, BigDecimal closingBalance, String notes);
    
    CashRegisterBalanceResponse getCashRegisterBalance(UUID tenantId, UUID domainId);
    
    PaymentReconciliationResponse getPaymentReconciliation(UUID tenantId, UUID domainId, LocalDate date);
}

