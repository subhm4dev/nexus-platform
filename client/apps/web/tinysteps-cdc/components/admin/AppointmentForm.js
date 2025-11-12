'use client';

import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useDoctors } from '@/hooks/useDoctors';
import { useSessionTypes } from '@/hooks/useSessionTypes';
import { useAvailableSlots } from '@/hooks/useAppointments';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
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
import { Checkbox } from '@/components/ui/checkbox';

export function AppointmentForm({ 
  open, 
  onClose, 
  appointment, 
  patientId,
  onSubmit, 
  isLoading 
}) {
  const [selectedDoctor, setSelectedDoctor] = useState('');
  const [selectedSessionType, setSelectedSessionType] = useState('');
  const [selectedDate, setSelectedDate] = useState('');
  const [selectedSlot, setSelectedSlot] = useState('');
  const [sendPaymentLink, setSendPaymentLink] = useState(false);

  const { data: doctorsData } = useDoctors({ page: 0, size: 100 });
  const { data: sessionTypes } = useSessionTypes();
  const { data: availableSlots } = useAvailableSlots(
    selectedDoctor,
    selectedDate,
    selectedSessionType
  );

  const doctors = doctorsData?.content || [];

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm({
    defaultValues: appointment || {
      patientId: patientId || '',
      doctorId: '',
      sessionTypeId: '',
      appointmentDate: '',
      startTime: '',
      notes: '',
    },
  });

  useEffect(() => {
    if (appointment) {
      setSelectedDoctor(appointment.doctorId);
      setSelectedSessionType(appointment.sessionTypeId);
      setSelectedDate(appointment.appointmentDate);
      setSelectedSlot(appointment.startTime);
    }
  }, [appointment]);

  const handleFormSubmit = async (data) => {
    try {
      const submitData = {
        ...data,
        doctorId: selectedDoctor,
        sessionTypeId: selectedSessionType,
        appointmentDate: selectedDate,
        startTime: selectedSlot,
      };
      await onSubmit(submitData, sendPaymentLink);
      reset();
      setSelectedDoctor('');
      setSelectedSessionType('');
      setSelectedDate('');
      setSelectedSlot('');
      setSendPaymentLink(false);
      onClose();
    } catch (error) {
      // Error handled by parent
    }
  };

  const minDate = new Date().toISOString().split('T')[0];

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {appointment ? 'Edit Appointment' : 'Create Appointment'}
          </DialogTitle>
          <DialogDescription>
            {appointment ? 'Update appointment details' : 'Book a new appointment'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          {!patientId && (
            <div>
              <Label htmlFor="patientId">Patient *</Label>
              <Input
                id="patientId"
                type="text"
                placeholder="Enter patient ID or search..."
                {...register('patientId', { required: 'Patient is required' })}
              />
              {errors.patientId && (
                <p className="text-sm text-red-600 mt-1">{errors.patientId.message}</p>
              )}
            </div>
          )}

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
              <p className="text-sm text-red-600 mt-1">Doctor is required</p>
            )}
          </div>

          <div>
            <Label htmlFor="sessionTypeId">Session Type *</Label>
            <Select value={selectedSessionType} onValueChange={setSelectedSessionType}>
              <SelectTrigger id="sessionTypeId">
                <SelectValue placeholder="Select a session type" />
              </SelectTrigger>
              <SelectContent>
                {sessionTypes?.map((st) => (
                  <SelectItem key={st.id} value={st.id}>
                    {st.name} - {st.durationMinutes} min - ₹{st.price}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {!selectedSessionType && (
              <p className="text-sm text-red-600 mt-1">Session type is required</p>
            )}
          </div>

          <div>
            <Label htmlFor="appointmentDate">Date *</Label>
            <Input
              id="appointmentDate"
              type="date"
              min={minDate}
              value={selectedDate}
              onChange={(e) => {
                setSelectedDate(e.target.value);
                setSelectedSlot('');
              }}
            />
            {!selectedDate && (
              <p className="text-sm text-red-600 mt-1">Date is required</p>
            )}
          </div>

          {selectedDoctor && selectedDate && selectedSessionType && availableSlots && (
            <div>
              <Label>Available Time Slots *</Label>
              <div className="grid grid-cols-4 gap-2 mt-2">
                {availableSlots.length === 0 ? (
                  <p className="text-sm text-neutral-500 col-span-4">
                    No available slots for this date
                  </p>
                ) : (
                  availableSlots.map((slot) => (
                    <button
                      key={slot.startTime}
                      type="button"
                      onClick={() => setSelectedSlot(slot.startTime)}
                      className={`p-2 rounded border text-sm ${
                        selectedSlot === slot.startTime
                          ? 'bg-blue-600 text-white border-blue-600'
                          : 'bg-white hover:bg-neutral-50'
                      }`}
                    >
                      {slot.startTime}
                    </button>
                  ))
                )}
              </div>
              {!selectedSlot && availableSlots.length > 0 && (
                <p className="text-sm text-red-600 mt-1">Please select a time slot</p>
              )}
            </div>
          )}

          <div>
            <Label htmlFor="notes">Notes</Label>
            <textarea
              id="notes"
              {...register('notes')}
              rows={3}
              className="w-full px-3 py-2 border rounded-md"
              placeholder="Additional notes..."
            />
          </div>

          {!appointment && (
            <div className="flex items-center gap-2">
              <Checkbox
                id="sendPaymentLink"
                checked={sendPaymentLink}
                onCheckedChange={setSendPaymentLink}
              />
              <Label htmlFor="sendPaymentLink" className="cursor-pointer">
                Send payment link to patient
              </Label>
            </div>
          )}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>
              Cancel
            </Button>
            <Button 
              type="submit" 
              disabled={isLoading || !selectedDoctor || !selectedSessionType || !selectedDate || !selectedSlot}
            >
              {isLoading ? 'Saving...' : appointment ? 'Update' : 'Create'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

