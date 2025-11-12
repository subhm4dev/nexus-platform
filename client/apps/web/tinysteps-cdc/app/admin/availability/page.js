'use client';

import { useState } from 'react';
import { useAvailabilitiesByDoctor } from '@/hooks/useAvailability';
import { useDoctors } from '@/hooks/useDoctors';
import { useDeleteAvailability } from '@/hooks/useAvailability';
import { AvailabilityBulkForm } from '@/components/admin/AvailabilityBulkForm';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { toast } from '@/lib/toast';
import { Plus, Trash2 } from 'lucide-react';

export default function AvailabilityPage() {
  const [bulkFormOpen, setBulkFormOpen] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState('');

  const { data: doctorsData } = useDoctors({ page: 0, size: 100 });
  const { data: availabilities, isLoading } = useAvailabilitiesByDoctor(selectedDoctor);
  const deleteMutation = useDeleteAvailability();

  const doctors = doctorsData?.content || [];

  const handleDelete = async (availabilityId) => {
    if (!confirm('Are you sure you want to delete this availability?')) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(availabilityId);
      toast.success('Availability deleted successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete availability');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-neutral-900">Availability</h1>
          <p className="text-sm text-neutral-600 mt-1">Manage doctor availability and time slots</p>
        </div>
        <Button onClick={() => setBulkFormOpen(true)}>
          <Plus className="w-4 h-4 mr-2" />
          Bulk Create Availability
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <Select value={selectedDoctor} onValueChange={setSelectedDoctor}>
              <SelectTrigger className="w-[300px]">
                <SelectValue placeholder="Select a doctor to view availability" />
              </SelectTrigger>
              <SelectContent>
                {doctors.map((doctor) => (
                  <SelectItem key={doctor.id} value={doctor.id}>
                    {doctor.firstName} {doctor.lastName} - {doctor.specialization}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          {!selectedDoctor ? (
            <div className="text-center py-8 text-neutral-500">
              Select a doctor to view their availability
            </div>
          ) : isLoading ? (
            <div className="text-center py-8 text-neutral-500">Loading availability...</div>
          ) : !availabilities || availabilities.length === 0 ? (
            <div className="text-center py-8 text-neutral-500">
              No availability slots found for this doctor
            </div>
          ) : (
            <div className="space-y-2">
              {availabilities.map((availability) => (
                <div
                  key={availability.id}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-neutral-50"
                >
                  <div>
                    <p className="font-medium">
                      {availability.dayOfWeek === 1 && 'Monday'}
                      {availability.dayOfWeek === 2 && 'Tuesday'}
                      {availability.dayOfWeek === 3 && 'Wednesday'}
                      {availability.dayOfWeek === 4 && 'Thursday'}
                      {availability.dayOfWeek === 5 && 'Friday'}
                      {availability.dayOfWeek === 6 && 'Saturday'}
                      {availability.dayOfWeek === 7 && 'Sunday'}
                    </p>
                    <p className="text-sm text-neutral-600">
                      {availability.startTime} - {availability.endTime} ({availability.slotDuration} min slots)
                    </p>
                    {availability.effectiveDate && (
                      <p className="text-xs text-neutral-500">
                        Effective: {new Date(availability.effectiveDate).toLocaleDateString()}
                      </p>
                    )}
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDelete(availability.id)}
                    className="text-red-600 hover:text-red-700"
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <AvailabilityBulkForm
        open={bulkFormOpen}
        onClose={() => setBulkFormOpen(false)}
      />
    </div>
  );
}
