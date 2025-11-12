package com.nexus.healthcare.consultation.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ConsultationRequest {
    
    @NotNull(message = "Appointment ID is required")
    private UUID appointmentId;
    
    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    private LocalDateTime consultationDate;
    
    private String diagnosis;
    
    private String notes;
}

