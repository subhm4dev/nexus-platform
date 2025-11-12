package com.nexus.healthcare.appointment.service;

import com.nexus.healthcare.appointment.model.response.AvailableSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Slot Availability Service
 * 
 * <p>CRITICAL service for intelligent slot calculation with session duration awareness,
 * conflict detection, and time-off exclusion.
 */
public interface SlotAvailabilityService {
    
    /**
     * Calculate available slots for a doctor on a specific date
     * 
     * <p>Algorithm:
     * 1. Get doctor's base availability for day of week
     * 2. Get all time-offs for this date
     * 3. Get all existing appointments for this date (excluding cancelled)
     * 4. Get session duration
     * 5. Generate potential slots from base availability
     * 6. Filter out slots during time-offs
     * 7. Filter out conflicting slots with existing appointments
     * 8. Return available slots
     * 
     * @param doctorId Doctor ID
     * @param date Date to calculate slots for
     * @param sessionTypeId Session type ID (determines duration)
     * @param tenantId Tenant ID
     * @param domainId Domain ID
     * @return List of available slots
     */
    List<AvailableSlot> calculateAvailableSlots(
        UUID doctorId,
        LocalDate date,
        UUID sessionTypeId,
        UUID tenantId,
        UUID domainId
    );
}

