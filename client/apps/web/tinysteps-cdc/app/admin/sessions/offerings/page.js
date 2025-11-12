'use client';

import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { BookOpen, Plus, Search, Filter } from 'lucide-react';

export default function SessionOfferingsPage() {
  const [searchQuery, setSearchQuery] = useState('');

  return (
    <div className="space-y-6">
      <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-4">
        <span>Admin</span>
        <span className="text-gray-400">/</span>
        <span>Sessions</span>
        <span className="text-gray-400">/</span>
        <span className="text-gray-900 font-medium">Session Offerings</span>
      </nav>

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
            <BookOpen className="w-8 h-8 text-blue-600" />
            Session Offerings
          </h1>
          <p className="text-gray-600 mt-1">Manage session offerings and availability</p>
        </div>
        <Button>
          <Plus className="w-4 h-4 mr-2" />
          Add Offering
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Session Offerings Management</CardTitle>
            <div className="flex items-center gap-2">
              <Input
                placeholder="Search offerings..."
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
            <BookOpen className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">Session Offerings Management Ready</h3>
            <p className="text-gray-600 mb-4">Manage session offerings linked to session types and doctors.</p>
            <Button>Set Up Session Offerings</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

