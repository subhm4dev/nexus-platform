'use client';

import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import apiClient, { authApi } from '@ecom/api-client';

/**
 * Decode JWT token payload to extract branches
 */
function decodeJwtPayload(token) {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    
    const payload = parts[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
    const decoded = Buffer.from(padded, 'base64').toString('utf-8');
    
    return JSON.parse(decoded);
  } catch (error) {
    return null;
  }
}

/**
 * Extract branches from JWT token
 * Branches are stored in JWT claims - typically in 'branches' or 'tenantIds' array
 */
function extractBranchesFromToken(token) {
  if (!token) {
    console.warn('extractBranchesFromToken: No token provided');
    return [];
  }
  
  const payload = decodeJwtPayload(token);
  if (!payload) {
    console.warn('extractBranchesFromToken: Could not decode token payload');
    return [];
  }
  
  // Debug: Log all payload keys to see what's available
  console.log('JWT Payload keys:', Object.keys(payload));
  console.log('JWT Payload (sanitized):', {
    tenantId: payload.tenantId,
    tenantType: payload.tenantType,
    hasBranches: !!payload.branches,
    hasTenantIds: !!payload.tenantIds,
    hasAccessibleTenants: !!payload.accessibleTenants,
    branchesValue: payload.branches,
    tenantIdsValue: payload.tenantIds,
    accessibleTenantsValue: payload.accessibleTenants,
  });
  
  // Try different possible claim names for branches
  let branches = payload.branches || payload.tenantIds || payload.accessibleTenants || [];
  
  // If branches is not an array, try to convert it
  if (!Array.isArray(branches)) {
    if (typeof branches === 'string') {
      // Try to parse as JSON if it's a string
      try {
        branches = JSON.parse(branches);
      } catch {
        branches = [branches];
      }
    } else if (branches) {
      branches = [branches];
    } else {
      branches = [];
    }
  }
  
  // If tenantId exists, always include it as a branch option
  if (payload.tenantId) {
    if (!branches.includes(payload.tenantId)) {
      branches = [payload.tenantId, ...branches];
    }
  }
  
  console.log('Extracted branches:', branches);
  return branches;
}

/**
 * Auth Store for Healthcare Domain
 * 
 * Manages authentication UI state with healthcare domain support.
 * - User info stored in localStorage (for persistence across refreshes)
 * - Tokens stored in cookies (set by backend, more secure)
 * - Extracts branches from JWT token for multi-branch support
 */
