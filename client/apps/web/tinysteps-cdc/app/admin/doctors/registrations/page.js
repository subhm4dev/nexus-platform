'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { FileText, Plus, Search, Filter } from 'lucide-react';

export default function DoctorRegistrationsPage() {
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div className="space-y-6">
      <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-4">
        <span>Admin</span>
        <span className="text-gray-400">/</span>
        <span>Doctors</span>
        <span className="text-gray-400">/</span>
        <span className="text-gray-900 font-medium">Registrations</span>
      </nav>

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
            <FileText className="w-8 h-8 text-purple-600" />
            Doctor Registrations
          </h1>
          <p className="text-gray-600 mt-1">Manage doctor professional registrations</p>
        </div>
        <Button>
          <Plus className="w-4 h-4 mr-2" />
          Add Registration
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Registrations Management</CardTitle>
            <div className="flex items-center gap-2">
              <Input
                placeholder="Search registrations..."
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
            <FileText className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">Registrations Management Ready</h3>
            <p className="text-gray-600 mb-4">Manage doctor professional registrations and licenses.</p>
            <Button>Set Up Registrations</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

