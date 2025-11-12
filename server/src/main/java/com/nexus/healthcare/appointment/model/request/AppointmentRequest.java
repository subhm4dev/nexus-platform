package com.nexus.healthcare.appointment.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentRequest {
    
    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Session type ID is required")
    private UUID sessionTypeId;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    private String notes;
}

