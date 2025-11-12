'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Camera, Plus, Search, Filter } from 'lucide-react';

export default function DoctorPhotosPage() {
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div className="space-y-6">
      <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-4">
        <span>Admin</span>
        <span className="text-gray-400">/</span>
        <span>Doctors</span>
        <span className="text-gray-400">/</span>
        <span className="text-gray-900 font-medium">Photos</span>
      </nav>

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
            <Camera className="w-8 h-8 text-pink-600" />
            Doctor Photos
          </h1>
          <p className="text-gray-600 mt-1">Manage doctor profile photos and images</p>
        </div>
        <Button>
          <Plus className="w-4 h-4 mr-2" />
          Upload Photo
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Photos Management</CardTitle>
            <div className="flex items-center gap-2">
              <Input
                placeholder="Search photos..."
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
            <Camera className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">Photos Management Ready</h3>
            <p className="text-gray-600 mb-4">Manage doctor profile photos and gallery images.</p>
            <Button>Set Up Photos</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

