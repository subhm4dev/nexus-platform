package com.nexus.healthcare.doctor.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class DoctorRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    private String registrationNumber;
    
    private Integer yearsOfExperience;
    
    private BigDecimal consultationFee;
    
    private String bio;
    
    private String profileImageUrl;
    
    private List<UUID> specializationIds;
    
    // Simple qualification name - just the name string (e.g., "MBBS", "MD")
    // If it doesn't exist, it will be created when saving to qualifications table
    private String qualificationName;
    
    private String qualificationInstitution;
    
    private Integer qualificationYear;
}

