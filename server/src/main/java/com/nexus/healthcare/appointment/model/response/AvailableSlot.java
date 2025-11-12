package com.nexus.healthcare.appointment.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Available Slot Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlot {
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMinutes;
}

