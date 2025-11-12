'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useDoctors } from '@/hooks/useDoctors';
import { useCreateAvailability } from '@/hooks/useAvailability';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
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

export function AvailabilityBulkForm({ open, onClose }) {
  const [selectedDoctors, setSelectedDoctors] = useState([]);
  const [selectedDays, setSelectedDays] = useState([]);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const { data: doctorsData } = useDoctors({ page: 0, size: 100 });
  const createMutation = useCreateAvailability();

  const doctors = doctorsData?.content || [];

  const daysOfWeek = [
    { value: 1, label: 'Monday' },
    { value: 2, label: 'Tuesday' },
    { value: 3, label: 'Wednesday' },
    { value: 4, label: 'Thursday' },
    { value: 5, label: 'Friday' },
    { value: 6, label: 'Saturday' },
    { value: 7, label: 'Sunday' },
  ];

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({
    defaultValues: {
      startTime: '09:00',
      endTime: '17:00',
      slotDuration: 60,
    },
  });

  const toggleDoctor = (doctorId) => {
    setSelectedDoctors(prev =>
      prev.includes(doctorId)
        ? prev.filter(id => id !== doctorId)
        : [...prev, doctorId]
    );
  };

  const toggleDay = (day) => {
    setSelectedDays(prev =>
      prev.includes(day)
        ? prev.filter(d => d !== day)
        : [...prev, day]
    );
  };

  const handleFormSubmit = async (data) => {
    if (selectedDoctors.length === 0) {
      toast.error('Please select at least one doctor');
      return;
    }

    if (selectedDays.length === 0) {
      toast.error('Please select at least one day of the week');
      return;
    }

    if (!startDate || !endDate) {
      toast.error('Please select start and end dates');
      return;
    }

    try {
      // Create availability for each doctor, each day in the range
      const promises = [];
      
      selectedDoctors.forEach(doctorId => {
        selectedDays.forEach(day => {
          // Generate dates for this day of week between start and end date
          const start = new Date(startDate);
          const end = new Date(endDate);
          
          for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
            if (d.getDay() === (day % 7)) {
              promises.push(
                createMutation.mutateAsync({
                  doctorId,
                  dayOfWeek: day,
                  startTime: data.startTime,
                  endTime: data.endTime,
                  slotDuration: data.slotDuration,
                  effectiveDate: d.toISOString().split('T')[0],
                })
              );
            }
          }
        });
      });

      await Promise.all(promises);
      toast.success(`Created ${promises.length} availability slots`);
      reset();
      setSelectedDoctors([]);
      setSelectedDays([]);
      setStartDate('');
      setEndDate('');
      onClose();
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to create availability');
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Bulk Create Availability</DialogTitle>
          <DialogDescription>
            Create availability slots for multiple doctors across multiple days
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div>
            <Label>Select Doctors *</Label>
            <div className="border rounded-md p-4 max-h-48 overflow-y-auto mt-2">
              {doctors.length === 0 ? (
                <p className="text-sm text-neutral-500">No doctors available</p>
              ) : (
                <div className="space-y-2">
                  {doctors.map((doctor) => (
                    <label key={doctor.id} className="flex items-center gap-2 cursor-pointer">
                      <Checkbox
                        checked={selectedDoctors.includes(doctor.id)}
                        onCheckedChange={() => toggleDoctor(doctor.id)}
                      />
                      <span className="text-sm">
                        {doctor.firstName} {doctor.lastName} - {doctor.specialization}
                      </span>
                    </label>
                  ))}
                </div>
              )}
            </div>
            {selectedDoctors.length === 0 && (
              <p className="text-sm text-red-600 mt-1">Please select at least one doctor</p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="startDate">Start Date *</Label>
              <Input
                id="startDate"
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                min={new Date().toISOString().split('T')[0]}
              />
            </div>
            <div>
              <Label htmlFor="endDate">End Date *</Label>
              <Input
                id="endDate"
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                min={startDate || new Date().toISOString().split('T')[0]}
              />
            </div>
          </div>

          <div>
            <Label>Days of Week *</Label>
            <div className="grid grid-cols-4 gap-2 mt-2">
              {daysOfWeek.map((day) => (
                <label key={day.value} className="flex items-center gap-2 cursor-pointer">
                  <Checkbox
                    checked={selectedDays.includes(day.value)}
                    onCheckedChange={() => toggleDay(day.value)}
                  />
                  <span className="text-sm">{day.label}</span>
                </label>
              ))}
            </div>
            {selectedDays.length === 0 && (
              <p className="text-sm text-red-600 mt-1">Please select at least one day</p>
            )}
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <Label htmlFor="startTime">Start Time *</Label>
              <Input
                id="startTime"
                type="time"
                {...register('startTime', { required: 'Start time is required' })}
              />
              {errors.startTime && (
                <p className="text-sm text-red-600 mt-1">{errors.startTime.message}</p>
              )}
            </div>
            <div>
              <Label htmlFor="endTime">End Time *</Label>
              <Input
                id="endTime"
                type="time"
                {...register('endTime', { required: 'End time is required' })}
              />
              {errors.endTime && (
                <p className="text-sm text-red-600 mt-1">{errors.endTime.message}</p>
              )}
            </div>
            <div>
              <Label htmlFor="slotDuration">Slot Duration (min) *</Label>
              <Input
                id="slotDuration"
                type="number"
                {...register('slotDuration', { 
                  required: 'Slot duration is required',
                  min: { value: 15, message: 'Minimum 15 minutes' }
                })}
                placeholder="60"
              />
              {errors.slotDuration && (
                <p className="text-sm text-red-600 mt-1">{errors.slotDuration.message}</p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose} disabled={createMutation.isPending}>
              Cancel
            </Button>
            <Button 
              type="submit" 
              disabled={
                createMutation.isPending || 
                selectedDoctors.length === 0 || 
                selectedDays.length === 0 || 
                !startDate || 
                !endDate
              }
            >
              {createMutation.isPending ? 'Creating...' : 'Create Availability'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

