package com.nexus.healthcare.appointment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    
    private UUID id;
    private UUID doctorId;
    private UUID patientId;
    private UUID sessionTypeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String paymentStatus;
    private UUID paymentId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

