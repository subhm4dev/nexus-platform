'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useProcessPosPayment } from '@/hooks/usePayments';
import { useAppointmentsByPatient } from '@/hooks/useAppointments';
import { usePatients } from '@/hooks/usePatients';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { toast } from '@/lib/toast';
import { Search } from 'lucide-react';

export default function PosPaymentPage() {
  const [selectedPatient, setSelectedPatient] = useState('');
  const [selectedAppointment, setSelectedAppointment] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  const { data: patientsData } = usePatients({ query: searchQuery, page: 0, size: 20 });
  const { data: appointments } = useAppointmentsByPatient(selectedPatient);
  const processMutation = useProcessPosPayment();

  const patients = patientsData?.content || [];

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm({
    defaultValues: {
      amount: '',
      paymentMethod: 'CASH',
      notes: '',
    },
  });

  const selectedAppointmentData = appointments?.find(a => a.id === selectedAppointment);

  const handleSubmitPayment = async (data) => {
    if (!selectedAppointment) {
      toast.error('Please select an appointment');
      return;
    }

    try {
      await processMutation.mutateAsync({
        appointmentId: selectedAppointment,
        amount: parseFloat(data.amount),
        paymentMethod: data.paymentMethod,
        paymentSource: 'POS',
        notes: data.notes,
      });
      toast.success('Payment processed successfully');
      reset();
      setSelectedPatient('');
      setSelectedAppointment('');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to process payment');
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-neutral-900">POS Payment</h1>
        <p className="text-sm text-neutral-600 mt-1">Process payments at point of sale</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Select Appointment</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div>
            <Label htmlFor="patientSearch">Search Patient</Label>
            <div className="flex gap-2">
              <Input
                id="patientSearch"
                placeholder="Search by name, email, or phone..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <Search className="w-5 h-5 text-neutral-400 mt-2" />
            </div>
          </div>

          {searchQuery && patients.length > 0 && (
            <div>
              <Label>Select Patient</Label>
              <Select value={selectedPatient} onValueChange={setSelectedPatient}>
                <SelectTrigger>
                  <SelectValue placeholder="Select a patient" />
                </SelectTrigger>
                <SelectContent>
                  {patients.map((patient) => (
                    <SelectItem key={patient.id} value={patient.id}>
                      {patient.firstName} {patient.lastName} - {patient.phone}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}

          {selectedPatient && appointments && (
            <div>
              <Label>Select Appointment</Label>
              <Select value={selectedAppointment} onValueChange={setSelectedAppointment}>
                <SelectTrigger>
                  <SelectValue placeholder="Select an appointment" />
                </SelectTrigger>
                <SelectContent>
                  {appointments
                    .filter(a => a.status === 'SCHEDULED' || a.status === 'COMPLETED')
                    .map((appointment) => (
                      <SelectItem key={appointment.id} value={appointment.id}>
                        {new Date(appointment.appointmentDate).toLocaleDateString()} - {appointment.startTime}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
            </div>
          )}

          {selectedAppointmentData && (
            <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
              <p className="text-sm font-medium">Appointment Details</p>
              <p className="text-xs text-neutral-600 mt-1">
                Date: {new Date(selectedAppointmentData.appointmentDate).toLocaleDateString()}
              </p>
              <p className="text-xs text-neutral-600">
                Time: {selectedAppointmentData.startTime}
              </p>
            </div>
          )}
        </CardContent>
      </Card>

      {selectedAppointment && (
        <Card>
          <CardHeader>
            <CardTitle>Payment Details</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(handleSubmitPayment)} className="space-y-4">
              <div>
                <Label htmlFor="amount">Amount (₹) *</Label>
                <Input
                  id="amount"
                  type="number"
                  step="0.01"
                  {...register('amount', { 
                    required: 'Amount is required',
                    min: { value: 0.01, message: 'Amount must be greater than 0' }
                  })}
                  placeholder="1500.00"
                />
                {errors.amount && (
                  <p className="text-sm text-red-600 mt-1">{errors.amount.message}</p>
                )}
              </div>

              <div>
                <Label htmlFor="paymentMethod">Payment Method *</Label>
                <select
                  id="paymentMethod"
                  {...register('paymentMethod', { required: 'Payment method is required' })}
                  className="w-full px-3 py-2 border rounded-md"
                >
                  <option value="CASH">Cash</option>
                  <option value="POS_CARD">Card (POS)</option>
                  <option value="POS_UPI">UPI</option>
                </select>
                {errors.paymentMethod && (
                  <p className="text-sm text-red-600 mt-1">{errors.paymentMethod.message}</p>
                )}
              </div>

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

              <Button type="submit" disabled={processMutation.isPending} className="w-full">
                {processMutation.isPending ? 'Processing...' : 'Process Payment'}
              </Button>
            </form>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
