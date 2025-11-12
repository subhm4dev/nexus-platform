'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useCreateAdminBooking, useCancelAppointment, useCompleteAppointment } from '@/hooks/useAppointments';
import { useDoctors } from '@/hooks/useDoctors';
import { AppointmentForm } from '@/components/admin/AppointmentForm';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { toast } from '@/lib/toast';
import { Plus, Calendar, List, X, Check } from 'lucide-react';

export default function AppointmentsPage() {
  const router = useRouter();
  const [formOpen, setFormOpen] = useState(false);
  const [viewMode, setViewMode] = useState('calendar');
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);

  const createMutation = useCreateAdminBooking();
  const cancelMutation = useCancelAppointment();
  const completeMutation = useCompleteAppointment();

  const handleCreate = async (data, sendPaymentLink) => {
    try {
      await createMutation.mutateAsync({ data, sendPaymentLink });
      toast.success('Appointment created successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create appointment');
      throw error;
    }
  };

  const handleCancel = async (appointmentId) => {
    if (!confirm('Are you sure you want to cancel this appointment?')) {
      return;
    }

    try {
      await cancelMutation.mutateAsync({ appointmentId, reason: 'Cancelled by admin' });
      toast.success('Appointment cancelled successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to cancel appointment');
    }
  };

  const handleComplete = async (appointmentId) => {
    try {
      await completeMutation.mutateAsync(appointmentId);
      toast.success('Appointment marked as completed');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to complete appointment');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-neutral-900">Appointments</h1>
          <p className="text-sm text-neutral-600 mt-1">Manage appointments and schedules</p>
        </div>
        <Button onClick={() => setFormOpen(true)}>
          <Plus className="w-4 h-4 mr-2" />
          Create Appointment
        </Button>
      </div>

      <Tabs value={viewMode} onValueChange={setViewMode}>
        <TabsList>
          <TabsTrigger value="calendar">
            <Calendar className="w-4 h-4 mr-2" />
            Calendar
          </TabsTrigger>
          <TabsTrigger value="list">
            <List className="w-4 h-4 mr-2" />
            List
          </TabsTrigger>
        </TabsList>

        <TabsContent value="calendar" className="space-y-4">
          <Card>
            <CardHeader>
              <div className="flex items-center gap-4">
                <Input
                  type="date"
                  value={selectedDate}
                  onChange={(e) => setSelectedDate(e.target.value)}
                  className="max-w-xs"
                />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-center py-8 text-neutral-500">
                Calendar view will be displayed here
                <br />
                <small>Selected date: {selectedDate}</small>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="list" className="space-y-4">
          <Card>
            <CardContent className="p-6">
              <div className="text-center py-8 text-neutral-500">
                Appointment list will be displayed here
                <br />
                <small>Filter by date, doctor, patient, or status</small>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <AppointmentForm
        open={formOpen}
        onClose={() => setFormOpen(false)}
        onSubmit={handleCreate}
        isLoading={createMutation.isPending}
      />
    </div>
  );
}
