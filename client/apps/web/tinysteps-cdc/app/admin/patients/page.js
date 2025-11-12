'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { usePatients, useCreatePatient, useUpdatePatient, useDeletePatient, useTransferPatient, useAddPatientToBranch } from '@/hooks/usePatients';
import { PatientForm } from '@/components/admin/PatientForm';
import { TransferModal } from '@/components/admin/TransferModal';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { toast } from '@/lib/toast';
import { Search, Plus, Edit, Trash2, ArrowRightLeft, Eye } from 'lucide-react';

export default function PatientsPage() {
  const router = useRouter();
  const [searchQuery, setSearchQuery] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [transferModalOpen, setTransferModalOpen] = useState(false);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [transferPatient, setTransferPatient] = useState(null);

  const { data: patientsData, isLoading, error } = usePatients({
    query: searchQuery || undefined,
    page: 0,
    size: 50,
  });

  const createMutation = useCreatePatient();
  const updateMutation = useUpdatePatient();
  const deleteMutation = useDeletePatient();
  const transferMutation = useTransferPatient();
  const addToBranchMutation = useAddPatientToBranch();

  const patients = patientsData?.content || [];

  const handleCreate = async (data) => {
    try {
      await createMutation.mutateAsync(data);
      toast.success('Patient created successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create patient');
      throw error;
    }
  };

  const handleUpdate = async (data) => {
    try {
      await updateMutation.mutateAsync({ patientId: selectedPatient.id, data });
      toast.success('Patient updated successfully');
      setSelectedPatient(null);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update patient');
      throw error;
    }
  };

  const handleDelete = async (patientId) => {
    if (!confirm('Are you sure you want to delete this patient?')) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(patientId);
      toast.success('Patient deleted successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete patient');
    }
  };

  const handleTransfer = async (targetTenantId) => {
    try {
      await transferMutation.mutateAsync({ 
        patientId: transferPatient.id, 
        targetTenantId 
      });
      toast.success('Patient transferred successfully');
      setTransferPatient(null);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to transfer patient');
      throw error;
    }
  };

  const handleAddToBranch = async (targetTenantId) => {
    try {
      await addToBranchMutation.mutateAsync({ 
        patientId: transferPatient.id, 
        targetTenantId 
      });
      toast.success('Patient added to branch successfully');
      setTransferPatient(null);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add patient to branch');
      throw error;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-neutral-900">Patients</h1>
          <p className="text-sm text-neutral-600 mt-1">Manage patient records and medical information</p>
        </div>
        <Button onClick={() => {
          setSelectedPatient(null);
          setFormOpen(true);
        }}>
          <Plus className="w-4 h-4 mr-2" />
          Add Patient
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <div className="flex-1">
              <Input
                placeholder="Search patients by name, email, or phone..."
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
            <div className="text-center py-8 text-neutral-500">Loading patients...</div>
          ) : error ? (
            <div className="text-center py-8 text-red-600">
              Error loading patients: {error.message}
            </div>
          ) : patients.length === 0 ? (
            <div className="text-center py-8 text-neutral-500">
              No patients found. Create your first patient to get started.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="text-left p-3 font-semibold">Name</th>
                    <th className="text-left p-3 font-semibold">Email</th>
                    <th className="text-left p-3 font-semibold">Phone</th>
                    <th className="text-left p-3 font-semibold">Date of Birth</th>
                    <th className="text-left p-3 font-semibold">Gender</th>
                    <th className="text-right p-3 font-semibold">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {patients.map((patient) => (
                    <tr key={patient.id} className="border-b hover:bg-neutral-50">
                      <td className="p-3">
                        {patient.firstName} {patient.lastName}
                      </td>
                      <td className="p-3">{patient.email || '-'}</td>
                      <td className="p-3">{patient.phone}</td>
                      <td className="p-3">
                        {patient.dateOfBirth ? new Date(patient.dateOfBirth).toLocaleDateString() : '-'}
                      </td>
                      <td className="p-3">{patient.gender || '-'}</td>
                      <td className="p-3">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => router.push(`/admin/patients/${patient.id}`)}
                          >
                            <Eye className="w-4 h-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedPatient(patient);
                              setFormOpen(true);
                            }}
                          >
                            <Edit className="w-4 h-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setTransferPatient(patient);
                              setTransferModalOpen(true);
                            }}
                          >
                            <ArrowRightLeft className="w-4 h-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDelete(patient.id)}
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

      <PatientForm
        open={formOpen}
        onClose={() => {
          setFormOpen(false);
          setSelectedPatient(null);
        }}
        patient={selectedPatient}
        onSubmit={selectedPatient ? handleUpdate : handleCreate}
        isLoading={createMutation.isPending || updateMutation.isPending}
      />

      <TransferModal
        open={transferModalOpen}
        onClose={() => {
          setTransferModalOpen(false);
          setTransferPatient(null);
        }}
        title="Transfer Patient"
        description="Move patient to another branch or add to multiple branches"
        onTransfer={handleTransfer}
        onAddToBranch={handleAddToBranch}
        isLoading={transferMutation.isPending || addToBranchMutation.isPending}
      />
    </div>
  );
}
