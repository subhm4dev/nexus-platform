'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Calendar, Plus, Search, Filter } from 'lucide-react';

export default function PatientAppointmentsPage() {
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div className="space-y-6">
      <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-4">
        <span>Admin</span>
        <span className="text-gray-400">/</span>
        <span>Patients</span>
        <span className="text-gray-400">/</span>
        <span className="text-gray-900 font-medium">Appointments</span>
      </nav>

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
            <Calendar className="w-8 h-8 text-blue-600" />
            Patient Appointments
          </h1>
          <p className="text-gray-600 mt-1">Manage patient appointments and schedules</p>
        </div>
        <Button>
          <Plus className="w-4 h-4 mr-2" />
          Schedule Appointment
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Appointments Management</CardTitle>
            <div className="flex items-center gap-2">
              <Input
                placeholder="Search appointments..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="max-w-md"
              />
              <Button variant="outline" size="icon">
                <Filter className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="text-center py-12">
            <Calendar className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">Appointments Management Ready</h3>
            <p className="text-gray-600 mb-4">Manage patient appointments, schedules, and visit history.</p>
            <Button>Set Up Appointments</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

