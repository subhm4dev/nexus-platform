'use client';

import { use } from 'react';
import { useRouter } from 'next/navigation';
import { useAppointment, useCancelAppointment, useCompleteAppointment } from '@/hooks/useAppointments';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { toast } from '@/lib/toast';
import { ArrowLeft, X, Check } from 'lucide-react';

export default function AppointmentDetailPage({ params }) {
  const router = useRouter();
  const { id } = use(params);
  const { data: appointment, isLoading, error } = useAppointment(id);
  const cancelMutation = useCancelAppointment();
  const completeMutation = useCompleteAppointment();

  const handleCancel = async () => {
    if (!confirm('Are you sure you want to cancel this appointment?')) {
      return;
    }

    try {
      await cancelMutation.mutateAsync({ appointmentId: id, reason: 'Cancelled by admin' });
      toast.success('Appointment cancelled successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to cancel appointment');
    }
  };

  const handleComplete = async () => {
    try {
      await completeMutation.mutateAsync(id);
      toast.success('Appointment marked as completed');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to complete appointment');
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="text-center py-8 text-neutral-500">Loading appointment details...</div>
      </div>
    );
  }

  if (error || !appointment) {
    return (
      <div className="space-y-6">
        <div className="text-center py-8 text-red-600">
          Error loading appointment: {error?.message || 'Appointment not found'}
        </div>
        <Button onClick={() => router.push('/admin/appointments')}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Appointments
        </Button>
      </div>
    );
  }

  const statusColors = {
    SCHEDULED: 'bg-blue-600',
    COMPLETED: 'bg-green-600',
    CANCELLED: 'bg-red-600',
    PENDING: 'bg-yellow-600',
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <Button variant="ghost" onClick={() => router.push('/admin/appointments')}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Appointments
        </Button>
        <div className="flex gap-2">
          {appointment.status === 'SCHEDULED' && (
            <>
              <Button
                variant="outline"
                onClick={handleComplete}
                disabled={completeMutation.isPending}
              >
                <Check className="w-4 h-4 mr-2" />
                Mark Complete
              </Button>
              <Button
                variant="destructive"
                onClick={handleCancel}
                disabled={cancelMutation.isPending}
              >
                <X className="w-4 h-4 mr-2" />
                Cancel
              </Button>
            </>
          )}
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Appointment Details</CardTitle>
            <Badge className={statusColors[appointment.status] || 'bg-neutral-600'}>
              {appointment.status}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-neutral-600">Patient</p>
              <p className="font-medium">{appointment.patientId}</p>
            </div>
            <div>
              <p className="text-sm text-neutral-600">Doctor</p>
              <p className="font-medium">{appointment.doctorId}</p>
            </div>
            <div>
              <p className="text-sm text-neutral-600">Date</p>
              <p className="font-medium">
                {appointment.appointmentDate ? new Date(appointment.appointmentDate).toLocaleDateString() : '-'}
              </p>
            </div>
            <div>
              <p className="text-sm text-neutral-600">Time</p>
              <p className="font-medium">{appointment.startTime || '-'}</p>
            </div>
            {appointment.notes && (
              <div className="col-span-2">
                <p className="text-sm text-neutral-600">Notes</p>
                <p className="font-medium">{appointment.notes}</p>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