export const useAuthStore = create(
  persist(
    (set, get) => ({
      // State
      user: null,
      isAuthenticated: false,
      isLoading: false,
      hasCheckedAuth: false,
      error: null,
      branches: [], // Accessible branches from JWT

      // Check auth status by calling API route
      checkAuthStatus: async () => {
        set({ isLoading: true, hasCheckedAuth: false });
        try {
          const response = await fetch('/api/auth/status');
          const data = await response.json();
          
          if (data.isAuthenticated) {
            // Get token to extract branches
            const tokenResponse = await fetch('/api/auth/token');
            const tokenData = await tokenResponse.json();
            const branches = extractBranchesFromToken(tokenData.accessToken);
            
            // Preserve existing user data (like email) from persisted state
            const existingUser = get().user || {};
            
            set({
              isAuthenticated: true,
              user: {
                ...existingUser, // Preserve other fields
                id: data.userId || existingUser.id,
                roles: data.roles || existingUser.roles || [],
                tenantId: data.tenantId || existingUser.tenantId,
                // Use email from API response (JWT) or fallback to persisted state
                email: data.email || existingUser.email,
              },
              branches,
              isLoading: false,
              hasCheckedAuth: true,
              error: null,
            });
          } else {
            set({
              isAuthenticated: false,
              user: null,
              branches: [],
              isLoading: false,
              hasCheckedAuth: true,
              error: null,
            });
          }
        } catch (error) {
          set({
            isAuthenticated: false,
            isLoading: false,
            hasCheckedAuth: true,
            error: 'Failed to check auth status',
            branches: [],
          });
        }
      },

      // Check if user has specific role
      hasRole: (role) => {
        const user = get().user;
        if (!user || !user.roles) return false;
        return Array.isArray(user.roles) 
          ? user.roles.includes(role)
          : user.roles === role;
      },

      // Check if user has ADMIN role
      isAdmin: () => {
        return get().hasRole('ADMIN');
      },

      // Check if user has DOCTOR role
      isDoctor: () => {
        return get().hasRole('DOCTOR');
      },

      // Check if user has PATIENT role
      isPatient: () => {
        return get().hasRole('PATIENT');
      },

      // Check if user has RECEPTIONIST role
      isReceptionist: () => {
        return get().hasRole('RECEPTIONIST');
      },

      // Login
      login: async (data) => {
        set({ isLoading: true, error: null });
        try {
          // Ensure domainCode is "healthcare" for healthcare domain
          const loginData = {
            ...data,
            domainCode: data.domainCode || 'healthcare',
          };
          
          const response = await authApi.login(loginData);
          
          // Extract branches from token
          const tokenResponse = await fetch('/api/auth/token');
          const tokenData = await tokenResponse.json();
          const branches = extractBranchesFromToken(tokenData.accessToken);
          
          // Backend returns role as array or single value
          const roles = Array.isArray(response.role) 
            ? response.role 
            : response.role 
              ? [response.role] 
              : [];
          
          set({
            user: {
              id: response.id,
              roles: roles,
              tenantId: response.tenantId,
            },
            branches,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });

          return response;
        } catch (error) {
          set({
            isLoading: false,
            error: error.response?.data?.message || error.message || 'Login failed',
          });
          throw error;
        }
      },

      // Register
      register: async (data) => {
        set({ isLoading: true, error: null });
        try {
          // Ensure domainCode is "healthcare" and tenantId is included
          const registrationData = {
            ...data,
            domainCode: data.domainCode || 'healthcare',
            tenantId: data.tenantId || (typeof window !== 'undefined' 
              ? window.process?.env?.NEXT_PUBLIC_APP_TENANT_ID || process.env.NEXT_PUBLIC_APP_TENANT_ID
              : process.env.NEXT_PUBLIC_APP_TENANT_ID),
          };
          
          if (!registrationData.tenantId) {
            throw new Error('NEXT_PUBLIC_APP_TENANT_ID environment variable is required. Please configure it in your .env.local file.');
          }
          
          const response = await authApi.register(registrationData);
          
          // Extract branches from token
          const tokenResponse = await fetch('/api/auth/token');
          const tokenData = await tokenResponse.json();
          const branches = extractBranchesFromToken(tokenData.accessToken);
          
          const roles = Array.isArray(response.role) 
            ? response.role 
            : response.role 
              ? [response.role] 
              : [];
          
          set({
            user: {
              id: response.id,
              roles: roles,
              tenantId: response.tenantId,
            },
            branches,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });

          return response;
        } catch (error) {
          set({
            isLoading: false,
            error: error.response?.data?.message || error.message || 'Registration failed',
          });
          throw error;
        }
      },

      // Logout
      logout: async () => {
        set({ isLoading: true });
        try {
          await authApi.logout();
        } catch (error) {
          // Continue with logout even if API call fails
        } finally {
          set({
            user: null,
            branches: [],
            isAuthenticated: false,
            isLoading: false,
            error: null,
          });
        }
      },

      // Logout from all devices
      logoutAll: async () => {
        set({ isLoading: true });
        try {
          await authApi.logoutAll();
        } catch (error) {
          // Continue with logout
        } finally {
          set({
            user: null,
            branches: [],
            isAuthenticated: false,
            isLoading: false,
            error: null,
          });
        }
      },

      // Clear error
      clearError: () => {
        set({ error: null });
      },
    }),
    {
      name: 'tinysteps-auth-storage', // localStorage key
      partialize: (state) => ({
        // Only persist user info and auth status, NOT tokens
        // Tokens are in cookies (set by backend)
        user: state.user,
        isAuthenticated: state.isAuthenticated,
        branches: state.branches,
      }),
    }
  )
);

