package com.nexus.ecommerce.fulfilment.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.ecommerce.fulfilment.model.response.TrackingHistoryResponse;
import com.nexus.ecommerce.fulfilment.repository.DeliveryRepository;
import com.nexus.ecommerce.fulfilment.repository.TrackingHistoryRepository;
import com.nexus.ecommerce.fulfilment.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tracking Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingServiceImpl implements TrackingService {
    
    private final TrackingHistoryRepository trackingHistoryRepository;
    private final DeliveryRepository deliveryRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<TrackingHistoryResponse> getTrackingHistory(UUID deliveryId, UUID tenantId) {
        // Verify delivery exists and belongs to tenant
        deliveryRepository.findById(deliveryId)
            .filter(d -> d.getTenantId().equals(tenantId))
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        return trackingHistoryRepository.findByDeliveryIdOrderByCreatedAtDesc(deliveryId)
            .stream()
            .map(th -> new TrackingHistoryResponse(
                th.getId(),
                th.getDeliveryId(),
                th.getLatitude(),
                th.getLongitude(),
                th.getLocationDescription(),
                th.getStatus(),
                th.getUpdatedBy(),
                th.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }
}

