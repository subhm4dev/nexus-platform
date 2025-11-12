package com.nexus.healthcare.doctor.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QualificationRequest {
    
    @NotBlank(message = "Code is required")
    private String code;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    
    private Boolean active;
}

