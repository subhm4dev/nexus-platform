package com.nexus.healthcare.appointment.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.healthcare.appointment.entity.Appointment;
import com.nexus.healthcare.appointment.model.request.AppointmentRequest;
import com.nexus.healthcare.appointment.model.response.AppointmentResponse;
import com.nexus.healthcare.appointment.repository.AppointmentRepository;
import com.nexus.healthcare.appointment.service.AppointmentService;
import com.nexus.healthcare.appointment.service.SlotAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final SlotAvailabilityService slotAvailabilityService;
    // TODO: Inject session-service client to get session duration
    // TODO: Inject payment-service client for payment integration
    
    @Override
    @Transactional
    public AppointmentResponse createAppointment(UUID userId, UUID tenantId, UUID domainId, AppointmentRequest request) {
        log.info("Creating appointment for patient: {}, doctor: {}, tenant: {}", request.getPatientId(), request.getDoctorId(), tenantId);
        
        // Validate slot is available
        LocalDate appointmentDate = request.getStartTime().toLocalDate();
        List<com.nexus.healthcare.appointment.model.response.AvailableSlot> availableSlots = 
            slotAvailabilityService.calculateAvailableSlots(request.getDoctorId(), appointmentDate, request.getSessionTypeId(), tenantId, domainId);
        
        boolean slotAvailable = availableSlots.stream()
            .anyMatch(slot -> slot.getStartTime().equals(request.getStartTime()));
        
        if (!slotAvailable) {
            throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Requested slot is not available");
        }
        
        // Calculate end time (TODO: Get from session-service)
        Duration sessionDuration = Duration.ofMinutes(30); // Default, replace with service call
        LocalDateTime endTime = request.getStartTime().plus(sessionDuration);
        
        // Check for conflicts with optimistic locking
        Appointment appointment = Appointment.builder()
            .doctorId(request.getDoctorId())
            .patientId(request.getPatientId())
            .sessionTypeId(request.getSessionTypeId())
            .startTime(request.getStartTime())
            .endTime(endTime)
            .status("SCHEDULED")
            .paymentStatus("PAYMENT_PENDING")
            .tenantId(tenantId)
            .domainId(domainId)
            .notes(request.getNotes())
            .build();
        
        try {
            Appointment saved = appointmentRepository.save(appointment);
            log.info("Created appointment: {}", saved.getId());
            return mapToResponse(saved);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Unique constraint violation = double booking
            throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Slot is already booked. Please select another time.");
        }
    }
    
    @Override
    public AppointmentResponse getAppointmentById(UUID appointmentId, UUID tenantId, UUID domainId) {
        Appointment appointment = appointmentRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(appointmentId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));
        return mapToResponse(appointment);
    }
    
    @Override
    public List<AppointmentResponse> getAppointmentsByPatient(UUID patientId, UUID tenantId, UUID domainId) {
        List<Appointment> appointments = appointmentRepository.findByPatientIdAndTenantIdAndDomainIdAndDeletedFalse(patientId, tenantId, domainId);
        return appointments.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    @Override
    public List<AppointmentResponse> getAppointmentsByDoctor(UUID doctorId, UUID tenantId, UUID domainId, LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(23, 59, 59);
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndStartTimeBetweenAndDeletedFalseAndStatusNot(
            doctorId, dayStart, dayEnd, "CANCELLED");
        return appointments.stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
    @Override
    public List<com.nexus.healthcare.appointment.model.response.AvailableSlot> getAvailableSlots(
            UUID doctorId, LocalDate date, UUID sessionTypeId, UUID tenantId, UUID domainId) {
        return slotAvailabilityService.calculateAvailableSlots(doctorId, date, sessionTypeId, tenantId, domainId);
    }
    
    @Override
    @Transactional
    public AppointmentResponse updateAppointment(UUID appointmentId, UUID tenantId, UUID domainId, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(appointmentId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));
        
        if (request.getStartTime() != null) {
            appointment.setStartTime(request.getStartTime());
            // Recalculate end time
            Duration sessionDuration = Duration.ofMinutes(30); // TODO: Get from session-service
            appointment.setEndTime(request.getStartTime().plus(sessionDuration));
        }
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }
        
        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(UUID appointmentId, UUID tenantId, UUID domainId, String reason) {
        Appointment appointment = appointmentRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(appointmentId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));
        
        appointment.setStatus("CANCELLED");
        if (reason != null) {
            appointment.setNotes((appointment.getNotes() != null ? appointment.getNotes() + "\n" : "") + "Cancellation reason: " + reason);
        }
        
        // TODO: Trigger refund processing if payment was completed
        
        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public AppointmentResponse completeAppointment(UUID appointmentId, UUID tenantId, UUID domainId) {
        Appointment appointment = appointmentRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(appointmentId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Appointment not found"));
        
        appointment.setStatus("COMPLETED");
        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public AppointmentResponse createAdminBooking(UUID adminUserId, UUID tenantId, UUID domainId, AppointmentRequest request, boolean sendPaymentLink) {
        log.info("Admin booking appointment for patient: {}, doctor: {}", request.getPatientId(), request.getDoctorId());
        
        // Create appointment (same as regular booking)
        AppointmentResponse appointment = createAppointment(adminUserId, tenantId, domainId, request);
        
        // TODO: Generate payment link if sendPaymentLink is true
        // TODO: Send payment link via SMS/Email
        
        return appointment;
    }
    
    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
            .id(appointment.getId())
            .doctorId(appointment.getDoctorId())
            .patientId(appointment.getPatientId())
            .sessionTypeId(appointment.getSessionTypeId())
            .startTime(appointment.getStartTime())
            .endTime(appointment.getEndTime())
            .status(appointment.getStatus())
            .paymentStatus(appointment.getPaymentStatus())
            .paymentId(appointment.getPaymentId())
            .notes(appointment.getNotes())
            .createdAt(appointment.getCreatedAt())
            .updatedAt(appointment.getUpdatedAt())
            .build();
    }
}

