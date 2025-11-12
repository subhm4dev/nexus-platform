package com.nexus.healthcare.doctor.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    
    private UUID id;
    private UUID userId;
    private String registrationNumber;
    private String verificationStatus;
    private Integer yearsOfExperience;
    private BigDecimal consultationFee;
    private String bio;
    private String profileImageUrl;
    private List<SpecializationResponse> specializations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

