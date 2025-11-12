package com.nexus.ecommerce.inventory.service.impl;

import com.nexus.ecommerce.inventory.entity.Location;
import com.nexus.ecommerce.inventory.model.request.LocationRequest;
import com.nexus.ecommerce.inventory.model.response.LocationResponse;
import com.nexus.ecommerce.inventory.repository.LocationRepository;
import com.nexus.ecommerce.inventory.service.LocationService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of LocationService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    @Transactional
    public LocationResponse createLocation(
            UUID tenantId,
            UUID currentUserId,
            List<String> roles,
            LocationRequest request) {
        
        if (!canManageLocations(roles)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Only SELLER and ADMIN can create locations"
            );
        }

        // Check if location name already exists for this tenant
        // Note: The unique constraint in database also enforces this, but we check here for better error message
        locationRepository.findByTenantId(tenantId).stream()
            .filter(loc -> loc.getName().equalsIgnoreCase(request.name()) && loc.getActive())
            .findFirst()
            .ifPresent(existing -> {
                throw new BusinessException(
                    ErrorCode.LOCATION_NAME_ALREADY_EXISTS,
                    "Location name already exists for this tenant"
                );
            });

        Location location = Location.builder()
            .name(request.name())
            .type(request.type())
            .address(request.address())
            .tenantId(tenantId)
            .active(true)
            .build();

        Location saved = locationRepository.save(location);
        log.info("Created location: {} for tenant: {}", saved.getId(), tenantId);

        return toLocationResponse(saved);
    }

    @Override
    public LocationResponse getLocationById(UUID locationId, UUID tenantId) {
        Location location = locationRepository.findByIdAndTenantId(locationId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.LOCATION_NOT_FOUND,
                "Location not found"
            ));

        return toLocationResponse(location);
    }

    @Override
    public List<LocationResponse> getAllLocations(UUID tenantId, boolean activeOnly) {
        List<Location> locations;
        if (activeOnly) {
            locations = locationRepository.findByTenantIdAndActiveTrue(tenantId);
        } else {
            locations = locationRepository.findByTenantId(tenantId);
        }
        
        return locations.stream()
            .map(this::toLocationResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LocationResponse updateLocation(
            UUID locationId,
            UUID tenantId,
            UUID currentUserId,
            List<String> roles,
            LocationRequest request) {
        
        if (!canManageLocations(roles)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Only SELLER and ADMIN can update locations"
            );
        }

        Location location = locationRepository.findByIdAndTenantId(locationId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.LOCATION_NOT_FOUND,
                "Location not found"
            ));

        // Check if name changed and if new name already exists
        if (!location.getName().equals(request.name())) {
            locationRepository.findByTenantId(tenantId).stream()
                .filter(loc -> loc.getName().equalsIgnoreCase(request.name()) && loc.getActive())
                .filter(loc -> !loc.getId().equals(locationId))
                .findFirst()
                .ifPresent(existing -> {
                    throw new BusinessException(
                        ErrorCode.LOCATION_NAME_ALREADY_EXISTS,
                        "Location name already exists for this tenant"
                    );
                });
        }

        location.setName(request.name());
        location.setType(request.type());
        location.setAddress(request.address());

        Location updated = locationRepository.save(location);
        log.info("Updated location: {} for tenant: {}", locationId, tenantId);

        return toLocationResponse(updated);
    }

    @Override
    @Transactional
    public void deactivateLocation(
            UUID locationId,
            UUID tenantId,
            UUID currentUserId,
            List<String> roles) {
        
        if (!canManageLocations(roles)) {
            throw new BusinessException(
                ErrorCode.UNAUTHORIZED,
                "Only SELLER and ADMIN can deactivate locations"
            );
        }

        Location location = locationRepository.findByIdAndTenantId(locationId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.LOCATION_NOT_FOUND,
                "Location not found"
            ));

        // Soft delete: set active = false
        location.setActive(false);
        locationRepository.save(location);
        log.info("Deactivated location: {} for tenant: {}", locationId, tenantId);
    }

    @Override
    public boolean canManageLocations(List<String> roles) {
        return roles.contains("SELLER") || roles.contains("ADMIN");
    }

    private LocationResponse toLocationResponse(Location location) {
        return new LocationResponse(
            location.getId(),
            location.getName(),
            location.getType(),
            location.getAddress(),
            location.getTenantId(),
            location.getActive(),
            location.getCreatedAt(),
            location.getUpdatedAt()
        );
    }
}

