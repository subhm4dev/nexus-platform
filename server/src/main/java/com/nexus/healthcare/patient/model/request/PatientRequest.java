package com.nexus.healthcare.patient.model.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PatientRequest {
    
    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;
    private Integer heightCm;
    private BigDecimal weightKg;
}

