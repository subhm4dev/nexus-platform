'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/stores/auth-store';
import apiClient from '@ecom/api-client';

/**
 * Auth Initializer Component
 * 
 * Checks authentication status on app load.
 * Reads cookies via API route and syncs with Zustand store.
 * Sets up API client user provider for tenantId access.
 * Listens for token revocation events and handles logout.
 */
export function AuthInitializer() {
  const { checkAuthStatus, user, logout } = useAuthStore();
  const router = useRouter();

  useEffect(() => {
    // Set up API client user provider to get tenantId
    // This allows catalog API to automatically include tenantId in requests
    apiClient.setUserProvider(() => useAuthStore.getState().user);
    
    // Set up API client token provider to get access token from cookies
    // The API client will fetch token from /api/auth/token if provider returns null
    apiClient.setTokenProvider(() => {
      // Tokens are in httpOnly cookies, so we can't read them directly
      // Return null to trigger fallback to /api/auth/token route
      return null;
    });
    
    // Check auth status when app loads
    checkAuthStatus();
  }, [checkAuthStatus]);

  // Update user provider when user changes
  useEffect(() => {
    apiClient.setUserProvider(() => useAuthStore.getState().user);
  }, [user]);

  // Listen for token revocation events (dispatched by API client on 401 with revoked token)
  useEffect(() => {
    let isLoggingOut = false; // Guard to prevent multiple simultaneous logout attempts

    const handleAuthLogout = async () => {
      // Prevent multiple simultaneous logout attempts
      if (isLoggingOut) {
        return;
      }

      // Check if already logged out
      const { isAuthenticated } = useAuthStore.getState();
      if (!isAuthenticated) {
        return; // Already logged out, no need to do anything
      }

      isLoggingOut = true;
      try {
        // Token was revoked (blacklisted), clear auth state
        // Skip the API call since token is already revoked - just clear local state
        const { logout } = useAuthStore.getState();
        // Clear state directly without calling logout API (token is already revoked)
        useAuthStore.setState({
          user: null,
          isAuthenticated: false,
          isLoading: false,
          error: null,
          pendingAction: null,
        });
        // Redirect to home page
        router.push('/');
      } catch (error) {
        console.error('Error during auto-logout:', error);
      } finally {
        // Reset flag after a delay to allow for any cleanup
        setTimeout(() => {
          isLoggingOut = false;
        }, 1000);
      }
    };

    if (typeof window !== 'undefined') {
      window.addEventListener('auth:logout', handleAuthLogout);
      return () => {
        window.removeEventListener('auth:logout', handleAuthLogout);
      };
    }
  }, [router]);

  // This component doesn't render anything
  return null;
}

