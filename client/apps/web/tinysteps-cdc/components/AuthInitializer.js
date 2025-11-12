'use client';

import { useEffect } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { useBranchStore } from '@/stores/branch-store';
import apiClient from '@ecom/api-client';

/**
 * Auth Initializer Component
 * 
 * Checks authentication status on app load.
 * Reads cookies via API route and syncs with Zustand store.
 * Sets up API client user provider for tenantId access.
 * Uses selected branch's tenantId for API calls.
 */
export function AuthInitializer() {
  const { checkAuthStatus, user } = useAuthStore();
  const { getSelectedBranch } = useBranchStore();

  useEffect(() => {
    // Set up API client user provider to get tenantId from selected branch
    // This allows healthcare API to automatically include tenantId in requests
    // Priority: selected branch > user's tenantId
    apiClient.setUserProvider(() => {
      const branchStore = useBranchStore.getState();
      const selectedBranchId = branchStore.getSelectedBranch();
      const authUser = useAuthStore.getState().user;
      
      // Return user object with tenantId from selected branch
      return {
        ...authUser,
        tenantId: selectedBranchId || authUser?.tenantId,
      };
    });
    
    // Set up API client token provider to get access token from cookies
    // The API client will fetch token from /api/auth/token if provider returns null
    apiClient.setTokenProvider(() => {
      // Tokens are in httpOnly cookies, so we can't read them directly
      // Return null to trigger fallback to /api/auth/token route
      return null;
    });
    
    // Check auth status when app loads
    checkAuthStatus();
  }, [checkAuthStatus, getSelectedBranch]);

  // Update user provider when user or branch changes
  useEffect(() => {
    apiClient.setUserProvider(() => {
      const branchStore = useBranchStore.getState();
      const selectedBranchId = branchStore.getSelectedBranch();
      const authUser = useAuthStore.getState().user;
      
      return {
        ...authUser,
        tenantId: selectedBranchId || authUser?.tenantId,
      };
    });
  }, [user, getSelectedBranch]);

  // This component doesn't render anything
  return null;
}

