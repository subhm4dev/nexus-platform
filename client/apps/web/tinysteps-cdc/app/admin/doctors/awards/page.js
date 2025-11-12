'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Award, Plus, Search, Filter } from 'lucide-react';

export default function DoctorAwardsPage() {
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div className="space-y-6">
      <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-4">
        <span>Admin</span>
        <span className="text-gray-400">/</span>
        <span>Doctors</span>
        <span className="text-gray-400">/</span>
        <span className="text-gray-900 font-medium">Awards</span>
      </nav>

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
            <Award className="w-8 h-8 text-yellow-600" />
            Doctor Awards
          </h1>
          <p className="text-gray-600 mt-1">Manage doctor awards and recognitions</p>
        </div>
        <Button>
          <Plus className="w-4 h-4 mr-2" />
          Add Award
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Awards Management</CardTitle>
            <div className="flex items-center gap-2">
              <Input
                placeholder="Search awards..."
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
            <Award className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">Awards Management Ready</h3>
            <p className="text-gray-600 mb-4">Manage doctor awards, honors, and recognitions.</p>
            <Button>Set Up Awards</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

