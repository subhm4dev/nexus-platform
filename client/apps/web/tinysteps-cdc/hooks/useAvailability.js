'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { healthcareApi } from '@ecom/api-client';

/**
 * Hook for fetching availabilities by doctor
 */
export function useAvailabilitiesByDoctor(doctorId) {
  return useQuery({
    queryKey: ['availabilities', 'doctor', doctorId],
    queryFn: () => healthcareApi.availability.getByDoctor(doctorId),
    enabled: !!doctorId,
  });
}

/**
 * Hook for creating availability
 */
export function useCreateAvailability() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data) => healthcareApi.availability.create(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['availabilities'] });
      if (variables.doctorId) {
        queryClient.invalidateQueries({ queryKey: ['availabilities', 'doctor', variables.doctorId] });
      }
    },
  });
}

/**
 * Hook for updating availability
 */
export function useUpdateAvailability() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ availabilityId, data }) => 
      healthcareApi.availability.update(availabilityId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['availabilities'] });
    },
  });
}

/**
 * Hook for deleting availability
 */
export function useDeleteAvailability() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (availabilityId) => healthcareApi.availability.delete(availabilityId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['availabilities'] });
    },
  });
}

