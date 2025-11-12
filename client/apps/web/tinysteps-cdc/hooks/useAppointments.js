'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { healthcareApi } from '@ecom/api-client';

/**
 * Hook for fetching appointments by patient
 */
export function useAppointmentsByPatient(patientId) {
  return useQuery({
    queryKey: ['appointments', 'patient', patientId],
    queryFn: () => healthcareApi.appointment.getByPatient(patientId),
    enabled: !!patientId,
  });
}

/**
 * Hook for fetching appointments by doctor
 */
export function useAppointmentsByDoctor(doctorId, date) {
  return useQuery({
    queryKey: ['appointments', 'doctor', doctorId, date],
    queryFn: () => healthcareApi.appointment.getByDoctor(doctorId, date),
    enabled: !!doctorId && !!date,
  });
}

/**
 * Hook for fetching available slots
 */
export function useAvailableSlots(doctorId, date, sessionTypeId) {
  return useQuery({
    queryKey: ['appointments', 'slots', doctorId, date, sessionTypeId],
    queryFn: () => healthcareApi.appointment.getAvailableSlots(doctorId, date, sessionTypeId),
    enabled: !!doctorId && !!date && !!sessionTypeId,
  });
}

/**
 * Hook for fetching a single appointment
 */
export function useAppointment(appointmentId) {
  return useQuery({
    queryKey: ['appointments', appointmentId],
    queryFn: () => healthcareApi.appointment.getById(appointmentId),
    enabled: !!appointmentId,
  });
}

/**
 * Hook for creating an appointment
 */
export function useCreateAppointment() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data) => healthcareApi.appointment.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}

/**
 * Hook for admin booking
 */
export function useCreateAdminBooking() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ data, sendPaymentLink }) => 
      healthcareApi.appointment.createAdminBooking(data, sendPaymentLink),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
  });
}

/**
 * Hook for updating an appointment
 */
export function useUpdateAppointment() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ appointmentId, data }) => 
      healthcareApi.appointment.update(appointmentId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      queryClient.invalidateQueries({ queryKey: ['appointments', variables.appointmentId] });
    },
  });
}

/**
 * Hook for canceling an appointment
 */
export function useCancelAppointment() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ appointmentId, reason }) => 
      healthcareApi.appointment.cancel(appointmentId, reason),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      queryClient.invalidateQueries({ queryKey: ['appointments', variables.appointmentId] });
    },
  });
}

/**
 * Hook for completing an appointment
 */
export function useCompleteAppointment() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (appointmentId) => healthcareApi.appointment.complete(appointmentId),
    onSuccess: (_, appointmentId) => {
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      queryClient.invalidateQueries({ queryKey: ['appointments', appointmentId] });
    },
  });
}

