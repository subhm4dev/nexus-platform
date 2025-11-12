package com.nexus.healthcare.doctor.controller;

import com.nexus.libs.response.dto.ApiResponse;
import com.nexus.healthcare.doctor.model.request.SpecializationRequest;
import com.nexus.healthcare.doctor.model.response.SpecializationResponse;
import com.nexus.healthcare.doctor.service.SpecializationService;
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
@RequestMapping("/api/v1/healthcare/specializations")
@Tag(name = "Specialization Management", description = "Master data for medical specializations")
@RequiredArgsConstructor
@Slf4j
public class SpecializationController {
    
    private final SpecializationService specializationService;
    
    @PostMapping
    @Operation(summary = "Create specialization", description = "Creates a new specialization (master data)")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<SpecializationResponse> createSpecialization(@Valid @RequestBody SpecializationRequest request) {
        SpecializationResponse response = specializationService.createSpecialization(request);
        return ApiResponse.success(response, "Specialization created successfully");
    }
    
    @GetMapping
    @Operation(summary = "Get all active specializations", description = "Retrieves all active specializations")
    public ApiResponse<List<SpecializationResponse>> getAllActiveSpecializations() {
        List<SpecializationResponse> response = specializationService.getAllActiveSpecializations();
        return ApiResponse.success(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get specialization by ID", description = "Retrieves specialization by ID")
    public ApiResponse<SpecializationResponse> getSpecializationById(@PathVariable UUID id) {
        SpecializationResponse response = specializationService.getSpecializationById(id);
        return ApiResponse.success(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update specialization", description = "Updates specialization")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<SpecializationResponse> updateSpecialization(
            @PathVariable UUID id,
            @Valid @RequestBody SpecializationRequest request) {
        SpecializationResponse response = specializationService.updateSpecialization(id, request);
        return ApiResponse.success(response, "Specialization updated successfully");
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete specialization", description = "Deactivates specialization")
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> deleteSpecialization(@PathVariable UUID id) {
        specializationService.deleteSpecialization(id);
        return ApiResponse.success(null, "Specialization deleted successfully");
    }
}

