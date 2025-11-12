'use client';

import { useState } from 'react';
import { useSessionTypes, useCreateSessionType, useUpdateSessionType, useDeleteSessionType } from '@/hooks/useSessionTypes';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { toast } from '@/lib/toast';
import { Search, Plus, Edit, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';

export default function SessionTypesPage() {
  const [formOpen, setFormOpen] = useState(false);
  const [selectedSessionType, setSelectedSessionType] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');

  const { data: sessionTypes, isLoading, error } = useSessionTypes();
  const createMutation = useCreateSessionType();
  const updateMutation = useUpdateSessionType();
  const deleteMutation = useDeleteSessionType();

  const filteredTypes = sessionTypes?.filter(st => 
    !searchQuery || 
    st.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    st.description?.toLowerCase().includes(searchQuery.toLowerCase())
  ) || [];

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    defaultValues: selectedSessionType || {
      name: '',
      description: '',
      durationMinutes: '',
      price: '',
    },
  });

  const handleFormSubmit = async (data) => {
    try {
      if (selectedSessionType) {
        await updateMutation.mutateAsync({ id: selectedSessionType.id, data });
        toast.success('Session type updated successfully');
      } else {
        await createMutation.mutateAsync(data);
        toast.success('Session type created successfully');
      }
      reset();
      setFormOpen(false);
      setSelectedSessionType(null);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to save session type');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this session type?')) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(id);
      toast.success('Session type deleted successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete session type');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-neutral-900">Session Types</h1>
          <p className="text-sm text-neutral-600 mt-1">Manage session types and offerings</p>
        </div>
        <Button onClick={() => {
          setSelectedSessionType(null);
          reset({ name: '', description: '', durationMinutes: '', price: '' });
          setFormOpen(true);
        }}>
          <Plus className="w-4 h-4 mr-2" />
          Add Session Type
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <div className="flex-1">
              <Input
                placeholder="Search session types..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="max-w-md"
              />
            </div>
            <Search className="w-5 h-5 text-neutral-400" />
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-8 text-neutral-500">Loading session types...</div>
          ) : error ? (
            <div className="text-center py-8 text-red-600">
              Error loading session types: {error.message}
            </div>
          ) : filteredTypes.length === 0 ? (
            <div className="text-center py-8 text-neutral-500">
              No session types found.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredTypes.map((sessionType) => (
                <Card key={sessionType.id}>
                  <CardContent className="p-4">
                    <div className="flex items-start justify-between mb-2">
                      <h3 className="font-semibold text-lg">{sessionType.name}</h3>
                      <div className="flex gap-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedSessionType(sessionType);
                            reset(sessionType);
                            setFormOpen(true);
                          }}
                        >
                          <Edit className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(sessionType.id)}
                          className="text-red-600 hover:text-red-700"
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                    {sessionType.description && (
                      <p className="text-sm text-neutral-600 mb-2">{sessionType.description}</p>
                    )}
                    <div className="flex gap-4 text-sm">
                      {sessionType.durationMinutes && (
                        <span className="text-neutral-500">
                          Duration: {sessionType.durationMinutes} min
                        </span>
                      )}
                      {sessionType.price && (
                        <span className="text-neutral-500">
                          Price: ₹{sessionType.price}
                        </span>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog open={formOpen} onOpenChange={setFormOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {selectedSessionType ? 'Edit Session Type' : 'Add Session Type'}
            </DialogTitle>
            <DialogDescription>
              {selectedSessionType ? 'Update session type details' : 'Create a new session type'}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
            <div>
              <Label htmlFor="name">Name *</Label>
              <Input
                id="name"
                {...register('name', { required: 'Name is required' })}
                placeholder="Speech Therapy"
              />
              {errors.name && (
                <p className="text-sm text-red-600 mt-1">{errors.name.message}</p>
              )}
            </div>
            <div>
              <Label htmlFor="description">Description</Label>
              <textarea
                id="description"
                {...register('description')}
                rows={3}
                className="w-full px-3 py-2 border rounded-md"
                placeholder="Session description..."
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="durationMinutes">Duration (minutes) *</Label>
                <Input
                  id="durationMinutes"
                  type="number"
                  {...register('durationMinutes', { 
                    required: 'Duration is required',
                    min: { value: 1, message: 'Duration must be at least 1 minute' }
                  })}
                  placeholder="60"
                />
                {errors.durationMinutes && (
                  <p className="text-sm text-red-600 mt-1">{errors.durationMinutes.message}</p>
                )}
              </div>
              <div>
                <Label htmlFor="price">Price (₹) *</Label>
                <Input
                  id="price"
                  type="number"
                  step="0.01"
                  {...register('price', { 
                    required: 'Price is required',
                    min: { value: 0, message: 'Price must be 0 or more' }
                  })}
                  placeholder="1500.00"
                />
                {errors.price && (
                  <p className="text-sm text-red-600 mt-1">{errors.price.message}</p>
                )}
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setFormOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={createMutation.isPending || updateMutation.isPending}>
                {createMutation.isPending || updateMutation.isPending 
                  ? 'Saving...' 
                  : selectedSessionType ? 'Update' : 'Create'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
