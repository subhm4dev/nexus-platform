'use client';

import { use } from 'react';
import { useRouter } from 'next/navigation';
import { useDoctor, useUpdateDoctor } from '@/hooks/useDoctors';
import { DoctorForm } from '@/components/admin/DoctorForm';
import { toast } from '@/lib/toast';

export default function EditDoctorPage({ params }) {
  const router = useRouter();
  const { id } = use(params);
  const { data: doctor, isLoading } = useDoctor(id);
  const updateMutation = useUpdateDoctor();

  const handleUpdate = async (data) => {
    try {
      await updateMutation.mutateAsync({ doctorId: id, data });
      toast.success('Doctor updated successfully');
      router.push(`/admin/doctors/${id}`);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update doctor');
      throw error;
    }
  };

  if (isLoading) {
    return <div className="text-center py-8 text-neutral-500">Loading...</div>;
  }

  if (!doctor) {
    return <div className="text-center py-8 text-red-600">Doctor not found</div>;
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-neutral-900">Edit Doctor</h1>
      <DoctorForm
        open={true}
        onClose={() => router.push(`/admin/doctors/${id}`)}
        doctor={doctor}
        onSubmit={handleUpdate}
        isLoading={updateMutation.isPending}
      />
    </div>
  );
}

