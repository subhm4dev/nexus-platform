'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { healthcareApi } from '@ecom/api-client';

/**
 * Hook for fetching all session types
 */
export function useSessionTypes() {
  return useQuery({
    queryKey: ['sessionTypes'],
    queryFn: () => healthcareApi.sessionType.getAll(),
  });
}

/**
 * Hook for fetching a single session type
 */
export function useSessionType(id) {
  return useQuery({
    queryKey: ['sessionTypes', id],
    queryFn: () => healthcareApi.sessionType.getById(id),
    enabled: !!id,
  });
}

/**
 * Hook for creating a session type
 */
export function useCreateSessionType() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (data) => healthcareApi.sessionType.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sessionTypes'] });
    },
  });
}

/**
 * Hook for updating a session type
 */
export function useUpdateSessionType() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }) => healthcareApi.sessionType.update(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['sessionTypes'] });
      queryClient.invalidateQueries({ queryKey: ['sessionTypes', variables.id] });
    },
  });
}

/**
 * Hook for deleting a session type
 */
export function useDeleteSessionType() {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id) => healthcareApi.sessionType.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sessionTypes'] });
    },
  });
}

