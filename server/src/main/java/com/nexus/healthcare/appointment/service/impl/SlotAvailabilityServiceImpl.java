package com.nexus.healthcare.appointment.service.impl;

import com.nexus.healthcare.appointment.entity.Appointment;
import com.nexus.healthcare.appointment.entity.Availability;
import com.nexus.healthcare.appointment.entity.TimeOff;
import com.nexus.healthcare.appointment.model.response.AvailableSlot;
import com.nexus.healthcare.appointment.repository.AppointmentRepository;
import com.nexus.healthcare.appointment.repository.AvailabilityRepository;
import com.nexus.healthcare.appointment.repository.TimeOffRepository;
import com.nexus.healthcare.appointment.service.SlotAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Slot Availability Service Implementation
 * 
 * <p>CRITICAL: Intelligent slot calculation with session duration awareness,
 * conflict detection, and time-off exclusion.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SlotAvailabilityServiceImpl implements SlotAvailabilityService {
    
    private final AvailabilityRepository availabilityRepository;
    private final TimeOffRepository timeOffRepository;
    private final AppointmentRepository appointmentRepository;
    
    // TODO: Inject session-service client to get session duration
    // For now, using a default duration - will be replaced with actual service call
    
    @Override
    public List<AvailableSlot> calculateAvailableSlots(
            UUID doctorId,
            LocalDate date,
            UUID sessionTypeId,
            UUID tenantId,
            UUID domainId) {
        
        log.debug("Calculating available slots for doctor: {}, date: {}, sessionType: {}", doctorId, date, sessionTypeId);
        
        // 1. Get doctor's base availability for day of week
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        List<Availability> baseAvailability = availabilityRepository
            .findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);
        
        if (baseAvailability.isEmpty()) {
            log.debug("No base availability found for doctor: {}, day: {}", doctorId, dayOfWeek);
            return List.of();
        }
        
        // 2. Get all time-offs for this date
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(23, 59, 59);
        List<TimeOff> timeOffs = timeOffRepository
            .findByDoctorIdAndStartTimeBetweenOrEndTimeBetween(doctorId, dayStart, dayEnd);
        
        // 3. Get all existing appointments for this date (excluding cancelled)
        List<Appointment> existingAppointments = appointmentRepository
            .findByDoctorIdAndStartTimeBetweenAndDeletedFalseAndStatusNot(doctorId, dayStart, dayEnd, "CANCELLED");
        
        // 4. Get session duration (TODO: Call session-service to get actual duration)
        Duration sessionDuration = Duration.ofMinutes(30); // Default, replace with service call
        
        // 5. Generate potential slots from base availability
        List<AvailableSlot> potentialSlots = new ArrayList<>();
        for (Availability availability : baseAvailability) {
            LocalDateTime slotStart = date.atTime(availability.getStartTime());
            LocalDateTime slotEnd = date.atTime(availability.getEndTime());
            
            // Generate slots at 15-minute intervals within availability window
            LocalDateTime currentSlotStart = slotStart;
            while (currentSlotStart.plus(sessionDuration).isBefore(slotEnd) || 
                   currentSlotStart.plus(sessionDuration).equals(slotEnd)) {
                LocalDateTime currentSlotEnd = currentSlotStart.plus(sessionDuration);
                
                potentialSlots.add(AvailableSlot.builder()
                    .startTime(currentSlotStart)
                    .endTime(currentSlotEnd)
                    .durationMinutes(sessionDuration.toMinutes())
                    .build());
                
                currentSlotStart = currentSlotStart.plusMinutes(15); // Next slot
            }
        }
        
        // 6. Filter out slots during time-offs
        potentialSlots = filterTimeOffs(potentialSlots, timeOffs);
        
        // 7. Filter out conflicting slots with existing appointments
        potentialSlots = filterConflictingSlots(potentialSlots, existingAppointments, sessionDuration);
        
        log.debug("Found {} available slots for doctor: {}, date: {}", potentialSlots.size(), doctorId, date);
        
        return potentialSlots;
    }
    
    /**
     * Filter out slots that overlap with time-off periods
     */
    private List<AvailableSlot> filterTimeOffs(List<AvailableSlot> slots, List<TimeOff> timeOffs) {
        return slots.stream()
            .filter(slot -> {
                for (TimeOff timeOff : timeOffs) {
                    // Check if slot overlaps with time-off (even partially)
                    if (slot.getStartTime().isBefore(timeOff.getEndTime()) && 
                        slot.getEndTime().isAfter(timeOff.getStartTime())) {
                        return false; // Slot conflicts with time-off
                    }
                }
                return true; // Slot is available
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Filter out slots that conflict with existing appointments
     * 
     * <p>Conflict detection: Slots overlap if:
     * slotStart < appointmentEnd AND slotEnd > appointmentStart
     */
    private List<AvailableSlot> filterConflictingSlots(
            List<AvailableSlot> slots,
            List<Appointment> appointments,
            Duration sessionDuration) {
        
        return slots.stream()
            .filter(slot -> {
                LocalDateTime slotStart = slot.getStartTime();
                LocalDateTime slotEnd = slot.getEndTime();
                
                // Check if slot conflicts with any existing appointment
                return appointments.stream().noneMatch(apt -> {
                    LocalDateTime aptStart = apt.getStartTime();
                    LocalDateTime aptEnd = apt.getEndTime();
                    
                    // Conflict if slots overlap (even partially)
                    return slotStart.isBefore(aptEnd) && slotEnd.isAfter(aptStart);
                });
            })
            .collect(Collectors.toList());
    }
}

