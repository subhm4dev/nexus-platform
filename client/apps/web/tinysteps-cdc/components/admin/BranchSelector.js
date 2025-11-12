'use client';

import { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/auth-store';
import { useBranchStore } from '@/stores/branch-store';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

/**
 * Branch Selector Component
 * 
 * Displays a dropdown to select the current branch.
 * All API operations will use the selected branch's tenantId.
 * 
 * Extracts branches from JWT token claims (branches, tenantIds, or accessibleTenants).
 * Falls back to user's tenantId if no branches found in JWT.
 */
export function BranchSelector() {
  const { user, branches: authBranches } = useAuthStore();
  const { selectedBranchId, setSelectedBranch, getSelectedBranch } = useBranchStore();
  const [branches, setBranches] = useState([]);

  // Get branches from auth store (extracted from JWT)
  useEffect(() => {
    if (authBranches && authBranches.length > 0) {
      // Use branches from JWT claims
      setBranches(authBranches);
      
      // Auto-select first branch if not already selected
      if (!selectedBranchId && authBranches.length > 0) {
        setSelectedBranch(authBranches[0]);
      }
    } else if (user?.tenantId) {
      // Fallback to user's tenantId if no branches in JWT
      setBranches([user.tenantId]);
      
      // Auto-select if not already selected
      if (!selectedBranchId) {
        setSelectedBranch(user.tenantId);
      }
    }
  }, [user, authBranches, selectedBranchId, setSelectedBranch]);

  // If no branches available, don't show selector
  if (!branches || branches.length === 0) {
    return null;
  }

  // If only one branch, show it as read-only
  if (branches.length === 1) {
    return (
      <div className="flex items-center gap-2 text-sm text-neutral-600">
        <span>Branch:</span>
        <span className="font-medium">Current Branch</span>
      </div>
    );
  }

  // Multiple branches - show dropdown
  return (
    <div className="flex items-center gap-2">
      <span className="text-sm text-neutral-600">Branch:</span>
      <Select
        value={selectedBranchId || getSelectedBranch()}
        onValueChange={setSelectedBranch}
      >
        <SelectTrigger className="w-[200px]">
          <SelectValue placeholder="Select branch" />
        </SelectTrigger>
        <SelectContent>
          {branches.map((branchId) => (
            <SelectItem key={branchId} value={branchId}>
              Branch {branchId.substring(0, 8)}...
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}

