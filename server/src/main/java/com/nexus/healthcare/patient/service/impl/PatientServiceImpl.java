package com.nexus.healthcare.patient.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.healthcare.patient.entity.*;
import com.nexus.healthcare.patient.model.request.PatientRequest;
import com.nexus.healthcare.patient.model.response.PatientResponse;
import com.nexus.healthcare.patient.repository.*;
import com.nexus.healthcare.patient.service.IamServiceClient;
import com.nexus.healthcare.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {
    
    private final PatientRepository patientRepository;
    private final PatientMedicalHistoryRepository medicalHistoryRepository;
    private final PatientAllergyRepository allergyRepository;
    private final PatientMedicationRepository medicationRepository;
    private final PatientInsuranceRepository insuranceRepository;
    private final PatientEmergencyContactRepository emergencyContactRepository;
    private final IamServiceClient iamServiceClient;
    
    @Override
    @Transactional
    public PatientResponse createPatient(UUID userId, UUID tenantId, UUID domainId, PatientRequest request) {
        log.info("Creating patient for user: {}, tenant: {}", userId, tenantId);
        
        patientRepository.findByUserIdAndTenantIdAndDomainIdAndDeletedFalse(userId, tenantId, domainId)
            .ifPresent(patient -> {
                throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Patient profile already exists for this user");
            });
        
        Patient patient = Patient.builder()
            .userId(userId)
            .tenantId(tenantId)
            .domainId(domainId)
            .dateOfBirth(request.getDateOfBirth())
            .gender(request.getGender())
            .bloodGroup(request.getBloodGroup())
            .heightCm(request.getHeightCm())
            .weightKg(request.getWeightKg())
            .build();
        
        Patient saved = patientRepository.save(patient);
        return mapToResponse(saved);
    }
    
    @Override
    public PatientResponse getPatientById(UUID patientId, UUID tenantId, UUID domainId) {
        Patient patient = patientRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(patientId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Patient not found"));
        return mapToResponse(patient);
    }
    
    @Override
    public Page<PatientResponse> searchPatients(UUID tenantId, UUID domainId, String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Patient> patients = patientRepository.searchPatients(List.of(tenantId), domainId, query, pageable);
        return patients.map(this::mapToResponse);
    }
    
    @Override
    @Transactional
    public PatientResponse updatePatient(UUID patientId, UUID tenantId, UUID domainId, PatientRequest request) {
        Patient patient = patientRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(patientId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Patient not found"));
        
        if (request.getDateOfBirth() != null) patient.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) patient.setGender(request.getGender());
        if (request.getBloodGroup() != null) patient.setBloodGroup(request.getBloodGroup());
        if (request.getHeightCm() != null) patient.setHeightCm(request.getHeightCm());
        if (request.getWeightKg() != null) patient.setWeightKg(request.getWeightKg());
        
        Patient updated = patientRepository.save(patient);
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public void deletePatient(UUID patientId, UUID tenantId, UUID domainId) {
        Patient patient = patientRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(patientId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Patient not found"));
        
        patient.setDeleted(true);
        patient.setDeletedAt(java.time.LocalDateTime.now());
        patientRepository.save(patient);
    }
    
    @Override
    @Transactional
    public PatientResponse transferPatient(UUID patientId, UUID currentTenantId, UUID targetTenantId, UUID domainId, String jwtToken) {
        log.info("Transferring patient {} from tenant {} to tenant {}", patientId, currentTenantId, targetTenantId);
        
        // Validate: target tenant must be different from current
        if (currentTenantId.equals(targetTenantId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Target tenant must be different from current tenant");
        }
        
        // Validate target tenant is a BRANCH tenant under same APP tenant
        iamServiceClient.validateBranchTransfer(currentTenantId, targetTenantId, domainId, jwtToken);
        
        // Get current patient
        Patient currentPatient = patientRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(patientId, currentTenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Patient not found"));
        
        // Check if patient already exists at target branch
        patientRepository.findByUserIdAndTenantIdAndDomainIdAndDeletedFalse(currentPatient.getUserId(), targetTenantId, domainId)
            .ifPresent(p -> {
                throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Patient already exists at target branch");
            });
        
        // Update patient's tenant_id (transfer)
        currentPatient.setTenantId(targetTenantId);
        Patient updatedPatient = patientRepository.save(currentPatient);
        
        // Transfer related data - update tenant_id for all related records
        // Medical History
        List<PatientMedicalHistory> medicalHistories = medicalHistoryRepository.findByPatientIdAndTenantIdAndDomainId(patientId, currentTenantId, domainId);
        medicalHistories.forEach(mh -> {
            mh.setTenantId(targetTenantId);
            medicalHistoryRepository.save(mh);
        });
        
        // Allergies
        List<PatientAllergy> allergies = allergyRepository.findByPatientIdAndTenantIdAndDomainId(patientId, currentTenantId, domainId);
        allergies.forEach(allergy -> {
            allergy.setTenantId(targetTenantId);
            allergyRepository.save(allergy);
        });
        
        // Medications
        List<PatientMedication> medications = medicationRepository.findByPatientIdAndTenantIdAndDomainId(patientId, currentTenantId, domainId);
        medications.forEach(med -> {
            med.setTenantId(targetTenantId);
            medicationRepository.save(med);
        });
        
        // Insurance
        List<PatientInsurance> insurances = insuranceRepository.findByPatientIdAndTenantIdAndDomainId(patientId, currentTenantId, domainId);
        insurances.forEach(ins -> {
            ins.setTenantId(targetTenantId);
            insuranceRepository.save(ins);
        });
        
        // Emergency Contacts
        List<PatientEmergencyContact> emergencyContacts = emergencyContactRepository.findByPatientIdAndTenantIdAndDomainId(patientId, currentTenantId, domainId);
        emergencyContacts.forEach(ec -> {
            ec.setTenantId(targetTenantId);
            emergencyContactRepository.save(ec);
        });
        
        // Update user domain mapping in IAM service
        iamServiceClient.updateUserDomainMapping(currentPatient.getUserId(), targetTenantId, domainId, jwtToken);
        
        log.info("Patient {} transferred successfully from tenant {} to tenant {}", patientId, currentTenantId, targetTenantId);
        return mapToResponse(updatedPatient);
    }
    
    @Override
    @Transactional
    public PatientResponse addPatientToBranch(UUID patientId, UUID currentTenantId, UUID targetTenantId, UUID domainId, String jwtToken) {
        log.info("Adding patient {} to branch {} (current branch: {})", patientId, targetTenantId, currentTenantId);
        
        // Validate: target tenant must be different from current
        if (currentTenantId.equals(targetTenantId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Target tenant must be different from current tenant");
        }
        
        // Validate target tenant is a BRANCH tenant under same APP tenant
        iamServiceClient.validateBranchTransfer(currentTenantId, targetTenantId, domainId, jwtToken);
        
        // Get current patient
        Patient currentPatient = patientRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(patientId, currentTenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Patient not found"));
        
        // Check if patient already exists at target branch
        if (patientRepository.findByUserIdAndTenantIdAndDomainIdAndDeletedFalse(currentPatient.getUserId(), targetTenantId, domainId).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Patient already exists at target branch");
        }
        
        // Create new patient at target branch (keep original active)
        Patient newPatient = Patient.builder()
            .userId(currentPatient.getUserId())
            .tenantId(targetTenantId)
            .domainId(domainId)
            .dateOfBirth(currentPatient.getDateOfBirth())
            .gender(currentPatient.getGender())
            .bloodGroup(currentPatient.getBloodGroup())
            .heightCm(currentPatient.getHeightCm())
            .weightKg(currentPatient.getWeightKg())
            .build();
        Patient savedPatient = patientRepository.save(newPatient);
        
        // Copy related data to new patient at target branch
        // Medical History
        List<PatientMedicalHistory> medicalHistories = medicalHistoryRepository.findByPatientIdAndTenantIdAndDomainId(patientId, currentTenantId, domainId);
        medicalHistories.forEach(mh -> {
            PatientMedicalHistory newMh = PatientMedicalHistory.builder()
                .patientId(savedPatient.getId())
                .tenantId(targetTenantId)
                .domainId(domainId)
                .conditionName(mh.getConditionName())
                .diagnosisDate(mh.getDiagnosisDate())
                .status(mh.getStatus())
                .notes(mh.getNotes())
                .build();
            medicalHistoryRepository.save(newMh);
        });
        
        // Allergies
        List<PatientAllergy> allergies = allergyRepository.findByPatientIdAndTenantIdAndDomainId(patientId, currentTenantId, domainId);
        allergies.forEach(allergy -> {
            PatientAllergy newAllergy = PatientAllergy.builder()
                .patientId(savedPatient.getId())
                .tenantId(targetTenantId)
                .domainId(domainId)
                .allergenName(allergy.getAllergenName())
                .severity(allergy.getSeverity())
                .reactionDescription(allergy.getReactionDescription())
                .build();
            allergyRepository.save(newAllergy);
        });
        
        // Medications
        List<PatientMedication> medications = medicationRepository.findByPatientIdAndTenantIdAndDomainId(patientId, currentTenantId, domainId);
        medications.forEach(med -> {
            PatientMedication newMed = PatientMedication.builder()
                .patientId(savedPatient.getId())
                .tenantId(targetTenantId)
                .domainId(domainId)
                .medicationName(med.getMedicationName())
                .dosage(med.getDosage())
                .frequency(med.getFrequency())
                .startDate(med.getStartDate())
                .endDate(med.getEndDate())
                .prescribedBy(med.getPrescribedBy())
                .notes(med.getNotes())
                .build();
            medicationRepository.save(newMed);
        });
        
        // Insurance
        List<PatientInsurance> insurances = insuranceRepository.findByPatientIdAndTenantIdAndDomainId(patientId, currentTenantId, domainId);
        insurances.forEach(ins -> {
            PatientInsurance newIns = PatientInsurance.builder()
                .patientId(savedPatient.getId())
                .tenantId(targetTenantId)
                .domainId(domainId)
                .insuranceProvider(ins.getInsuranceProvider())
                .policyNumber(ins.getPolicyNumber())
                .groupNumber(ins.getGroupNumber())
                .expiryDate(ins.getExpiryDate())
                .isPrimary(ins.getIsPrimary())
                .build();
            insuranceRepository.save(newIns);
        });
        
        // Emergency Contacts
        List<PatientEmergencyContact> emergencyContacts = emergencyContactRepository.findByPatientIdAndTenantIdAndDomainId(patientId, currentTenantId, domainId);
        emergencyContacts.forEach(ec -> {
            PatientEmergencyContact newEc = PatientEmergencyContact.builder()
                .patientId(savedPatient.getId())
                .tenantId(targetTenantId)
                .domainId(domainId)
                .name(ec.getName())
                .relationship(ec.getRelationship())
                .phoneNumber(ec.getPhoneNumber())
                .email(ec.getEmail())
                .address(ec.getAddress())
                .isPrimary(ec.getIsPrimary())
                .build();
            emergencyContactRepository.save(newEc);
        });
        
        // Update user domain mapping in IAM service (add new mapping, keep old)
        iamServiceClient.updateUserDomainMapping(currentPatient.getUserId(), targetTenantId, domainId, jwtToken);
        
        log.info("Patient {} added to branch {} successfully", patientId, targetTenantId);
        return mapToResponse(savedPatient);
    }
    
    private PatientResponse mapToResponse(Patient patient) {
        return PatientResponse.builder()
            .id(patient.getId())
            .userId(patient.getUserId())
            .dateOfBirth(patient.getDateOfBirth())
            .gender(patient.getGender())
            .bloodGroup(patient.getBloodGroup())
            .heightCm(patient.getHeightCm())
            .weightKg(patient.getWeightKg())
            .createdAt(patient.getCreatedAt())
            .updatedAt(patient.getUpdatedAt())
            .build();
    }
}

