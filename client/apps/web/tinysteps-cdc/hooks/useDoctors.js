'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { healthcareApi } from '@ecom/api-client';

/**
 * Hook for fetching doctors list
 */
export function useDoctors(params = {}) {
  return useQuery({
    queryKey: ['doctors', params],
    queryFn: async () => {
      try {
        const result = await healthcareApi.doctor.search(params);
        console.log('✅ Doctors fetched successfully:', { count: result?.content?.length || 0 });
        return result;
      } catch (error) {
        const fullUrl = error?.config?.baseURL + error?.config?.url;
        console.error('❌ Error fetching doctors:', {
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
 * Hook for fetching a single doctor
 */
export function useDoctor(doctorId) {
  return useQuery({
    queryKey: ['doctors', doctorId],
    queryFn: async () => {
      try {
        return await healthcareApi.doctor.getById(doctorId);
      } catch (error) {
        console.error('Error fetching doctor:', {
          error,
          doctorId,
          message: error?.response?.data?.message || error?.message,
          status: error?.response?.status,
          url: error?.config?.url,
        });
        throw error;
      }
    },
    enabled: !!doctorId,
  });
}

/**
 * Hook for creating a doctor
 */
export function useCreateDoctor() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data) => healthcareApi.doctor.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['doctors'] });
    },
  });
}

/**
 * Hook for updating a doctor
 */
export function useUpdateDoctor() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ doctorId, data }) => healthcareApi.doctor.update(doctorId, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['doctors'] });
      queryClient.invalidateQueries({ queryKey: ['doctors', variables.doctorId] });
    },
  });
}

/**
 * Hook for deleting a doctor
 */
export function useDeleteDoctor() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (doctorId) => healthcareApi.doctor.delete(doctorId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['doctors'] });
    },
  });
}

/**
 * Hook for transferring a doctor to another branch
 */
export function useTransferDoctor() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ doctorId, targetTenantId }) => 
      healthcareApi.doctor.transfer(doctorId, targetTenantId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['doctors'] });
    },
  });
}

/**
 * Hook for adding a doctor to another branch
 */
export function useAddDoctorToBranch() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ doctorId, targetTenantId }) => 
      healthcareApi.doctor.addToBranch(doctorId, targetTenantId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['doctors'] });
    },
  });
}

/**
 * Hook for verifying a doctor
 */
export function useVerifyDoctor() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ doctorId, verificationStatus }) => 
      healthcareApi.doctor.verify(doctorId, verificationStatus),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['doctors'] });
      queryClient.invalidateQueries({ queryKey: ['doctors', variables.doctorId] });
    },
  });
}

