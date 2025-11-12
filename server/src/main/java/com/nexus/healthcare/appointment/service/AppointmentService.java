package com.nexus.healthcare.appointment.service;

import com.nexus.healthcare.appointment.model.request.AppointmentRequest;
import com.nexus.healthcare.appointment.model.response.AppointmentResponse;
import com.nexus.healthcare.appointment.model.response.AvailableSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {
    
    AppointmentResponse createAppointment(UUID userId, UUID tenantId, UUID domainId, AppointmentRequest request);
    
    AppointmentResponse getAppointmentById(UUID appointmentId, UUID tenantId, UUID domainId);
    
    List<AppointmentResponse> getAppointmentsByPatient(UUID patientId, UUID tenantId, UUID domainId);
    
    List<AppointmentResponse> getAppointmentsByDoctor(UUID doctorId, UUID tenantId, UUID domainId, LocalDate date);
    
    List<AvailableSlot> getAvailableSlots(UUID doctorId, LocalDate date, UUID sessionTypeId, UUID tenantId, UUID domainId);
    
    AppointmentResponse updateAppointment(UUID appointmentId, UUID tenantId, UUID domainId, AppointmentRequest request);
    
    AppointmentResponse cancelAppointment(UUID appointmentId, UUID tenantId, UUID domainId, String reason);
    
    AppointmentResponse completeAppointment(UUID appointmentId, UUID tenantId, UUID domainId);
    
    AppointmentResponse createAdminBooking(UUID adminUserId, UUID tenantId, UUID domainId, AppointmentRequest request, boolean sendPaymentLink);
}

