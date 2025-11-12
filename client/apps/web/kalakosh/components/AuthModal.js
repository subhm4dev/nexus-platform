'use client';

import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { X } from 'lucide-react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { LoginRequestSchema, RegisterRequestSchema } from '@ecom/shared-schemas';
import { useAuthStore } from '@/stores/auth-store';
import { useAuthModal } from '@/hooks/useAuthModal';
import { useCreateOrUpdateProfile } from '@/hooks/useProfile';
import { Dialog, DialogContent, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';

/**
 * Unified Auth Modal Component
 * 
 * Single modal with tab switching between Login and Sign Up.
 * Defaults to Login tab.
 * Smooth tab transitions with Framer Motion.
 */
export function AuthModal() {
  const { isOpen, closeModal } = useAuthModal();
  const { login, register: registerUser, isLoading, error, clearError } = useAuthStore();
  const createOrUpdateProfileMutation = useCreateOrUpdateProfile();
  const [activeTab, setActiveTab] = useState('login');
  const [showPassword, setShowPassword] = useState(false);

  // Login form
  const loginForm = useForm({
    resolver: zodResolver(LoginRequestSchema),
  });

  // Get tenant ID from environment (needed for form defaults)
  // Use a constant value to ensure it's always available
  const KALAKOSH_TENANT_ID = '371e4723-6d8c-40d2-934e-dd82a80e6541';
  const appTenantId = typeof window !== 'undefined' 
    ? (window.process?.env?.NEXT_PUBLIC_APP_TENANT_ID || process.env.NEXT_PUBLIC_APP_TENANT_ID || KALAKOSH_TENANT_ID)
    : (process.env.NEXT_PUBLIC_APP_TENANT_ID || KALAKOSH_TENANT_ID);

  // Register form - create custom schema that includes fullName (for profile creation after registration)
  const registerFormSchema = z.object({
    email: z
      .union([z.string().email(), z.literal('')])
      .transform((val) => (val === '' ? undefined : val))
      .optional(),
    phone: z
      .union([
        z.string().regex(/^\+[1-9]\d{1,14}$/, 'Phone must be in E.164 format (e.g., +919876543210)'),
        z.literal('')
      ])
      .transform((val) => (val === '' ? undefined : val))
      .optional(),
    password: z.string().min(8, 'Password must be at least 8 characters'),
    domainCode: z.string().min(1, 'Domain code is required'),
    tenantId: z.string().uuid('Tenant ID must be a valid UUID'),
    role: z.enum(['CUSTOMER', 'SELLER', 'ADMIN', 'STAFF', 'DRIVER']).default('CUSTOMER'),
    fullName: z.string().min(1, 'Full name is required').optional(),
  }).refine(data => data.email || data.phone, {
    message: 'Either email or phone is required',
    path: ['email'],
  });
  
  // Ensure tenantId is always a valid string
  const defaultTenantId = appTenantId || KALAKOSH_TENANT_ID;
  
  const registerForm = useForm({
    resolver: zodResolver(registerFormSchema),
    defaultValues: {
      role: 'CUSTOMER',
      domainCode: 'ecommerce',
      tenantId: defaultTenantId, // Always set from env or fallback
    },
  });

  // Ensure domainCode and tenantId are always set in the form
  // Set them when modal opens or when register tab is active
  useEffect(() => {
    if (isOpen && activeTab === 'register') {
      registerForm.setValue('domainCode', 'ecommerce', { shouldValidate: false });
      registerForm.setValue('tenantId', defaultTenantId, { shouldValidate: false });
      registerForm.setValue('role', 'CUSTOMER', { shouldValidate: false });
    }
  }, [isOpen, activeTab, registerForm, defaultTenantId]);

  const handleLogin = async (data) => {
    try {
      clearError();
      await login(data);
      loginForm.reset();
      closeModal();
    } catch (error) {
      // Error is handled by auth store
    }
  };

  const handleRegister = async (data) => {
    try {
      clearError();
      
      // Always ensure domainCode and tenantId are set (from form, env, or fallback)
      const finalTenantId = data.tenantId || defaultTenantId || KALAKOSH_TENANT_ID;
      const finalDomainCode = data.domainCode || 'ecommerce';
      
      // Extract fullName before registration (it's not part of RegisterRequest)
      const { fullName, ...registrationData } = data;
      
      // Build final registration data with guaranteed values
      const finalRegistrationData = {
        email: registrationData.email,
        phone: registrationData.phone,
        password: registrationData.password,
        role: registrationData.role || 'CUSTOMER',
        domainCode: finalDomainCode,
        tenantId: finalTenantId,
      };
      
      // Validate tenantId is set and is a valid UUID
      if (!finalRegistrationData.tenantId) {
        throw new Error('Tenant ID is required. Please refresh the page and try again.');
      }
      
      if (!finalRegistrationData.domainCode) {
        throw new Error('Domain code is required. Please refresh the page and try again.');
      }
      
      console.log('Registering with data:', { ...finalRegistrationData, password: '***' });
      
      // Register user
      const response = await registerUser(finalRegistrationData);
      
      // Create profile with fullName after successful registration
      if (fullName) {
        try {
          await createOrUpdateProfileMutation.mutateAsync({
            fullName,
            email: data.email,
          });
        } catch (profileError) {
          // Profile creation failed, but registration succeeded
          // User can update profile later
          console.error('Failed to create profile:', profileError);
        }
      }
      
      registerForm.reset();
      closeModal();
    } catch (error) {
      // Error is handled by auth store
      console.error('Registration error:', error);
    }
  };

  const handleClose = () => {
    loginForm.reset();
    registerForm.reset();
    clearError();
    setActiveTab('login');
    setShowPassword(false);
    closeModal();
  };

  const switchToRegister = () => {
    loginForm.reset();
    clearError();
    setActiveTab('register');
  };

  const switchToLogin = () => {
    registerForm.reset();
    clearError();
    setActiveTab('login');
  };

  if (!isOpen) return null;

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md p-0 overflow-hidden">
        <DialogTitle className="sr-only">Authentication</DialogTitle>
        <DialogDescription className="sr-only">
          Login or register to access your account
        </DialogDescription>
        <motion.div
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          exit={{ scale: 0.9, opacity: 0 }}
          transition={{ duration: 0.2 }}
        >
          {/* Header */}
          <div className="bg-gradient-to-r from-[rgb(var(--color-indigo))] to-[rgb(var(--color-terracotta))] p-6 text-white relative">
            <button
              onClick={handleClose}
              className="absolute top-4 right-4 p-1 hover:bg-white/20 rounded-full transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
            <h2 className="text-2xl mb-1 font-semibold">Welcome to Kalakosh</h2>
            <p className="text-white/80 text-sm">A treasure of Odisha's pride possession - Pattachitra</p>
          </div>

          {/* Tabs */}
          <Tabs value={activeTab} onValueChange={(value) => {
            if (value === 'login') {
              switchToLogin();
            } else {
              switchToRegister();
            }
          }} className="p-6">
            <TabsList className="!grid !w-full grid-cols-2 mb-6">
              <TabsTrigger value="login">Login</TabsTrigger>
              <TabsTrigger value="register">Register</TabsTrigger>
            </TabsList>

            {/* Login Tab */}
            <TabsContent value="login">
              <form onSubmit={loginForm.handleSubmit(handleLogin)} className="space-y-4">
                <div>
                  <Label htmlFor="login-email">Email</Label>
                  <Input
                    {...loginForm.register('email')}
                    id="login-email"
                    type="email"
                    placeholder="you@example.com"
                    required
                  />
                  {loginForm.formState.errors.email && (
                    <p className="mt-1 text-sm text-red-600">{loginForm.formState.errors.email.message}</p>
                  )}
                </div>
                <div>
                  <Label htmlFor="login-password">Password</Label>
                  <Input
                    {...loginForm.register('password')}
                    id="login-password"
                    type="password"
                    placeholder="••••••••"
                    required
                  />
                  {loginForm.formState.errors.password && (
                    <p className="mt-1 text-sm text-red-600">{loginForm.formState.errors.password.message}</p>
                  )}
                </div>
                {error && (
                  <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                    {error}
                  </div>
                )}
                <Button type="submit" disabled={isLoading} className="w-full bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white">
                  {isLoading ? 'Logging in...' : 'Login'}
                </Button>
              </form>
            </TabsContent>

            {/* Register Tab */}
            <TabsContent value="register">
              <form onSubmit={registerForm.handleSubmit(handleRegister)} className="space-y-4">
                {/* Hidden registered fields - ensures domainCode and tenantId are always in form data */}
                <Controller
                  name="domainCode"
                  control={registerForm.control}
                  defaultValue="ecommerce"
                  render={({ field }) => <input type="hidden" {...field} />}
                />
                <Controller
                  name="tenantId"
                  control={registerForm.control}
                  defaultValue={defaultTenantId}
                  render={({ field }) => <input type="hidden" {...field} />}
                />
                <Controller
                  name="role"
                  control={registerForm.control}
                  defaultValue="CUSTOMER"
                  render={({ field }) => <input type="hidden" {...field} />}
                />
                
                <div>
                  <Label htmlFor="register-name">Full Name</Label>
                  <Input
                    {...registerForm.register('fullName')}
                    id="register-name"
                    type="text"
                    placeholder="Your Name"
                    required
                  />
                  {registerForm.formState.errors.fullName && (
                    <p className="mt-1 text-sm text-red-600">{registerForm.formState.errors.fullName.message}</p>
                  )}
                </div>
                <div>
                  <Label htmlFor="register-email">Email</Label>
                  <Input
                    {...registerForm.register('email')}
                    id="register-email"
                    type="email"
                    placeholder="you@example.com"
                    required
                  />
                  {registerForm.formState.errors.email && (
                    <p className="mt-1 text-sm text-red-600">{registerForm.formState.errors.email.message}</p>
                  )}
                </div>
                <div>
                  <Label htmlFor="register-password">Password</Label>
                  <Input
                    {...registerForm.register('password')}
                    id="register-password"
                    type="password"
                    placeholder="••••••••"
                    required
                  />
                  {registerForm.formState.errors.password && (
                    <p className="mt-1 text-sm text-red-600">{registerForm.formState.errors.password.message}</p>
                  )}
                </div>
                {error && (
                  <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-md text-sm">
                    {error}
                  </div>
                )}
                <Button type="submit" disabled={isLoading} className="w-full bg-[rgb(var(--color-indigo))] hover:bg-[rgb(var(--color-indigo-light))] text-white">
                  {isLoading ? 'Registering...' : 'Create Account'}
                </Button>
              </form>
            </TabsContent>
          </Tabs>
        </motion.div>
      </DialogContent>
    </Dialog>
  );
}

