'use client';

import { useState } from 'react';
import { useTimeOffsByDoctor, useCreateTimeOff, useDeleteTimeOff } from '@/hooks/useTimeOffs';
import { useDoctors } from '@/hooks/useDoctors';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { toast } from '@/lib/toast';
import { Plus, Trash2 } from 'lucide-react';
import { useForm } from 'react-hook-form';

export default function TimeOffsPage() {
  const [formOpen, setFormOpen] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState('');

  const { data: doctorsData } = useDoctors({ page: 0, size: 100 });
  const { data: timeOffs, isLoading } = useTimeOffsByDoctor(selectedDoctor);
  const createMutation = useCreateTimeOff();
  const deleteMutation = useDeleteTimeOff();

  const doctors = doctorsData?.content || [];

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    defaultValues: {
      doctorId: '',
      startDate: '',
      endDate: '',
      reason: '',
      allDay: true,
    },
  });

  const handleFormSubmit = async (data) => {
    if (!selectedDoctor) {
      toast.error('Please select a doctor');
      return;
    }

    try {
      await createMutation.mutateAsync({
        ...data,
        doctorId: selectedDoctor,
      });
      toast.success('Time-off created successfully');
      reset();
      setFormOpen(false);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create time-off');
    }
  };

  const handleDelete = async (timeOffId) => {
    if (!confirm('Are you sure you want to delete this time-off?')) {
      return;
    }

    try {
      await deleteMutation.mutateAsync(timeOffId);
      toast.success('Time-off deleted successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete time-off');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-neutral-900">Time Offs</h1>
          <p className="text-sm text-neutral-600 mt-1">Manage doctor time-off periods</p>
        </div>
        <Button onClick={() => setFormOpen(true)}>
          <Plus className="w-4 h-4 mr-2" />
          Add Time Off
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-4">
            <Select value={selectedDoctor} onValueChange={setSelectedDoctor}>
              <SelectTrigger className="w-[300px]">
                <SelectValue placeholder="Select a doctor to view time-offs" />
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
              Select a doctor to view their time-offs
            </div>
          ) : isLoading ? (
            <div className="text-center py-8 text-neutral-500">Loading time-offs...</div>
          ) : !timeOffs || timeOffs.length === 0 ? (
            <div className="text-center py-8 text-neutral-500">
              No time-offs found for this doctor
            </div>
          ) : (
            <div className="space-y-2">
              {timeOffs.map((timeOff) => (
                <div
                  key={timeOff.id}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-neutral-50"
                >
                  <div>
                    <p className="font-medium">
                      {new Date(timeOff.startDate).toLocaleDateString()} - {new Date(timeOff.endDate).toLocaleDateString()}
                    </p>
                    {timeOff.reason && (
                      <p className="text-sm text-neutral-600">{timeOff.reason}</p>
                    )}
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDelete(timeOff.id)}
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

      <Dialog open={formOpen} onOpenChange={setFormOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add Time Off</DialogTitle>
            <DialogDescription>Create a time-off period for a doctor</DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
            <div>
              <Label htmlFor="doctorId">Doctor *</Label>
              <Select value={selectedDoctor} onValueChange={setSelectedDoctor}>
                <SelectTrigger id="doctorId">
                  <SelectValue placeholder="Select a doctor" />
                </SelectTrigger>
                <SelectContent>
                  {doctors.map((doctor) => (
                    <SelectItem key={doctor.id} value={doctor.id}>
                      {doctor.firstName} {doctor.lastName} - {doctor.specialization}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {!selectedDoctor && (
                <p className="text-sm text-red-600 mt-1">Please select a doctor</p>
              )}
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="startDate">Start Date *</Label>
                <Input
                  id="startDate"
                  type="date"
                  {...register('startDate', { required: 'Start date is required' })}
                />
                {errors.startDate && (
                  <p className="text-sm text-red-600 mt-1">{errors.startDate.message}</p>
                )}
              </div>
              <div>
                <Label htmlFor="endDate">End Date *</Label>
                <Input
                  id="endDate"
                  type="date"
                  {...register('endDate', { required: 'End date is required' })}
                />
                {errors.endDate && (
                  <p className="text-sm text-red-600 mt-1">{errors.endDate.message}</p>
                )}
              </div>
            </div>
            <div>
              <Label htmlFor="reason">Reason</Label>
              <Input
                id="reason"
                {...register('reason')}
                placeholder="Vacation, sick leave, etc."
              />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setFormOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={createMutation.isPending || !selectedDoctor}>
                {createMutation.isPending ? 'Creating...' : 'Create'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
}
