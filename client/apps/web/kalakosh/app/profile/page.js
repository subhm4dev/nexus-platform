'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useProfile, useUpdateProfile } from '@/hooks/useProfile';
import { useAuthStore } from '@/stores/auth-store';
import { useAuthModal } from '@/hooks/useAuthModal';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

/**
 * Profile Page
 */
export default function ProfilePage() {
  const router = useRouter();
  const { isAuthenticated, hasCheckedAuth } = useAuthStore();
  const { openLogin } = useAuthModal();
  const { data: profile, isLoading } = useProfile();
  const updateProfileMutation = useUpdateProfile();

  useEffect(() => {
    if (hasCheckedAuth && !isAuthenticated) {
      openLogin();
      router.push('/');
    }
  }, [hasCheckedAuth, isAuthenticated, openLogin, router]);

  if (!hasCheckedAuth || !isAuthenticated) {
    return null;
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">Loading...</div>
      </div>
    );
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const profileData = {
      fullName: formData.get('fullName'),
      phone: formData.get('phone'),
      dateOfBirth: formData.get('dateOfBirth'),
    };
    try {
      await updateProfileMutation.mutateAsync(profileData);
    } catch (error) {
      // Error handling
    }
  };

  return (
    <div className="min-h-screen bg-[rgb(var(--color-ivory))] py-12">
      <div className="container mx-auto px-6 max-w-2xl">
        <h1 className="text-4xl mb-8 text-[rgb(var(--color-indigo))]">My Profile</h1>
        
        <div className="bg-white rounded-xl shadow-lg p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <Label htmlFor="fullName">Full Name</Label>
              <Input
                id="fullName"
                name="fullName"
                defaultValue={profile?.fullName || ''}
                required
              />
            </div>
            <div>
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                defaultValue={useAuthStore.getState().user?.email || ''}
                disabled
                className="bg-[rgb(var(--muted))]"
              />
            </div>
            <div>
              <Label htmlFor="phone">Phone</Label>
              <Input
                id="phone"
                name="phone"
                type="tel"
                defaultValue={profile?.phone || ''}
              />
            </div>
            <div>
              <Label htmlFor="dateOfBirth">Date of Birth</Label>
              <Input
                id="dateOfBirth"
                name="dateOfBirth"
                type="date"
                defaultValue={profile?.dateOfBirth || ''}
              />
            </div>
            <Button
              type="submit"
              size="lg"
              className="w-full bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white"
            >
              Save Changes
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
}

