package com.nexus.ecommerce.fulfilment.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.ecommerce.fulfilment.entity.Driver;
import com.nexus.ecommerce.fulfilment.model.request.CreateDriverRequest;
import com.nexus.ecommerce.fulfilment.model.request.UpdateDriverRequest;
import com.nexus.ecommerce.fulfilment.model.response.DriverResponse;
import com.nexus.ecommerce.fulfilment.repository.DriverRepository;
import com.nexus.ecommerce.fulfilment.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Driver Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DriverServiceImpl implements DriverService {
    
    private final DriverRepository driverRepository;
    
    @Override
    @Transactional
    public DriverResponse createDriver(UUID tenantId, CreateDriverRequest request) {
        log.info("Creating driver: name={}, tenantId={}", request.name(), tenantId);
        
        // Check if driver with same phone already exists
        driverRepository.findByPhoneAndTenantId(request.phone(), tenantId)
            .ifPresent(d -> {
                throw new BusinessException(
                    ErrorCode.INVALID_OPERATION,
                    "Driver with phone " + request.phone() + " already exists"
                );
            });
        
        Driver driver = Driver.builder()
            .tenantId(tenantId)
            .name(request.name())
            .phone(request.phone())
            .email(request.email())
            .vehicleType(request.vehicleType())
            .vehicleNumber(request.vehicleNumber())
            .status(Driver.DriverStatus.AVAILABLE)
            .createdAt(LocalDateTime.now())
            .build();
        
        Driver savedDriver = driverRepository.save(driver);
        
        log.info("Driver created: driverId={}", savedDriver.getId());
        
        return toResponse(savedDriver);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DriverResponse getDriverById(UUID driverId, UUID tenantId) {
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Driver not found: " + driverId
            ));
        
        if (!driver.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Driver belongs to different tenant"
            );
        }
        
        return toResponse(driver);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getAllDrivers(UUID tenantId) {
        return driverRepository.findByTenantId(tenantId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> getAvailableDrivers(UUID tenantId) {
        return driverRepository.findByTenantIdAndStatus(tenantId, Driver.DriverStatus.AVAILABLE)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public DriverResponse updateDriver(UUID driverId, UUID tenantId, UpdateDriverRequest request) {
        log.info("Updating driver: driverId={}", driverId);
        
        Driver driver = driverRepository.findById(driverId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Driver not found: " + driverId
            ));
        
        if (!driver.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Driver belongs to different tenant"
            );
        }
        
        if (request.name() != null) {
            driver.setName(request.name());
        }
        if (request.phone() != null) {
            driver.setPhone(request.phone());
        }
        if (request.email() != null) {
            driver.setEmail(request.email());
        }
        if (request.vehicleType() != null) {
            driver.setVehicleType(request.vehicleType());
        }
        if (request.vehicleNumber() != null) {
            driver.setVehicleNumber(request.vehicleNumber());
        }
        if (request.status() != null) {
            driver.setStatus(request.status());
        }
        
        driver.setUpdatedAt(LocalDateTime.now());
        
        Driver savedDriver = driverRepository.save(driver);
        
        log.info("Driver updated: driverId={}", driverId);
        
        return toResponse(savedDriver);
    }
    
    private DriverResponse toResponse(Driver driver) {
        return new DriverResponse(
            driver.getId(),
            driver.getTenantId(),
            driver.getName(),
            driver.getPhone(),
            driver.getEmail(),
            driver.getVehicleType(),
            driver.getVehicleNumber(),
            driver.getStatus(),
            driver.getCreatedAt(),
            driver.getUpdatedAt()
        );
    }
}

