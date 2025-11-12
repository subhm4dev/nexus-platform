'use client';

import { useState } from 'react';
import { Plus, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { cn } from '@/lib/utils';

/**
 * Simple ComboboxWithAdd Component
 * 
 * A select dropdown with the ability to add new items.
 * For qualifications - just works with name strings, not complex objects.
 * 
 * @param {Object} props
 * @param {Array<string>} props.options - List of option strings (e.g., ["MBBS", "MD", "PhD"])
 * @param {string} props.value - Selected value (the string itself)
 * @param {Function} props.onChange - Callback when selection changes (receives the string value)
 * @param {Function} props.onCreate - Callback to create new item (receives the name string, returns the name)
 * @param {string} props.placeholder - Placeholder text
 * @param {string} props.label - Field label
 * @param {boolean} props.isLoading - Loading state
 * @param {boolean} props.isCreating - Creating state
 * @param {string} props.error - Error message
 * @param {string} props.fieldName - Field name for display (e.g., "Qualification")
 */
export function ComboboxWithAdd({
  options = [],
  value,
  onChange,
  onCreate,
  placeholder = 'Select or add new...',
  label,
  isLoading = false,
  isCreating = false,
  error,
  fieldName = 'Item',
}) {
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [newItemName, setNewItemName] = useState('');

  const handleCreate = async () => {
    if (!newItemName.trim()) return;
    
    try {
      // For simple string-based items, just pass the name
      const newName = await onCreate(newItemName.trim());
      
      // If onCreate returns a name, use it; otherwise use what was entered
      onChange(newName || newItemName.trim());
      
      setNewItemName('');
      setIsAddDialogOpen(false);
    } catch (error) {
      console.error(`Failed to create ${fieldName}:`, error);
      // Error handling is typically done by the mutation hook in the parent
    }
  };

  return (
    <div className="space-y-2">
      {label && (
        <Label className="text-sm font-medium text-neutral-700">
          {label}
        </Label>
      )}
      
      <div className="flex gap-2">
        <Select
          value={value || ''}
          onValueChange={onChange}
          disabled={isLoading || isCreating}
        >
          <SelectTrigger className={cn('flex-1', error && 'border-red-500')}>
            <SelectValue placeholder={isLoading ? 'Loading...' : placeholder}>
              {value || placeholder}
            </SelectValue>
          </SelectTrigger>
          <SelectContent>
            {isLoading ? (
              <div className="flex items-center justify-center p-4">
                <Loader2 className="h-4 w-4 animate-spin mr-2" />
                Loading...
              </div>
            ) : options.length === 0 ? (
              <div className="p-4 text-sm text-neutral-500 text-center">
                No {fieldName.toLowerCase()} found. Click the + button to add one.
              </div>
            ) : (
              options.map((option) => (
                <SelectItem key={option} value={option}>
                  {option}
                </SelectItem>
              ))
            )}
          </SelectContent>
        </Select>
        
        <Button
          type="button"
          variant="outline"
          size="default"
          onClick={() => setIsAddDialogOpen(true)}
          disabled={isLoading || isCreating}
          className="px-3"
        >
          <Plus className="h-4 w-4" />
        </Button>
      </div>

      {error && (
        <p className="text-sm text-red-600">{error}</p>
      )}

      <Dialog open={isAddDialogOpen} onOpenChange={setIsAddDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add New {fieldName}</DialogTitle>
            <DialogDescription>
              Enter a new {fieldName.toLowerCase()} name. It will be saved when you create the doctor.
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-4 py-4">
            <div>
              <Label htmlFor="new-item-name">
                {fieldName} Name *
              </Label>
              <Input
                id="new-item-name"
                value={newItemName}
                onChange={(e) => setNewItemName(e.target.value)}
                placeholder={`Enter ${fieldName.toLowerCase()} name (e.g., MBBS, MD)`}
                disabled={isCreating}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && newItemName.trim()) {
                    handleCreate();
                  }
                }}
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => {
                setIsAddDialogOpen(false);
                setNewItemName('');
              }}
              disabled={isCreating}
            >
              Cancel
            </Button>
            <Button
              type="button"
              onClick={handleCreate}
              disabled={!newItemName.trim() || isCreating}
            >
              {isCreating ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Adding...
                </>
              ) : (
                `Add ${fieldName}`
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

