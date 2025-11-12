'use client';

import { useState } from 'react';
import { useGenerateAppointmentReport, useGeneratePaymentReconciliation } from '@/hooks/useReports';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { toast } from '@/lib/toast';
import { FileText, Download } from 'lucide-react';

export default function ReportsPage() {
  const [appointmentStartDate, setAppointmentStartDate] = useState('');
  const [appointmentEndDate, setAppointmentEndDate] = useState('');
  const [paymentDate, setPaymentDate] = useState(new Date().toISOString().split('T')[0]);

  const appointmentReportMutation = useGenerateAppointmentReport();
  const paymentReportMutation = useGeneratePaymentReconciliation();

  const handleGenerateAppointmentReport = async () => {
    if (!appointmentStartDate || !appointmentEndDate) {
      toast.error('Please select both start and end dates');
      return;
    }

    try {
      await appointmentReportMutation.mutateAsync({
        startDate: appointmentStartDate,
        endDate: appointmentEndDate,
      });
      toast.success('Appointment report generated successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to generate report');
    }
  };

  const handleGeneratePaymentReport = async () => {
    if (!paymentDate) {
      toast.error('Please select a date');
      return;
    }

    try {
      await paymentReportMutation.mutateAsync(paymentDate);
      toast.success('Payment reconciliation report generated successfully');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to generate report');
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-neutral-900">Reports</h1>
        <p className="text-sm text-neutral-600 mt-1">Generate and export reports</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="w-5 h-5" />
              Appointment Report
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-sm text-neutral-600">
              Generate Excel report with appointment and payment data
            </p>
            <div className="space-y-3">
              <div>
                <Label htmlFor="appointmentStartDate">Start Date *</Label>
                <Input
                  id="appointmentStartDate"
                  type="date"
                  value={appointmentStartDate}
                  onChange={(e) => setAppointmentStartDate(e.target.value)}
                />
              </div>
              <div>
                <Label htmlFor="appointmentEndDate">End Date *</Label>
                <Input
                  id="appointmentEndDate"
                  type="date"
                  value={appointmentEndDate}
                  onChange={(e) => setAppointmentEndDate(e.target.value)}
                  min={appointmentStartDate}
                />
              </div>
              <Button
                onClick={handleGenerateAppointmentReport}
                disabled={appointmentReportMutation.isPending || !appointmentStartDate || !appointmentEndDate}
                className="w-full"
              >
                <Download className="w-4 h-4 mr-2" />
                {appointmentReportMutation.isPending ? 'Generating...' : 'Generate Report'}
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="w-5 h-5" />
              Payment Reconciliation Report
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-sm text-neutral-600">
              Generate payment reconciliation Excel report
            </p>
            <div className="space-y-3">
              <div>
                <Label htmlFor="paymentDate">Date *</Label>
                <Input
                  id="paymentDate"
                  type="date"
                  value={paymentDate}
                  onChange={(e) => setPaymentDate(e.target.value)}
                />
              </div>
              <Button
                onClick={handleGeneratePaymentReport}
                disabled={paymentReportMutation.isPending || !paymentDate}
                className="w-full"
              >
                <Download className="w-4 h-4 mr-2" />
                {paymentReportMutation.isPending ? 'Generating...' : 'Generate Report'}
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
