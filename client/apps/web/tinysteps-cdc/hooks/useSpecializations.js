'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { healthcareApi } from '@ecom/api-client';

/**
 * Hook for fetching all specializations
 */
export function useSpecializations() {
  return useQuery({
    queryKey: ['specializations'],
    queryFn: async () => {
      try {
        return await healthcareApi.specialization.getAll();
      } catch (error) {
        console.error('Error fetching specializations:', {
          error,
          message: error?.response?.data?.message || error?.message,
          status: error?.response?.status,
          url: error?.config?.url,
        });
        throw error;
      }
    },
  });
}

/**
 * Hook for fetching a single specialization
 */
export function useSpecialization(id) {
  return useQuery({
    queryKey: ['specializations', id],
    queryFn: async () => {
      try {
        return await healthcareApi.specialization.getById(id);
      } catch (error) {
        console.error('Error fetching specialization:', {
          error,
          id,
          message: error?.response?.data?.message || error?.message,
          status: error?.response?.status,
        });
        throw error;
      }
    },
    enabled: !!id,
  });
}

/**
 * Hook for creating a specialization
 */
export function useCreateSpecialization() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data) => healthcareApi.specialization.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['specializations'] });
    },
  });
}

/**
 * Hook for updating a specialization
 */
export function useUpdateSpecialization() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }) => healthcareApi.specialization.update(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['specializations'] });
      queryClient.invalidateQueries({ queryKey: ['specializations', variables.id] });
    },
  });
}

/**
 * Hook for deleting a specialization
 */
export function useDeleteSpecialization() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id) => healthcareApi.specialization.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['specializations'] });
    },
  });
}

