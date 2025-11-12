'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useDoctors, useCreateDoctor, useUpdateDoctor, useDeleteDoctor, useTransferDoctor, useAddDoctorToBranch } from '@/hooks/useDoctors';
import { DoctorForm } from '@/components/admin/DoctorForm';
import { TransferModal } from '@/components/admin/TransferModal';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from '@/lib/toast';
import { Search, Plus, Edit, Trash2, ArrowRightLeft, Eye, CheckCircle, XCircle } from 'lucide-react';
import { useBranchStore } from '@/stores/branch-store';

export default function DoctorsPage() {
  const router = useRouter();
  const { getSelectedBranch } = useBranchStore();
  const [searchQuery, setSearchQuery] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [transferModalOpen, setTransferModalOpen] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState(null);
  const [transferDoctor, setTransferDoctor] = useState(null);

  const { data: doctorsData, isLoading, error } = useDoctors({
    query: searchQuery || undefined,
    page: 0,
    size: 50,
  });

  const createMutation = useCreateDoctor();
  const updateMutation = useUpdateDoctor();
  const deleteMutation = useDeleteDoctor();
  const transferMutation = useTransferDoctor();
  const addToBranchMutation = useAddDoctorToBranch();

  const doctors = doctorsData?.content || [];

  const handleCreate = async (data) => {
    try {
      await createMutation.mutateAsync(data);
      toast.success('Doctor created successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create doctor');
      throw error;
    }
  };

  const handleUpdate = async (data) => {
    try {
      await updateMutation.mutateAsync({ doctorId: selectedDoctor.id, data });
      toast.success('Doctor updated successfully');
      setSelectedDoctor(null);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update doctor');
      throw error;
    }
  };

  const handleDelete = async (doctorId) => {
    if (!confirm('Are you sure you want to delete this doctor?')) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(doctorId);
      toast.success('Doctor deleted successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete doctor');
    }
  };

  const handleTransfer = async (targetTenantId) => {
    try {
      await transferMutation.mutateAsync({ 
        doctorId: transferDoctor.id, 
        targetTenantId 
      });
      toast.success('Doctor transferred successfully');
      setTransferDoctor(null);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to transfer doctor');
      throw error;
    }
  };

  const handleAddToBranch = async (targetTenantId) => {
    try {
      await addToBranchMutation.mutateAsync({ 
        doctorId: transferDoctor.id, 
        targetTenantId 
      });
      toast.success('Doctor added to branch successfully');
      setTransferDoctor(null);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add doctor to branch');
      throw error;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-neutral-900">Doctors</h1>
          <p className="text-sm text-neutral-600 mt-1">Manage doctor profiles and information</p>
        </div>
        <Button onClick={() => {
          setSelectedDoctor(null);
          setFormOpen(true);
        }}>
          <Plus className="w-4 h-4 mr-2" />
          Add Doctor
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <div className="flex-1">
              <Input
                placeholder="Search doctors by name, email, or specialization..."
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
            <div className="text-center py-8 text-neutral-500">Loading doctors...</div>
          ) : error ? (
            <div className="text-center py-8 text-red-600">
              Error loading doctors: {error.message}
            </div>
          ) : doctors.length === 0 ? (
            <div className="text-center py-8 text-neutral-500">
              No doctors found. Create your first doctor to get started.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="text-left p-3 font-semibold">Name</th>
                    <th className="text-left p-3 font-semibold">Email</th>
                    <th className="text-left p-3 font-semibold">Phone</th>
                    <th className="text-left p-3 font-semibold">Specialization</th>
                    <th className="text-left p-3 font-semibold">Status</th>
                    <th className="text-right p-3 font-semibold">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {doctors.map((doctor) => (
                    <tr key={doctor.id} className="border-b hover:bg-neutral-50">
                      <td className="p-3">
                        {doctor.firstName} {doctor.lastName}
                      </td>
                      <td className="p-3">{doctor.email}</td>
                      <td className="p-3">{doctor.phone}</td>
                      <td className="p-3">{doctor.specialization}</td>
                      <td className="p-3">
                        {doctor.verificationStatus === 'VERIFIED' ? (
                          <Badge variant="default" className="bg-green-600">
                            <CheckCircle className="w-3 h-3 mr-1" />
                            Verified
                          </Badge>
                        ) : (
                          <Badge variant="outline">
                            <XCircle className="w-3 h-3 mr-1" />
                            {doctor.verificationStatus || 'Pending'}
                          </Badge>
                        )}
                      </td>
                      <td className="p-3">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => router.push(`/admin/doctors/${doctor.id}`)}
                          >
                            <Eye className="w-4 h-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedDoctor(doctor);
                              setFormOpen(true);
                            }}
                          >
                            <Edit className="w-4 h-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setTransferDoctor(doctor);
                              setTransferModalOpen(true);
                            }}
                          >
                            <ArrowRightLeft className="w-4 h-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDelete(doctor.id)}
                            className="text-red-600 hover:text-red-700"
                          >
                            <Trash2 className="w-4 h-4" />
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      <DoctorForm
        open={formOpen}
        onClose={() => {
          setFormOpen(false);
          setSelectedDoctor(null);
        }}
        doctor={selectedDoctor}
        onSubmit={selectedDoctor ? handleUpdate : handleCreate}
        isLoading={createMutation.isPending || updateMutation.isPending}
      />

      <TransferModal
        open={transferModalOpen}
        onClose={() => {
          setTransferModalOpen(false);
          setTransferDoctor(null);
        }}
        title="Transfer Doctor"
        description="Move doctor to another branch or add to multiple branches"
        onTransfer={handleTransfer}
        onAddToBranch={handleAddToBranch}
        isLoading={transferMutation.isPending || addToBranchMutation.isPending}
      />
    </div>
  );
}
