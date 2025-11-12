'use client';

import { use } from 'react';
import { useRouter } from 'next/navigation';
import { usePatient, useUpdatePatient } from '@/hooks/usePatients';
import { PatientForm } from '@/components/admin/PatientForm';
import { toast } from '@/lib/toast';

export default function EditPatientPage({ params }) {
  const router = useRouter();
  const { id } = use(params);
  const { data: patient, isLoading } = usePatient(id);
  const updateMutation = useUpdatePatient();

  const handleUpdate = async (data) => {
    try {
      await updateMutation.mutateAsync({ patientId: id, data });
      toast.success('Patient updated successfully');
      router.push(`/admin/patients/${id}`);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update patient');
      throw error;
    }
  };

  if (isLoading) {
    return <div className="text-center py-8 text-neutral-500">Loading...</div>;
  }

  if (!patient) {
    return <div className="text-center py-8 text-red-600">Patient not found</div>;
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-neutral-900">Edit Patient</h1>
      <PatientForm
        open={true}
        onClose={() => router.push(`/admin/patients/${id}`)}
        patient={patient}
        onSubmit={handleUpdate}
        isLoading={updateMutation.isPending}
      />
    </div>
  );
}

