'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useAuthStore } from '@/stores/auth-store';

export function TransferModal({ 
  open, 
  onClose, 
  title, 
  description,
  onTransfer,
  onAddToBranch,
  isLoading 
}) {
  const { branches } = useAuthStore();
  const [selectedBranch, setSelectedBranch] = useState('');
  const [transferType, setTransferType] = useState('transfer'); // 'transfer' or 'add'

  const handleSubmit = async () => {
    if (!selectedBranch) {
      return;
    }

    try {
      if (transferType === 'transfer') {
        await onTransfer(selectedBranch);
      } else {
        await onAddToBranch(selectedBranch);
      }
      setSelectedBranch('');
      setTransferType('transfer');
      onClose();
    } catch (error) {
      // Error handled by parent
    }
  };

  const currentTenantId = useAuthStore.getState().user?.tenantId;
  const availableBranches = branches?.filter(b => b !== currentTenantId) || [];

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        <div className="space-y-4 py-4">
          <div>
            <Label>Operation Type</Label>
            <div className="flex gap-4 mt-2">
              <label className="flex items-center gap-2">
                <input
                  type="radio"
                  value="transfer"
                  checked={transferType === 'transfer'}
                  onChange={(e) => setTransferType(e.target.value)}
                  className="w-4 h-4"
                />
                <span>Transfer (move to new branch)</span>
              </label>
              <label className="flex items-center gap-2">
                <input
                  type="radio"
                  value="add"
                  checked={transferType === 'add'}
                  onChange={(e) => setTransferType(e.target.value)}
                  className="w-4 h-4"
                />
                <span>Add to Branch (keep at current)</span>
              </label>
            </div>
          </div>

          <div>
            <Label htmlFor="branch">Select Target Branch *</Label>
            <Select value={selectedBranch} onValueChange={setSelectedBranch}>
              <SelectTrigger id="branch">
                <SelectValue placeholder="Select a branch" />
              </SelectTrigger>
              <SelectContent>
                {availableBranches.length === 0 ? (
                  <SelectItem value="" disabled>No other branches available</SelectItem>
                ) : (
                  availableBranches.map((branchId) => (
                    <SelectItem key={branchId} value={branchId}>
                      {branchId}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
          </div>

          {transferType === 'transfer' && (
            <div className="bg-yellow-50 border border-yellow-200 rounded-md p-3 text-sm text-yellow-800">
              <strong>Warning:</strong> This will move the record to the new branch. The current record will be soft deleted.
            </div>
          )}

          {transferType === 'add' && (
            <div className="bg-blue-50 border border-blue-200 rounded-md p-3 text-sm text-blue-800">
              <strong>Info:</strong> This will create a copy at the new branch while keeping the current record active.
            </div>
          )}
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button 
            onClick={handleSubmit} 
            disabled={isLoading || !selectedBranch || availableBranches.length === 0}
          >
            {isLoading ? 'Processing...' : transferType === 'transfer' ? 'Transfer' : 'Add to Branch'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

