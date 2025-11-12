'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useDoctors } from '@/hooks/useDoctors';
import { usePatients } from '@/hooks/usePatients';
import { useTransferDoctor, useAddDoctorToBranch } from '@/hooks/useDoctors';
import { useTransferPatient, useAddPatientToBranch } from '@/hooks/usePatients';
import { TransferModal } from '@/components/admin/TransferModal';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { toast } from '@/lib/toast';
import { Search, ArrowRightLeft, Plus, Users, Stethoscope } from 'lucide-react';
import { useAuthStore } from '@/stores/auth-store';
import { useBranchStore } from '@/stores/branch-store';

export default function BranchesPage() {
  const router = useRouter();
  const { branches: authBranches, user } = useAuthStore();
  const { selectedBranchId, getSelectedBranch } = useBranchStore();
  const [searchQuery, setSearchQuery] = useState('');
  const [transferModalOpen, setTransferModalOpen] = useState(false);
  const [transferType, setTransferType] = useState(null); // 'doctor' or 'patient'
  const [transferEntity, setTransferEntity] = useState(null);
  const [actionType, setActionType] = useState(null); // 'transfer' or 'add'

  const { data: doctorsData, isLoading: doctorsLoading } = useDoctors({
    query: searchQuery || undefined,
    page: 0,
    size: 50,
  });

  const { data: patientsData, isLoading: patientsLoading } = usePatients({
    query: searchQuery || undefined,
    page: 0,
    size: 50,
  });

  const transferDoctorMutation = useTransferDoctor();
  const addDoctorToBranchMutation = useAddDoctorToBranch();
  const transferPatientMutation = useTransferPatient();
  const addPatientToBranchMutation = useAddPatientToBranch();

  const doctors = doctorsData?.content || [];
  const patients = patientsData?.content || [];
  // Get branches from auth store, with fallback to user tenantId
  const branches = authBranches && authBranches.length > 0 
    ? authBranches 
    : (user?.tenantId ? [user.tenantId] : []);
  
  // Debug logging
  console.log('BranchesPage - authBranches:', authBranches);
  console.log('BranchesPage - user:', user);
  console.log('BranchesPage - branches:', branches);

  const handleTransferDoctor = (doctor, action) => {
    setTransferType('doctor');
    setTransferEntity(doctor);
    setActionType(action);
    setTransferModalOpen(true);
  };

  const handleTransferPatient = (patient, action) => {
    setTransferType('patient');
    setTransferEntity(patient);
    setActionType(action);
    setTransferModalOpen(true);
  };

  const handleTransfer = async (targetTenantId) => {
    try {
      if (transferType === 'doctor') {
        if (actionType === 'transfer') {
          await transferDoctorMutation.mutateAsync({
            doctorId: transferEntity.id,
            targetTenantId,
          });
          toast.success('Doctor transferred successfully');
        } else {
          await addDoctorToBranchMutation.mutateAsync({
            doctorId: transferEntity.id,
            targetTenantId,
          });
          toast.success('Doctor added to branch successfully');
        }
      } else {
        if (actionType === 'transfer') {
          await transferPatientMutation.mutateAsync({
            patientId: transferEntity.id,
            targetTenantId,
          });
          toast.success('Patient transferred successfully');
        } else {
          await addPatientToBranchMutation.mutateAsync({
            patientId: transferEntity.id,
            targetTenantId,
          });
          toast.success('Patient added to branch successfully');
        }
      }
      setTransferModalOpen(false);
      setTransferEntity(null);
      setTransferType(null);
      setActionType(null);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to transfer');
      throw error;
    }
  };

  const handleAddToBranch = async (targetTenantId) => {
    await handleTransfer(targetTenantId);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-neutral-900">Branch Management</h1>
        <p className="text-sm text-neutral-600 mt-1">
          Transfer or add doctors and patients to branches
        </p>
      </div>

      {/* Current Branch Info */}
      <Card>
        <CardHeader>
          <CardTitle>Current Branch</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-neutral-600">Selected Branch</p>
              <p className="font-medium text-neutral-900">
                {selectedBranchId || getSelectedBranch() || 'No branch selected'}
              </p>
            </div>
            <Badge variant="outline">
              {branches.length} {branches.length === 1 ? 'Branch' : 'Branches'} Available
            </Badge>
          </div>
        </CardContent>
      </Card>

      {/* Doctors Section */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Stethoscope className="w-5 h-5 text-blue-600" />
              <CardTitle>Doctors</CardTitle>
            </div>
            <div className="flex-1 max-w-md">
              <Input
                placeholder="Search doctors..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {doctorsLoading ? (
            <div className="text-center py-8 text-neutral-500">Loading doctors...</div>
          ) : doctors.length === 0 ? (
            <div className="text-center py-8 text-neutral-500">
              No doctors found
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="text-left p-3 font-semibold">Name</th>
                    <th className="text-left p-3 font-semibold">Email</th>
                    <th className="text-left p-3 font-semibold">Specialization</th>
                    <th className="text-right p-3 font-semibold">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {doctors.map((doctor) => (
                    <tr key={doctor.id} className="border-b hover:bg-neutral-50">
                      <td className="p-3">
                        {doctor.firstName} {doctor.lastName}
                      </td>
                      <td className="p-3">{doctor.email}</td>
                      <td className="p-3">{doctor.specialization || '-'}</td>
                      <td className="p-3">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleTransferDoctor(doctor, 'transfer')}
                          >
                            <ArrowRightLeft className="w-4 h-4 mr-1" />
                            Transfer
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleTransferDoctor(doctor, 'add')}
                          >
                            <Plus className="w-4 h-4 mr-1" />
                            Add to Branch
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Patients Section */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Users className="w-5 h-5 text-purple-600" />
              <CardTitle>Patients</CardTitle>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {patientsLoading ? (
            <div className="text-center py-8 text-neutral-500">Loading patients...</div>
          ) : patients.length === 0 ? (
            <div className="text-center py-8 text-neutral-500">
              No patients found
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b">
                    <th className="text-left p-3 font-semibold">Name</th>
                    <th className="text-left p-3 font-semibold">Email</th>
                    <th className="text-left p-3 font-semibold">Phone</th>
                    <th className="text-right p-3 font-semibold">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {patients.map((patient) => (
                    <tr key={patient.id} className="border-b hover:bg-neutral-50">
                      <td className="p-3">
                        {patient.firstName} {patient.lastName}
                      </td>
                      <td className="p-3">{patient.email}</td>
                      <td className="p-3">{patient.phone || '-'}</td>
                      <td className="p-3">
                        <div className="flex items-center justify-end gap-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleTransferPatient(patient, 'transfer')}
                          >
                            <ArrowRightLeft className="w-4 h-4 mr-1" />
                            Transfer
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleTransferPatient(patient, 'add')}
                          >
                            <Plus className="w-4 h-4 mr-1" />
                            Add to Branch
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      <TransferModal
        open={transferModalOpen}
        onClose={() => {
          setTransferModalOpen(false);
          setTransferEntity(null);
          setTransferType(null);
          setActionType(null);
        }}
        title={
          actionType === 'transfer'
            ? `Transfer ${transferType === 'doctor' ? 'Doctor' : 'Patient'}`
            : `Add ${transferType === 'doctor' ? 'Doctor' : 'Patient'} to Branch`
        }
        description={
          actionType === 'transfer'
            ? `Move ${transferType === 'doctor' ? 'doctor' : 'patient'} to another branch`
            : `Add ${transferType === 'doctor' ? 'doctor' : 'patient'} to multiple branches`
        }
        onTransfer={handleTransfer}
        onAddToBranch={handleAddToBranch}
        isLoading={
          transferDoctorMutation.isPending ||
          addDoctorToBranchMutation.isPending ||
          transferPatientMutation.isPending ||
          addPatientToBranchMutation.isPending
        }
      />
    </div>
  );
}

