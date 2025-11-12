'use client';

import { useState } from 'react';
import { useSpecializations, useCreateSpecialization, useUpdateSpecialization, useDeleteSpecialization } from '@/hooks/useSpecializations';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { toast } from '@/lib/toast';
import { Heart, Plus, Search, Edit, Trash2, Loader2 } from 'lucide-react';

export default function DoctorSpecializationsPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [selectedSpecialization, setSelectedSpecialization] = useState(null);

  const { data: specializations = [], isLoading, error } = useSpecializations();
  const createMutation = useCreateSpecialization();
  const updateMutation = useUpdateSpecialization();
  const deleteMutation = useDeleteSpecialization();

  // Filter specializations based on search query
  const filteredSpecializations = specializations.filter((spec) =>
    spec.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
    spec.description?.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleCreate = async (data) => {
    try {
      await createMutation.mutateAsync(data);
      toast.success('Specialization created successfully');
      setFormOpen(false);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create specialization');
      throw error;
    }
  };

  const handleUpdate = async (data) => {
    try {
      await updateMutation.mutateAsync({ id: selectedSpecialization.id, data });
      toast.success('Specialization updated successfully');
      setSelectedSpecialization(null);
      setFormOpen(false);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update specialization');
      throw error;
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this specialization?')) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(id);
      toast.success('Specialization deleted successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete specialization');
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="text-center py-8">
          <Loader2 className="w-8 h-8 animate-spin mx-auto text-blue-600" />
          <p className="text-neutral-500 mt-2">Loading specializations...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="space-y-6">
        <Card>
          <CardContent className="p-6">
            <div className="text-center py-8 text-red-600">
              <p className="font-semibold">Error loading specializations</p>
              <p className="text-sm mt-2">{error.message}</p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-4">
        <span>Admin</span>
        <span className="text-gray-400">/</span>
        <span>Doctors</span>
        <span className="text-gray-400">/</span>
        <span className="text-gray-900 font-medium">Specializations</span>
      </nav>

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
            <Heart className="w-8 h-8 text-red-600" />
            Doctor Specializations
          </h1>
          <p className="text-gray-600 mt-1">
            Manage medical specializations (master data)
          </p>
        </div>
        <Button onClick={() => {
          setSelectedSpecialization(null);
          setFormOpen(true);
        }}>
          <Plus className="w-4 h-4 mr-2" />
          Add Specialization
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Specializations ({filteredSpecializations.length})</CardTitle>
            <div className="flex items-center gap-2">
              <Input
                placeholder="Search specializations..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="max-w-md"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {filteredSpecializations.length === 0 ? (
            <div className="text-center py-12">
              <Heart className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">
                {searchQuery ? 'No specializations found' : 'No specializations yet'}
              </h3>
              <p className="text-gray-600 mb-4">
                {searchQuery 
                  ? 'Try a different search term'
                  : 'Create your first specialization to get started'}
              </p>
              {!searchQuery && (
                <Button onClick={() => setFormOpen(true)}>
                  <Plus className="w-4 h-4 mr-2" />
                  Add Specialization
                </Button>
              )}
            </div>
          ) : (
            <div className="space-y-2">
              {filteredSpecializations.map((spec) => (
                <div
                  key={spec.id}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-neutral-50 transition-colors"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-3">
                      <h3 className="font-semibold text-gray-900">{spec.name}</h3>
                      {spec.isActive !== false && (
                        <Badge variant="default" className="bg-green-600">
                          Active
                        </Badge>
                      )}
                    </div>
                    {spec.description && (
                      <p className="text-sm text-gray-600 mt-1">{spec.description}</p>
                    )}
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => {
                        setSelectedSpecialization(spec);
                        setFormOpen(true);
                      }}
                    >
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDelete(spec.id)}
                      className="text-red-600 hover:text-red-700"
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Simple form modal - you can enhance this later */}
      {formOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>
                {selectedSpecialization ? 'Edit Specialization' : 'Add Specialization'}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  const formData = new FormData(e.target);
                  const data = {
                    name: formData.get('name'),
                    description: formData.get('description'),
                  };
                  if (selectedSpecialization) {
                    handleUpdate(data);
                  } else {
                    handleCreate(data);
                  }
                }}
                className="space-y-4"
              >
                <div>
                  <label className="block text-sm font-medium mb-1">Name *</label>
                  <Input
                    name="name"
                    defaultValue={selectedSpecialization?.name}
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Description</label>
                  <textarea
                    name="description"
                    className="w-full border rounded-md p-2"
                    rows={3}
                    defaultValue={selectedSpecialization?.description}
                  />
                </div>
                <div className="flex gap-2 justify-end">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => {
                      setFormOpen(false);
                      setSelectedSpecialization(null);
                    }}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    disabled={createMutation.isPending || updateMutation.isPending}
                  >
                    {createMutation.isPending || updateMutation.isPending ? (
                      <Loader2 className="w-4 h-4 animate-spin mr-2" />
                    ) : null}
                    {selectedSpecialization ? 'Update' : 'Create'}
                  </Button>
                </div>
              </form>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}

