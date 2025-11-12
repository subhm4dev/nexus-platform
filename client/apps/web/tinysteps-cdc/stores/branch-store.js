'use client';

import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { useAuthStore } from './auth-store';

/**
 * Branch Store
 * 
 * Manages selected branch state for multi-branch operations.
 * - Selected branch is persisted in localStorage
 * - Defaults to user's primary branch (first branch from JWT)
 * - All API calls should use selected branch's tenantId
 */
export const useBranchStore = create(
  persist(
    (set, get) => ({
      // State
      selectedBranchId: null, // UUID of selected branch (tenantId)

      // Set selected branch
      setSelectedBranch: (branchId) => {
        set({ selectedBranchId: branchId });
      },

      // Get selected branch ID
      // If no branch selected, defaults to user's primary branch
      getSelectedBranch: () => {
        const selected = get().selectedBranchId;
        if (selected) return selected;
        
        // Default to first branch from auth store
        const authStore = useAuthStore.getState();
        const branches = authStore.branches || [];
        if (branches.length > 0) {
          // Set and return first branch
          const firstBranch = branches[0];
          set({ selectedBranchId: firstBranch });
          return firstBranch;
        }
        
        // Fallback to user's tenantId if no branches
        return authStore.user?.tenantId || null;
      },

      // Reset selected branch (use primary branch)
      resetToPrimaryBranch: () => {
        const authStore = useAuthStore.getState();
        const branches = authStore.branches || [];
        if (branches.length > 0) {
          set({ selectedBranchId: branches[0] });
        } else {
          set({ selectedBranchId: authStore.user?.tenantId || null });
        }
      },

      // Check if branch is selected
      hasSelectedBranch: () => {
        return !!get().getSelectedBranch();
      },
    }),
    {
      name: 'tinysteps-branch-storage', // localStorage key
      partialize: (state) => ({
        selectedBranchId: state.selectedBranchId,
      }),
    }
  )
);

