'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { healthcareApi } from '@ecom/api-client';

/**
 * Hook for fetching time-offs by doctor
 */
export function useTimeOffsByDoctor(doctorId) {
  return useQuery({
    queryKey: ['timeOffs', 'doctor', doctorId],
    queryFn: () => healthcareApi.timeOff.getByDoctor(doctorId),
    enabled: !!doctorId,
  });
}

/**
 * Hook for creating time-off
 */
export function useCreateTimeOff() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data) => healthcareApi.timeOff.create(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['timeOffs'] });
      if (variables.doctorId) {
        queryClient.invalidateQueries({ queryKey: ['timeOffs', 'doctor', variables.doctorId] });
      }
    },
  });
}

/**
 * Hook for updating time-off
 */
export function useUpdateTimeOff() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ timeOffId, data }) => 
      healthcareApi.timeOff.update(timeOffId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['timeOffs'] });
    },
  });
}

/**
 * Hook for deleting time-off
 */
export function useDeleteTimeOff() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (timeOffId) => healthcareApi.timeOff.delete(timeOffId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['timeOffs'] });
    },
  });
}

