package com.nexus.healthcare.doctor.controller;

import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.doctor.model.request.QualificationRequest;
import com.nexus.healthcare.doctor.model.response.QualificationResponse;
import com.nexus.healthcare.doctor.service.QualificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/healthcare/qualifications")
@Tag(name = "Qualification Management", description = "Master data for medical qualifications")
@RequiredArgsConstructor
@Slf4j
public class QualificationController {
    
    private final QualificationService qualificationService;
    
    @PostMapping
    @Operation(summary = "Create qualification", description = "Creates a new qualification (master data)")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<QualificationResponse> createQualification(@Valid @RequestBody QualificationRequest request) {
        QualificationResponse response = qualificationService.createQualification(request);
        return ApiResponse.success(response, "Qualification created successfully");
    }
    
    @GetMapping
    @Operation(summary = "Get all active qualifications", description = "Retrieves all active qualifications")
    public ApiResponse<List<QualificationResponse>> getAllActiveQualifications() {
        List<QualificationResponse> response = qualificationService.getAllActiveQualifications();
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get qualification by ID", description = "Retrieves qualification by ID")
    public ApiResponse<QualificationResponse> getQualificationById(@PathVariable UUID id) {
        QualificationResponse response = qualificationService.getQualificationById(id);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update qualification", description = "Updates qualification")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<QualificationResponse> updateQualification(
            @PathVariable UUID id,
            @Valid @RequestBody QualificationRequest request) {
        QualificationResponse response = qualificationService.updateQualification(id, request);
        return ApiResponse.success(response, "Qualification updated successfully");
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete qualification", description = "Deactivates qualification")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deleteQualification(@PathVariable UUID id) {
        qualificationService.deleteQualification(id);
        return ApiResponse.success(null, "Qualification deleted successfully");
    }
}

