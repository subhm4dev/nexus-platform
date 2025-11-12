'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { healthcareApi } from '@ecom/api-client';

/**
 * Hook for fetching patients list
 */
export function usePatients(params = {}) {
  return useQuery({
    queryKey: ['patients', params],
    queryFn: async () => {
      try {
        const result = await healthcareApi.patient.search(params);
        console.log('✅ Patients fetched successfully:', { count: result?.content?.length || 0 });
        return result;
      } catch (error) {
        const fullUrl = error?.config?.baseURL + error?.config?.url;
        console.error('❌ Error fetching patients:', {
          message: error?.response?.data?.message || error?.message,
          status: error?.response?.status,
          fullUrl,
          baseURL: error?.config?.baseURL,
          path: error?.config?.url,
          method: error?.config?.method,
          params,
          response: error?.response?.data,
        });
        throw error;
      }
    },
  });
}

/**
 * Hook for fetching a single patient
 */
export function usePatient(patientId) {
  return useQuery({
    queryKey: ['patients', patientId],
    queryFn: async () => {
      try {
        return await healthcareApi.patient.getById(patientId);
      } catch (error) {
        console.error('Error fetching patient:', {
          error,
          patientId,
          message: error?.response?.data?.message || error?.message,
          status: error?.response?.status,
          url: error?.config?.url,
        });
        throw error;
      }
    },
    enabled: !!patientId,
  });
}

/**
 * Hook for creating a patient
 */
export function useCreatePatient() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data) => healthcareApi.patient.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
    },
  });
}

/**
 * Hook for updating a patient
 */
export function useUpdatePatient() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ patientId, data }) => healthcareApi.patient.update(patientId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
      queryClient.invalidateQueries({ queryKey: ['patients', variables.patientId] });
    },
  });
}

/**
 * Hook for deleting a patient
 */
export function useDeletePatient() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (patientId) => healthcareApi.patient.delete(patientId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
    },
  });
}

/**
 * Hook for transferring a patient to another branch
 */
export function useTransferPatient() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ patientId, targetTenantId }) => 
      healthcareApi.patient.transfer(patientId, targetTenantId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
    },
  });
}

/**
 * Hook for adding a patient to another branch
 */
export function useAddPatientToBranch() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ patientId, targetTenantId }) => 
      healthcareApi.patient.addToBranch(patientId, targetTenantId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['patients'] });
    },
  });
}

