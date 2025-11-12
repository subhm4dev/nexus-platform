'use client';

import { useRouter } from 'next/navigation';
import { useDoctors } from '@/hooks/useDoctors';
import { usePatients } from '@/hooks/usePatients';
import { useAppointmentsByDoctor } from '@/hooks/useAppointments';
import { Calendar, Users, Stethoscope, CreditCard } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import StatsCard from '@/components/admin/cards/StatsCard';

/**
 * Admin Dashboard
 * 
 * Overview page with key metrics and quick actions.
 */
export default function AdminDashboard() {
  const router = useRouter();
  const today = new Date().toISOString().split('T')[0];
  
  // Get first doctor for today's appointments (if any)
  const { data: doctorsData, error: doctorsError, isLoading: doctorsLoading } = useDoctors({ page: 0, size: 1 });
  const firstDoctor = doctorsData?.content?.[0];
  const { data: todayAppointments, error: appointmentsError } = useAppointmentsByDoctor(
    firstDoctor?.id || '',
    today
  );

  const { data: allDoctors, error: allDoctorsError, isLoading: allDoctorsLoading } = useDoctors({ page: 0, size: 100 });
  const { data: patientsData, error: patientsError, isLoading: patientsLoading } = usePatients({ page: 0, size: 100 });

  // Handle errors gracefully - don't throw, just log
  if (doctorsError || allDoctorsError || patientsError || appointmentsError) {
    console.error('Error loading dashboard data:', {
      doctorsError: doctorsError?.response?.status === 404 
        ? 'Backend service not available. Please ensure healthcare services are running.'
        : doctorsError?.message,
      allDoctorsError: allDoctorsError?.response?.status === 404
        ? 'Backend service not available. Please ensure healthcare services are running.'
        : allDoctorsError?.message,
      patientsError: patientsError?.response?.status === 404
        ? 'Backend service not available. Please ensure healthcare services are running.'
        : patientsError?.message,
      appointmentsError: appointmentsError?.message,
      statusCodes: {
        doctors: doctorsError?.response?.status,
        patients: patientsError?.response?.status,
      },
    });
  }

  const doctorsCount = allDoctors?.content?.length || 0;
  const patientsCount = patientsData?.content?.length || 0;
  const appointmentsCount = todayAppointments?.length || 0;
  
  const isLoading = doctorsLoading || allDoctorsLoading || patientsLoading;

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-neutral-900">Dashboard</h1>
          <p className="text-sm text-neutral-600 mt-1">Welcome to TinySteps CDC Admin Panel</p>
        </div>
        <div className="text-center py-8 text-neutral-500">Loading dashboard data...</div>
      </div>
    );
  }

  // Show helpful error message if services are not available
  const has404Error = doctorsError?.response?.status === 404 || patientsError?.response?.status === 404;
  if (has404Error && !isLoading) {
    const gatewayUrl = typeof window !== 'undefined' 
      ? (window.process?.env?.NEXT_PUBLIC_GATEWAY_URL || process.env.NEXT_PUBLIC_GATEWAY_URL || 'http://localhost:8080')
      : 'http://localhost:8080';
    
    const failedUrl = doctorsError?.config?.url || patientsError?.config?.url || 'unknown';
    
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-neutral-900">Dashboard</h1>
          <p className="text-sm text-neutral-600 mt-1">Welcome to TinySteps CDC Admin Panel</p>
        </div>
        <Card>
          <CardContent className="p-6">
            <div className="text-center py-8">
              <div className="text-amber-600 mb-4">
                <p className="text-lg font-semibold mb-2">API Connection Issue</p>
                <p className="text-sm text-neutral-600 mb-4">
                  Getting 404 errors from backend. Services may be running but gateway routing may need attention.
                </p>
                <div className="bg-neutral-50 border rounded-lg p-4 text-left max-w-2xl mx-auto space-y-3">
                  <div>
                    <p className="font-medium text-sm mb-1">Current Configuration:</p>
                    <p className="text-xs text-neutral-600 font-mono">Gateway URL: {gatewayUrl}</p>
                    <p className="text-xs text-neutral-600 font-mono">Failed URL: {failedUrl}</p>
                  </div>
                  <div>
                    <p className="font-medium text-sm mb-1">Troubleshooting Steps:</p>
                    <ul className="text-xs text-neutral-600 space-y-1 list-disc list-inside">
                      <li>Verify gateway is running: <code className="bg-neutral-200 px-1 rounded">curl http://localhost:8080/actuator/health</code></li>
                      <li>Check gateway routes: Ensure <code className="bg-neutral-200 px-1 rounded">/api/v1/healthcare/doctors/**</code> and <code className="bg-neutral-200 px-1 rounded">/api/v1/healthcare/patients/**</code> are configured</li>
                      <li>Verify services are accessible: <code className="bg-neutral-200 px-1 rounded">curl http://localhost:8093/actuator/health</code> and <code className="bg-neutral-200 px-1 rounded">curl http://localhost:8094/actuator/health</code></li>
                      <li>Create <code className="bg-neutral-200 px-1 rounded">.env.local</code> with: <code className="bg-neutral-200 px-1 rounded">NEXT_PUBLIC_GATEWAY_URL=http://localhost:8080</code></li>
                      <li>Restart Next.js dev server after creating .env.local</li>
                    </ul>
                  </div>
                  <div className="pt-2 border-t">
                    <p className="text-xs text-neutral-500">
                      Check browser console (F12) for detailed error logs and JWT payload information.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-neutral-900">Dashboard</h1>
        <p className="text-sm text-neutral-600 mt-1">Welcome to TinySteps CDC Admin Panel</p>
      </div>

      {/* Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard
          title="Today's Appointments"
          value={appointmentsCount}
          icon={Calendar}
          color="blue"
          description="Scheduled for today"
        />
        <StatsCard
          title="Pending Payments"
          value="-"
          icon={CreditCard}
          color="orange"
          description="Awaiting payment"
        />
        <StatsCard
          title="Active Doctors"
          value={doctorsCount}
          icon={Stethoscope}
          color="green"
          description="Registered doctors"
        />
        <StatsCard
          title="Active Patients"
          value={patientsCount}
          icon={Users}
          color="purple"
          description="Total patients"
        />
      </div>

      {/* Quick Actions */}
      <Card>
        <CardContent className="p-6">
          <h2 className="text-lg font-semibold text-neutral-900 mb-4">Quick Actions</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Button
              onClick={() => router.push('/admin/appointments')}
              className="w-full"
            >
              <Calendar className="w-5 h-5 mr-2" />
              Create Appointment
            </Button>
            <Button
              onClick={() => router.push('/admin/patients')}
              variant="secondary"
              className="w-full"
            >
              <Users className="w-5 h-5 mr-2" />
              Add Patient
            </Button>
            <Button
              onClick={() => router.push('/admin/payments/pos')}
              variant="outline"
              className="w-full"
            >
              <CreditCard className="w-5 h-5 mr-2" />
              Record Payment
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Upcoming Appointments */}
      <Card>
        <CardContent className="p-6">
          <h2 className="text-lg font-semibold text-neutral-900 mb-4">Upcoming Appointments</h2>
          {todayAppointments && todayAppointments.length > 0 ? (
            <div className="space-y-2">
              {todayAppointments.slice(0, 5).map((appointment) => (
                <div
                  key={appointment.id}
                  className="flex items-center justify-between p-3 border rounded-lg hover:bg-neutral-50"
                >
                  <div>
                    <p className="font-medium">{appointment.startTime}</p>
                    <p className="text-sm text-neutral-600">Patient ID: {appointment.patientId}</p>
                  </div>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => router.push(`/admin/appointments/${appointment.id}`)}
                  >
                    View
                  </Button>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-neutral-500">
              <Calendar className="w-12 h-12 mx-auto mb-2 opacity-50" />
              <p>No upcoming appointments for today</p>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

