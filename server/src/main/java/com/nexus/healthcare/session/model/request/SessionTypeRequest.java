package com.nexus.healthcare.session.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SessionTypeRequest {
    
    @NotNull(message = "Name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Duration in minutes is required")
    private Integer durationMinutes;
}

