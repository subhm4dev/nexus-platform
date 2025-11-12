'use client';

import { use } from 'react';
import { useRouter } from 'next/navigation';
import { usePatient } from '@/hooks/usePatients';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { ArrowLeft, Edit } from 'lucide-react';

export default function PatientDetailPage({ params }) {
  const router = useRouter();
  const { id } = use(params);
  const { data: patient, isLoading, error } = usePatient(id);

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="text-center py-8 text-neutral-500">Loading patient details...</div>
      </div>
    );
  }

  if (error || !patient) {
    return (
      <div className="space-y-6">
        <div className="text-center py-8 text-red-600">
          Error loading patient: {error?.message || 'Patient not found'}
        </div>
        <Button onClick={() => router.push('/admin/patients')}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Patients
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Button variant="ghost" onClick={() => router.push('/admin/patients')}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Patients
        </Button>
        <Button variant="outline" onClick={() => router.push(`/admin/patients/${id}/edit`)}>
          <Edit className="w-4 h-4 mr-2" />
          Edit
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-2xl">
            {patient.firstName} {patient.lastName}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-neutral-600">Email</p>
              <p className="font-medium">{patient.email || '-'}</p>
            </div>
            <div>
              <p className="text-sm text-neutral-600">Phone</p>
              <p className="font-medium">{patient.phone}</p>
            </div>
            <div>
              <p className="text-sm text-neutral-600">Date of Birth</p>
              <p className="font-medium">
                {patient.dateOfBirth ? new Date(patient.dateOfBirth).toLocaleDateString() : '-'}
              </p>
            </div>
            <div>
              <p className="text-sm text-neutral-600">Gender</p>
              <p className="font-medium">{patient.gender || '-'}</p>
            </div>
            {patient.address && (
              <div className="col-span-2">
                <p className="text-sm text-neutral-600">Address</p>
                <p className="font-medium">{patient.address}</p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

