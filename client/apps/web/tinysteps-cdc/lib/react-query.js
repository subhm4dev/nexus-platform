'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useState } from 'react';

/**
 * React Query Provider
 * 
 * Wraps the app with QueryClientProvider for server state management.
 */
export function ReactQueryProvider({ children }) {
  // Create QueryClient once per app instance
  // Using useState with function initializer ensures it's only created once
  // This prevents recreating the client on every re-render (performance optimization)
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            // Data is considered fresh for 1 minute
            // During this time, React Query uses cached data (no API call)
            // After 1 minute, data becomes "stale" and refetches in background
            staleTime: 60 * 1000,

            // Don't refetch when user switches browser tabs back
            // Reduces unnecessary API calls
            // Set to true if you need real-time data updates
            refetchOnWindowFocus: false,

            // Retry failed requests once before showing error
            // Handles temporary network issues gracefully
            retry: 1,

            // Don't throw errors, store them in error state instead
            // Allows components to handle errors gracefully
            throwOnError: false,
          },
          mutations: {
            // Retry failed mutations (POST/PUT/DELETE) once
            // Useful for network hiccups during form submissions
            retry: 1,
          },
        },
      })
  );

  return (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}

