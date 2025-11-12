'use client';

import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { ComboboxWithAdd } from '@/components/admin/ComboboxWithAdd';
import { useSpecializations, useCreateSpecialization } from '@/hooks/useSpecializations';
import { useQualificationNames } from '@/hooks/useQualifications';

export function DoctorForm({ open, onClose, doctor, onSubmit, isLoading }) {
  const { data: specializations = [], isLoading: isLoadingSpecializations } = useSpecializations();
  const { data: qualificationNames = [], isLoading: isLoadingQualificationNames } = useQualificationNames();
  const createSpecializationMutation = useCreateSpecialization();

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
    setValue,
  } = useForm({
    defaultValues: doctor || {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      specializationIds: [],
      qualificationName: '',
      qualificationInstitution: '',
      qualificationYear: '',
      experienceYears: '',
      bio: '',
    },
    mode: 'onChange',
  });

  const specializationId = watch('specializationIds')?.[0] || '';
  const qualificationName = watch('qualificationName') || '';

  // Reset form when doctor changes
  useEffect(() => {
    if (doctor) {
      reset({
        ...doctor,
        specializationIds: doctor.specializations?.map(s => s.id) || [],
        qualificationName: doctor.qualifications?.[0]?.name || '',
        qualificationInstitution: doctor.qualifications?.[0]?.institution || '',
        qualificationYear: doctor.qualifications?.[0]?.year || '',
      });
    } else {
      reset({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        specializationIds: [],
        qualificationName: '',
        qualificationInstitution: '',
        qualificationYear: '',
        experienceYears: '',
        bio: '',
      });
    }
  }, [doctor, reset]);

  const handleFormSubmit = async (data) => {
    try {
      // Ensure specializationIds is an array and has at least one item
      const specializationIds = Array.isArray(data.specializationIds) 
        ? data.specializationIds 
        : data.specializationIds ? [data.specializationIds] : [];
      
      if (specializationIds.length === 0) {
        setValue('specializationIds', [], { shouldValidate: true });
        return; // Don't submit if no specialization selected
      }

      const submitData = {
        ...data,
        specializationIds,
        qualificationName: data.qualificationName || null,
        qualificationInstitution: data.qualificationInstitution || null,
        qualificationYear: data.qualificationYear ? parseInt(data.qualificationYear) : null,
      };
      
      await onSubmit(submitData);
      reset();
      onClose();
    } catch (error) {
      // Error handled by parent
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{doctor ? 'Edit Doctor' : 'Add New Doctor'}</DialogTitle>
          <DialogDescription>
            {doctor ? 'Update doctor information' : 'Enter doctor details to create a new profile'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="firstName">First Name *</Label>
              <Input
                id="firstName"
                {...register('firstName', { required: 'First name is required' })}
                placeholder="John"
              />
              {errors.firstName && (
                <p className="text-sm text-red-600 mt-1">{errors.firstName.message}</p>
              )}
            </div>
            <div>
              <Label htmlFor="lastName">Last Name *</Label>
              <Input
                id="lastName"
                {...register('lastName', { required: 'Last name is required' })}
                placeholder="Doe"
              />
              {errors.lastName && (
                <p className="text-sm text-red-600 mt-1">{errors.lastName.message}</p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="email">Email *</Label>
              <Input
                id="email"
                type="email"
                {...register('email', { 
                  required: 'Email is required',
                  pattern: {
                    value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                    message: 'Invalid email address'
                  }
                })}
                placeholder="john.doe@example.com"
              />
              {errors.email && (
                <p className="text-sm text-red-600 mt-1">{errors.email.message}</p>
              )}
            </div>
            <div>
              <Label htmlFor="phone">Phone *</Label>
              <Input
                id="phone"
                type="tel"
                {...register('phone', { required: 'Phone is required' })}
                placeholder="+919876543210"
              />
              {errors.phone && (
                <p className="text-sm text-red-600 mt-1">{errors.phone.message}</p>
              )}
            </div>
          </div>

          <div>
            <ComboboxWithAdd
              label="Specialization *"
              options={specializations}
              value={specializationId}
              onChange={(id) => {
                const ids = id ? [id] : [];
                setValue('specializationIds', ids, { shouldValidate: true });
                // Validate that at least one specialization is selected
                if (!id) {
                  setValue('specializationIds', [], { shouldValidate: true });
                }
              }}
              onCreate={async (data) => {
                const result = await createSpecializationMutation.mutateAsync(data);
                if (result?.id) {
                  setValue('specializationIds', [result.id], { shouldValidate: true });
                }
                return result;
              }}
              placeholder="Select or add specialization..."
              isLoading={isLoadingSpecializations}
              isCreating={createSpecializationMutation.isPending}
              error={!specializationId ? 'Specialization is required' : undefined}
              fieldName="Specialization"
            />
            {!specializationId && (
              <p className="text-sm text-red-600 mt-1">Specialization is required</p>
            )}
          </div>

          <div>
            <ComboboxWithAdd
              label="Qualification"
              options={qualificationNames}
              value={qualificationName}
              onChange={(name) => {
                setValue('qualificationName', name || '', { shouldValidate: true });
              }}
              onCreate={async (name) => {
                // For qualifications, we just return the name - it will be saved when doctor is created
                return name;
              }}
              placeholder="Select or add qualification (e.g., MBBS, MD)..."
              isLoading={isLoadingQualificationNames}
              isCreating={false}
              error={errors.qualificationName?.message}
              fieldName="Qualification"
            />
            {errors.qualificationName && (
              <p className="text-sm text-red-600 mt-1">{errors.qualificationName.message}</p>
            )}
          </div>

          {qualificationName && (
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="qualificationInstitution">Institution</Label>
                <Input
                  id="qualificationInstitution"
                  {...register('qualificationInstitution')}
                  placeholder="University/College name"
                />
              </div>
              <div>
                <Label htmlFor="qualificationYear">Year</Label>
                <Input
                  id="qualificationYear"
                  type="number"
                  {...register('qualificationYear', { 
                    min: { value: 1900, message: 'Year must be 1900 or later' },
                    max: { value: new Date().getFullYear(), message: 'Year cannot be in the future' }
                  })}
                  placeholder="2020"
                />
                {errors.qualificationYear && (
                  <p className="text-sm text-red-600 mt-1">{errors.qualificationYear.message}</p>
                )}
              </div>
            </div>
          )}

          <div>
            <Label htmlFor="experienceYears">Years of Experience</Label>
            <Input
              id="experienceYears"
              type="number"
              {...register('experienceYears', { 
                min: { value: 0, message: 'Experience must be 0 or more' }
              })}
              placeholder="5"
            />
            {errors.experienceYears && (
              <p className="text-sm text-red-600 mt-1">{errors.experienceYears.message}</p>
            )}
          </div>

          <div>
            <Label htmlFor="bio">Bio</Label>
            <textarea
              id="bio"
              {...register('bio')}
              rows={4}
              className="w-full px-3 py-2 border rounded-md"
              placeholder="Brief description about the doctor..."
            />
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose} disabled={isLoading}>
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? 'Saving...' : doctor ? 'Update' : 'Create'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

