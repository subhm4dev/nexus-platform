'use client';

import { useQuery } from '@tanstack/react-query';
import { healthcareApi } from '@ecom/api-client';

/**
 * Hook for fetching distinct qualification names
 * Returns a simple list of qualification name strings (e.g., ["MBBS", "MD", "PhD"])
 */
export function useQualificationNames() {
  return useQuery({
    queryKey: ['qualificationNames'],
    queryFn: async () => {
      try {
        const names = await healthcareApi.qualification.getNames();
        return names || [];
      } catch (error) {
        console.error('Error fetching qualification names:', {
          error,
          message: error?.response?.data?.message || error?.message,
          status: error?.response?.status,
          url: error?.config?.url,
        });
        // Return empty array on error so form can still work
        return [];
      }
    },
    staleTime: 1000 * 60 * 5, // Cache for 5 minutes
  });
}

