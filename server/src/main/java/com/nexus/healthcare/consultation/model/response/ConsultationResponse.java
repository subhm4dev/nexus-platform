package com.nexus.healthcare.consultation.model.response;

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
public class ConsultationResponse {
    
    private UUID id;
    private UUID appointmentId;
    private UUID doctorId;
    private UUID patientId;
    private LocalDateTime consultationDate;
    private String diagnosis;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

