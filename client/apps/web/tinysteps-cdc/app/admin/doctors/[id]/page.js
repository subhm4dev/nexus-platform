'use client';

import { use } from 'react';
import { useRouter } from 'next/navigation';
import { useDoctor, useVerifyDoctor } from '@/hooks/useDoctors';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { toast } from '@/lib/toast';
import { ArrowLeft, CheckCircle, XCircle, Edit } from 'lucide-react';

export default function DoctorDetailPage({ params }) {
  const router = useRouter();
  const { id } = use(params);
  const { data: doctor, isLoading, error } = useDoctor(id);
  const verifyMutation = useVerifyDoctor();

  const handleVerify = async (status) => {
    try {
      await verifyMutation.mutateAsync({ doctorId: id, verificationStatus: status });
      toast.success(`Doctor ${status.toLowerCase()} successfully`);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update verification status');
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="text-center py-8 text-neutral-500">Loading doctor details...</div>
      </div>
    );
  }

  if (error || !doctor) {
    return (
      <div className="space-y-6">
        <div className="text-center py-8 text-red-600">
          Error loading doctor: {error?.message || 'Doctor not found'}
        </div>
        <Button onClick={() => router.push('/admin/doctors')}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Doctors
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Button variant="ghost" onClick={() => router.push('/admin/doctors')}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Doctors
        </Button>
        <div className="flex gap-2">
          {doctor.verificationStatus !== 'VERIFIED' && (
            <Button
              onClick={() => handleVerify('VERIFIED')}
              disabled={verifyMutation.isPending}
            >
              <CheckCircle className="w-4 h-4 mr-2" />
              Verify Doctor
            </Button>
          )}
          <Button variant="outline" onClick={() => router.push(`/admin/doctors/${id}/edit`)}>
            <Edit className="w-4 h-4 mr-2" />
            Edit
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="text-2xl">
              {doctor.firstName} {doctor.lastName}
            </CardTitle>
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
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-neutral-600">Email</p>
              <p className="font-medium">{doctor.email}</p>
            </div>
            <div>
              <p className="text-sm text-neutral-600">Phone</p>
              <p className="font-medium">{doctor.phone}</p>
            </div>
            <div>
              <p className="text-sm text-neutral-600">Specialization</p>
              <p className="font-medium">{doctor.specialization}</p>
            </div>
            {doctor.qualification && (
              <div>
                <p className="text-sm text-neutral-600">Qualification</p>
                <p className="font-medium">{doctor.qualification}</p>
              </div>
            )}
            {doctor.experienceYears && (
              <div>
                <p className="text-sm text-neutral-600">Experience</p>
                <p className="font-medium">{doctor.experienceYears} years</p>
              </div>
            )}
          </div>
          {doctor.bio && (
            <div>
              <p className="text-sm text-neutral-600 mb-2">Bio</p>
              <p className="text-neutral-900">{doctor.bio}</p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

