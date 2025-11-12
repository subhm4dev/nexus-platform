package com.nexus.ecommerce.fulfilment.service.impl;

import com.nexus.ecommerce.fulfilment.entity.DeliveryPreference;
import com.nexus.ecommerce.fulfilment.entity.Fulfillment;
import com.nexus.ecommerce.fulfilment.model.request.UpdateDeliveryPreferenceRequest;
import com.nexus.ecommerce.fulfilment.model.response.DeliveryPreferenceResponse;
import com.nexus.ecommerce.fulfilment.repository.DeliveryPreferenceRepository;
import com.nexus.ecommerce.fulfilment.repository.FulfillmentRepository;
import com.nexus.ecommerce.fulfilment.service.DeliveryPreferenceService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryPreferenceServiceImpl implements DeliveryPreferenceService {
    
    private final DeliveryPreferenceRepository preferenceRepository;
    private final FulfillmentRepository fulfillmentRepository;
    
    @Override
    @Transactional
    public DeliveryPreferenceResponse updatePreferences(
        UUID fulfillmentId,
        UUID customerUserId,
        UUID tenantId,
        UpdateDeliveryPreferenceRequest request
    ) {
        log.info("Updating delivery preferences: fulfillmentId={}, customerUserId={}",
            fulfillmentId, customerUserId);
        
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Fulfillment not found: " + fulfillmentId
            ));
        
        if (!fulfillment.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        // Get or create preference
        DeliveryPreference preference = preferenceRepository.findByFulfillmentId(fulfillmentId)
            .orElse(DeliveryPreference.builder()
                .fulfillment(fulfillment)
                .fulfillmentId(fulfillmentId)
                .tenantId(tenantId)
                .customerUserId(customerUserId)
                .build());
        
        // Update fields
        preference.setScheduledDeliveryDate(request.scheduledDeliveryDate());
        preference.setScheduledDeliveryTimeStart(request.scheduledDeliveryTimeStart());
        preference.setScheduledDeliveryTimeEnd(request.scheduledDeliveryTimeEnd());
        preference.setDeliveryTimeWindow(request.deliveryTimeWindow());
        preference.setDeliveryInstructions(request.deliveryInstructions());
        preference.setSpecialHandlingNotes(request.specialHandlingNotes());
        preference.setLeaveAtDoor(request.leaveAtDoor());
        preference.setHandToCustomer(request.handToCustomer());
        preference.setRequireSignature(request.requireSignature());
        preference.setPreferredContactMethod(request.preferredContactMethod());
        preference.setPreferredContactTime(request.preferredContactTime());
        preference.setDoNotDisturb(request.doNotDisturb());
        preference.setPreferredDeliveryLocation(request.preferredDeliveryLocation());
        preference.setGateCode(request.gateCode());
        preference.setBuildingName(request.buildingName());
        preference.setFloorNumber(request.floorNumber());
        preference.setApartmentNumber(request.apartmentNumber());
        preference.setIsActive(true);
        preference.setAppliedAt(LocalDateTime.now());
        
        // Update fulfillment with scheduled date if provided
        if (request.scheduledDeliveryDate() != null) {
            fulfillment.setScheduledDeliveryDate(
                request.scheduledDeliveryDate().atStartOfDay()
            );
        }
        fulfillment.setDeliveryTimeWindow(request.deliveryTimeWindow());
        fulfillment.setDeliveryInstructions(request.deliveryInstructions());
        fulfillmentRepository.save(fulfillment);
        
        DeliveryPreference saved = preferenceRepository.save(preference);
        
        log.info("Delivery preferences updated: preferenceId={}", saved.getId());
        
        return toResponse(saved);
    }
    
    @Override
    public DeliveryPreferenceResponse getPreferences(UUID fulfillmentId, UUID tenantId) {
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Fulfillment not found: " + fulfillmentId
            ));
        
        if (!fulfillment.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return preferenceRepository.findByFulfillmentId(fulfillmentId)
            .map(this::toResponse)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Preferences not found for fulfillment: " + fulfillmentId
            ));
    }
    
    private DeliveryPreferenceResponse toResponse(DeliveryPreference preference) {
        return new DeliveryPreferenceResponse(
            preference.getId(),
            preference.getFulfillmentId(),
            preference.getDeliveryId(),
            preference.getTenantId(),
            preference.getCustomerUserId(),
            preference.getScheduledDeliveryDate(),
            preference.getScheduledDeliveryTimeStart(),
            preference.getScheduledDeliveryTimeEnd(),
            preference.getDeliveryTimeWindow(),
            preference.getDeliveryInstructions(),
            preference.getSpecialHandlingNotes(),
            preference.getLeaveAtDoor(),
            preference.getHandToCustomer(),
            preference.getRequireSignature(),
            preference.getPreferredContactMethod(),
            preference.getPreferredContactTime(),
            preference.getDoNotDisturb(),
            preference.getPreferredDeliveryLocation(),
            preference.getGateCode(),
            preference.getBuildingName(),
            preference.getFloorNumber(),
            preference.getApartmentNumber(),
            preference.getIsActive(),
            preference.getAppliedAt(),
            preference.getCreatedAt(),
            preference.getUpdatedAt()
        );
    }
}

