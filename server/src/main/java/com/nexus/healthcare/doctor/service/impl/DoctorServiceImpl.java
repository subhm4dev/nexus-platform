package com.nexus.healthcare.doctor.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.healthcare.doctor.entity.Doctor;
import com.nexus.healthcare.doctor.entity.DoctorSpecialization;
import com.nexus.healthcare.doctor.model.request.DoctorRequest;
import com.nexus.healthcare.doctor.model.response.DoctorResponse;
import com.nexus.healthcare.doctor.model.response.SpecializationResponse;
import com.nexus.healthcare.doctor.repository.AwardRepository;
import com.nexus.healthcare.doctor.repository.DoctorRepository;
import com.nexus.healthcare.doctor.repository.DoctorSpecializationRepository;
import com.nexus.healthcare.doctor.repository.DoctorQualificationRepository;
import com.nexus.healthcare.doctor.entity.DoctorQualification;
import com.nexus.healthcare.doctor.repository.SpecializationRepository;
import com.nexus.healthcare.doctor.service.IamServiceClient;
import com.nexus.healthcare.doctor.service.DoctorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {
    
    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;
    private final DoctorSpecializationRepository doctorSpecializationRepository;
    private final DoctorQualificationRepository doctorQualificationRepository;
    private final AwardRepository awardRepository;
    private final IamServiceClient iamServiceClient;
    
    @Override
    @Transactional
    public DoctorResponse createDoctor(UUID userId, UUID tenantId, UUID domainId, DoctorRequest request) {
        log.info("Creating doctor for user: {}, tenant: {}, domain: {}", userId, tenantId, domainId);
        
        // Check if doctor already exists for this user
        doctorRepository.findByUserIdAndTenantIdAndDomainIdAndDeletedFalse(userId, tenantId, domainId)
            .ifPresent(doctor -> {
                throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Doctor profile already exists for this user");
            });
        
        Doctor doctor = Doctor.builder()
            .userId(userId)
            .tenantId(tenantId)
            .domainId(domainId)
            .registrationNumber(request.getRegistrationNumber())
            .yearsOfExperience(request.getYearsOfExperience())
            .consultationFee(request.getConsultationFee())
            .bio(request.getBio())
            .profileImageUrl(request.getProfileImageUrl())
            .verificationStatus("PENDING")
            .build();
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        
        // Add specializations if provided
        if (request.getSpecializationIds() != null && !request.getSpecializationIds().isEmpty()) {
            for (UUID specializationId : request.getSpecializationIds()) {
                specializationRepository.findById(specializationId)
                    .ifPresent(spec -> {
                        DoctorSpecialization doctorSpec = DoctorSpecialization.builder()
                            .doctorId(savedDoctor.getId())
                            .specializationId(specializationId)
                            .isPrimary(false)
                            .build();
                        doctorSpecializationRepository.save(doctorSpec);
                    });
            }
        }
        
        // Add qualification if provided
        if (request.getQualificationName() != null && !request.getQualificationName().trim().isEmpty()) {
            DoctorQualification qualification = DoctorQualification.builder()
                .doctorId(savedDoctor.getId())
                .tenantId(tenantId)
                .domainId(domainId)
                .name(request.getQualificationName().trim())
                .institution(request.getQualificationInstitution())
                .year(request.getQualificationYear())
                .build();
            doctorQualificationRepository.save(qualification);
        }
        
        return mapToResponse(savedDoctor);
    }
    
    @Override
    public List<String> getDistinctQualificationNames() {
        return doctorQualificationRepository.findDistinctQualificationNames();
    }
    
    @Override
    public DoctorResponse getDoctorById(UUID doctorId, UUID tenantId, UUID domainId) {
        Doctor doctor = doctorRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(doctorId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Doctor not found"));
        
        return mapToResponse(doctor);
    }
    
    @Override
    public Page<DoctorResponse> searchDoctors(UUID tenantId, UUID domainId, String verificationStatus, String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Doctor> doctors = doctorRepository.searchDoctors(
            List.of(tenantId), domainId, verificationStatus, query, pageable
        );
        
        return doctors.map(this::mapToResponse);
    }
    
    @Override
    @Transactional
    public DoctorResponse updateDoctor(UUID doctorId, UUID tenantId, UUID domainId, DoctorRequest request) {
        Doctor doctor = doctorRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(doctorId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Doctor not found"));
        
        if (request.getRegistrationNumber() != null) {
            doctor.setRegistrationNumber(request.getRegistrationNumber());
        }
        if (request.getYearsOfExperience() != null) {
            doctor.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getConsultationFee() != null) {
            doctor.setConsultationFee(request.getConsultationFee());
        }
        if (request.getBio() != null) {
            doctor.setBio(request.getBio());
        }
        if (request.getProfileImageUrl() != null) {
            doctor.setProfileImageUrl(request.getProfileImageUrl());
        }
        
        Doctor updated = doctorRepository.save(doctor);
        
        // Update specializations if provided
        if (request.getSpecializationIds() != null) {
            doctorSpecializationRepository.deleteByDoctorId(doctorId);
            for (UUID specializationId : request.getSpecializationIds()) {
                specializationRepository.findById(specializationId)
                    .ifPresent(spec -> {
                        DoctorSpecialization doctorSpec = DoctorSpecialization.builder()
                            .doctorId(doctorId)
                            .specializationId(specializationId)
                            .isPrimary(false)
                            .build();
                        doctorSpecializationRepository.save(doctorSpec);
                    });
            }
        }
        
        // Update qualification if provided
        if (request.getQualificationName() != null && !request.getQualificationName().trim().isEmpty()) {
            // Delete existing qualifications for this doctor
            List<DoctorQualification> existingQuals = doctorQualificationRepository.findByDoctorIdAndTenantIdAndDomainId(doctorId, tenantId, domainId);
            doctorQualificationRepository.deleteAll(existingQuals);
            
            // Add new qualification
            DoctorQualification qualification = DoctorQualification.builder()
                .doctorId(doctorId)
                .tenantId(tenantId)
                .domainId(domainId)
                .name(request.getQualificationName().trim())
                .institution(request.getQualificationInstitution())
                .year(request.getQualificationYear())
                .build();
            doctorQualificationRepository.save(qualification);
        }
        
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteDoctor(UUID doctorId, UUID tenantId, UUID domainId) {
        Doctor doctor = doctorRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(doctorId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Doctor not found"));
        
        doctor.setDeleted(true);
        doctor.setDeletedAt(java.time.LocalDateTime.now());
        doctorRepository.save(doctor);
    }
    
    @Override
    @Transactional
    public DoctorResponse verifyDoctor(UUID doctorId, UUID tenantId, UUID domainId, String verificationStatus) {
        Doctor doctor = doctorRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(doctorId, tenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Doctor not found"));
        
        if (!List.of("PENDING", "VERIFIED", "REJECTED").contains(verificationStatus)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Invalid verification status");
        }
        
        doctor.setVerificationStatus(verificationStatus);
        Doctor updated = doctorRepository.save(doctor);
        
        return mapToResponse(updated);
    }
    
    @Override
    @Transactional
    public DoctorResponse transferDoctor(UUID doctorId, UUID currentTenantId, UUID targetTenantId, UUID domainId, String jwtToken) {
        log.info("Transferring doctor {} from tenant {} to tenant {}", doctorId, currentTenantId, targetTenantId);
        
        // Validate: target tenant must be different from current
        if (currentTenantId.equals(targetTenantId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Target tenant must be different from current tenant");
        }
        
        // Validate target tenant is a BRANCH tenant under same APP tenant
        iamServiceClient.validateBranchTransfer(currentTenantId, targetTenantId, domainId, jwtToken);
        
        // Get current doctor
        Doctor currentDoctor = doctorRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(doctorId, currentTenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Doctor not found"));
        
        // Check if doctor already exists at target branch
        doctorRepository.findByUserIdAndTenantIdAndDomainIdAndDeletedFalse(currentDoctor.getUserId(), targetTenantId, domainId)
            .ifPresent(d -> {
                throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Doctor already exists at target branch");
            });
        
        // Soft delete current doctor
        currentDoctor.setDeleted(true);
        currentDoctor.setDeletedAt(java.time.LocalDateTime.now());
        doctorRepository.save(currentDoctor);
        
        // Create new doctor at target branch
        Doctor newDoctor = Doctor.builder()
            .userId(currentDoctor.getUserId())
            .tenantId(targetTenantId)
            .domainId(domainId)
            .registrationNumber(currentDoctor.getRegistrationNumber())
            .yearsOfExperience(currentDoctor.getYearsOfExperience())
            .consultationFee(currentDoctor.getConsultationFee())
            .bio(currentDoctor.getBio())
            .profileImageUrl(currentDoctor.getProfileImageUrl())
            .verificationStatus(currentDoctor.getVerificationStatus())
            .build();
        Doctor savedDoctor = doctorRepository.save(newDoctor);
        
        // Transfer qualifications
        List<DoctorQualification> qualifications = 
            doctorQualificationRepository.findByDoctorIdAndTenantIdAndDomainId(currentDoctor.getId(), currentTenantId, domainId);
        for (var qual : qualifications) {
            DoctorQualification newQual = 
                DoctorQualification.builder()
                    .doctorId(savedDoctor.getId())
                    .tenantId(targetTenantId)
                    .domainId(domainId)
                    .name(qual.getName())
                    .institution(qual.getInstitution())
                    .year(qual.getYear())
                    .certificateUrl(qual.getCertificateUrl())
                    .build();
            doctorQualificationRepository.save(newQual);
        }
        
        // Transfer awards
        List<com.nexus.healthcare.doctor.entity.Award> awards = 
            awardRepository.findByDoctorIdAndTenantIdAndDomainId(currentDoctor.getId(), currentTenantId, domainId);
        for (var award : awards) {
            com.nexus.healthcare.doctor.entity.Award newAward = 
                com.nexus.healthcare.doctor.entity.Award.builder()
                    .doctorId(savedDoctor.getId())
                    .tenantId(targetTenantId)
                    .domainId(domainId)
                    .name(award.getName())
                    .organization(award.getOrganization())
                    .year(award.getYear())
                    .description(award.getDescription())
                    .certificateUrl(award.getCertificateUrl())
                    .build();
            awardRepository.save(newAward);
        }
        
        // Transfer specializations
        List<com.nexus.healthcare.doctor.entity.DoctorSpecialization> specializations = 
            doctorSpecializationRepository.findByDoctorId(currentDoctor.getId());
        for (var spec : specializations) {
            com.nexus.healthcare.doctor.entity.DoctorSpecialization newSpec = 
                com.nexus.healthcare.doctor.entity.DoctorSpecialization.builder()
                    .doctorId(savedDoctor.getId())
                    .specializationId(spec.getSpecializationId())
                    .isPrimary(spec.getIsPrimary())
                    .build();
            doctorSpecializationRepository.save(newSpec);
        }
        
        // Update user domain mapping in IAM service
        iamServiceClient.updateUserDomainMapping(currentDoctor.getUserId(), targetTenantId, domainId, jwtToken);
        
        log.info("Doctor {} transferred successfully from tenant {} to tenant {}", doctorId, currentTenantId, targetTenantId);
        return mapToResponse(savedDoctor);
    }
    
    @Override
    @Transactional
    public DoctorResponse addDoctorToBranch(UUID doctorId, UUID currentTenantId, UUID targetTenantId, UUID domainId, String jwtToken) {
        log.info("Adding doctor {} to branch {} (current branch: {})", doctorId, targetTenantId, currentTenantId);
        
        // Validate: target tenant must be different from current
        if (currentTenantId.equals(targetTenantId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Target tenant must be different from current tenant");
        }
        
        // Validate target tenant is a BRANCH tenant under same APP tenant
        iamServiceClient.validateBranchTransfer(currentTenantId, targetTenantId, domainId, jwtToken);
        
        // Get current doctor
        Doctor currentDoctor = doctorRepository.findByIdAndTenantIdAndDomainIdAndDeletedFalse(doctorId, currentTenantId, domainId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Doctor not found"));
        
        // Check if doctor already exists at target branch
        if (doctorRepository.findByUserIdAndTenantIdAndDomainIdAndDeletedFalse(currentDoctor.getUserId(), targetTenantId, domainId).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_TAKEN, "Doctor already exists at target branch");
        }
        
        // Create new doctor at target branch (keep original active)
        Doctor newDoctor = Doctor.builder()
            .userId(currentDoctor.getUserId())
            .tenantId(targetTenantId)
            .domainId(domainId)
            .registrationNumber(currentDoctor.getRegistrationNumber())
            .yearsOfExperience(currentDoctor.getYearsOfExperience())
            .consultationFee(currentDoctor.getConsultationFee())
            .bio(currentDoctor.getBio())
            .profileImageUrl(currentDoctor.getProfileImageUrl())
            .verificationStatus(currentDoctor.getVerificationStatus())
            .build();
        Doctor savedDoctor = doctorRepository.save(newDoctor);
        
        // Copy qualifications
        List<DoctorQualification> qualifications = 
            doctorQualificationRepository.findByDoctorIdAndTenantIdAndDomainId(currentDoctor.getId(), currentTenantId, domainId);
        for (var qual : qualifications) {
            DoctorQualification newQual = 
                DoctorQualification.builder()
                    .doctorId(savedDoctor.getId())
                    .tenantId(targetTenantId)
                    .domainId(domainId)
                    .name(qual.getName())
                    .institution(qual.getInstitution())
                    .year(qual.getYear())
                    .certificateUrl(qual.getCertificateUrl())
                    .build();
            doctorQualificationRepository.save(newQual);
        }
        
        // Copy awards
        List<com.nexus.healthcare.doctor.entity.Award> awards = 
            awardRepository.findByDoctorIdAndTenantIdAndDomainId(currentDoctor.getId(), currentTenantId, domainId);
        for (var award : awards) {
            com.nexus.healthcare.doctor.entity.Award newAward = 
                com.nexus.healthcare.doctor.entity.Award.builder()
                    .doctorId(savedDoctor.getId())
                    .tenantId(targetTenantId)
                    .domainId(domainId)
                    .name(award.getName())
                    .organization(award.getOrganization())
                    .year(award.getYear())
                    .description(award.getDescription())
                    .certificateUrl(award.getCertificateUrl())
                    .build();
            awardRepository.save(newAward);
        }
        
        // Copy specializations
        List<com.nexus.healthcare.doctor.entity.DoctorSpecialization> specializations = 
            doctorSpecializationRepository.findByDoctorId(currentDoctor.getId());
        for (var spec : specializations) {
            com.nexus.healthcare.doctor.entity.DoctorSpecialization newSpec = 
                com.nexus.healthcare.doctor.entity.DoctorSpecialization.builder()
                    .doctorId(savedDoctor.getId())
                    .specializationId(spec.getSpecializationId())
                    .isPrimary(spec.getIsPrimary())
                    .build();
            doctorSpecializationRepository.save(newSpec);
        }
        
        // Update user domain mapping in IAM service (add new mapping, keep old)
        iamServiceClient.updateUserDomainMapping(currentDoctor.getUserId(), targetTenantId, domainId, jwtToken);
        
        log.info("Doctor {} added to branch {} successfully", doctorId, targetTenantId);
        return mapToResponse(savedDoctor);
    }
    
    private DoctorResponse mapToResponse(Doctor doctor) {
        List<SpecializationResponse> specializations = doctorSpecializationRepository.findByDoctorId(doctor.getId())
            .stream()
            .map(ds -> specializationRepository.findById(ds.getSpecializationId()))
            .filter(java.util.Optional::isPresent)
            .map(opt -> {
                var spec = opt.get();
                return SpecializationResponse.builder()
                    .id(spec.getId())
                    .code(spec.getCode())
                    .name(spec.getName())
                    .description(spec.getDescription())
                    .active(spec.getActive())
                    .createdAt(spec.getCreatedAt())
                    .updatedAt(spec.getUpdatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        
        return DoctorResponse.builder()
            .id(doctor.getId())
            .userId(doctor.getUserId())
            .registrationNumber(doctor.getRegistrationNumber())
            .verificationStatus(doctor.getVerificationStatus())
            .yearsOfExperience(doctor.getYearsOfExperience())
            .consultationFee(doctor.getConsultationFee())
            .bio(doctor.getBio())
            .profileImageUrl(doctor.getProfileImageUrl())
            .specializations(specializations)
            .createdAt(doctor.getCreatedAt())
            .updatedAt(doctor.getUpdatedAt())
            .build();
    }
}

