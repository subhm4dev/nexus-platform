package com.nexus.healthcare.appointment.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PosPaymentRequest {
    
    @NotNull(message = "Appointment ID is required")
    private UUID appointmentId;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private String paymentMethod; // CASH, POS_CARD, POS_UPI, CHEQUE, BANK_TRANSFER
    
    @NotNull(message = "Payment source is required")
    private String paymentSource; // POS, MANUAL
    
    private String receiptNumber;
    
    private String posDeviceId;
    
    private String notes;
    
    private Boolean receiptRequired = true;
}

