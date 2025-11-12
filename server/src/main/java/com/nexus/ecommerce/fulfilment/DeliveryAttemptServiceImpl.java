package com.nexus.ecommerce.fulfilment.service.impl;

import com.nexus.ecommerce.fulfilment.entity.Delivery;
import com.nexus.ecommerce.fulfilment.entity.DeliveryAttempt;
import com.nexus.ecommerce.fulfilment.model.request.RecordDeliveryAttemptRequest;
import com.nexus.ecommerce.fulfilment.model.response.DeliveryAttemptResponse;
import com.nexus.ecommerce.fulfilment.repository.DeliveryAttemptRepository;
import com.nexus.ecommerce.fulfilment.repository.DeliveryRepository;
import com.nexus.ecommerce.fulfilment.service.DeliveryAttemptService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryAttemptServiceImpl implements DeliveryAttemptService {
    
    private final DeliveryAttemptRepository attemptRepository;
    private final DeliveryRepository deliveryRepository;
    
    @Override
    @Transactional
    public DeliveryAttemptResponse recordAttempt(
        UUID deliveryId,
        UUID driverId,
        UUID tenantId,
        RecordDeliveryAttemptRequest request
    ) {
        log.info("Recording delivery attempt: deliveryId={}, driverId={}, status={}",
            deliveryId, driverId, request.attemptStatus());
        
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Delivery belongs to different tenant"
            );
        }
        
        // Get current attempt count
        Integer attemptCount = attemptRepository.countAttemptsByDeliveryId(deliveryId);
        int nextAttemptNumber = attemptCount + 1;
        
        // Create attempt record
        DeliveryAttempt attempt = DeliveryAttempt.builder()
            .delivery(delivery)
            .deliveryId(deliveryId)
            .fulfillmentId(delivery.getFulfillmentId())
            .tenantId(tenantId)
            .attemptNumber(nextAttemptNumber)
            .attemptedAt(LocalDateTime.now())
            .attemptedByUserId(driverId)
            .attemptStatus(DeliveryAttempt.AttemptStatus.valueOf(request.attemptStatus()))
            .failureReason(request.failureReason())
            .failureCode(request.failureCode())
            .attemptLatitude(request.attemptLatitude())
            .attemptLongitude(request.attemptLongitude())
            .attemptLocationDescription(request.attemptLocationDescription())
            .photoUrls(request.photoUrls() != null ? request.photoUrls() : List.of())
            .notes(request.notes())
            .nextAttemptAt(request.nextAttemptAt())
            .nextAttemptScheduled(request.nextAttemptAt() != null)
            .build();
        
        DeliveryAttempt saved = attemptRepository.save(attempt);
        
        // Update delivery attempt count
        delivery.setAttemptCount(nextAttemptNumber);
        delivery.setLastAttemptAt(saved.getAttemptedAt());
        delivery.setNextAttemptAt(saved.getNextAttemptAt());
        delivery.setFailureReason(request.failureReason());
        deliveryRepository.save(delivery);
        
        log.info("Delivery attempt recorded: attemptId={}, attemptNumber={}", 
            saved.getId(), saved.getAttemptNumber());
        
        return toResponse(saved);
    }
    
    @Override
    public List<DeliveryAttemptResponse> getAttemptsByDeliveryId(UUID deliveryId, UUID tenantId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return attemptRepository.findByDeliveryIdOrderByAttemptNumberDesc(deliveryId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public Integer getAttemptCount(UUID deliveryId) {
        return attemptRepository.countAttemptsByDeliveryId(deliveryId);
    }
    
    @Override
    @Transactional
    public DeliveryAttemptResponse scheduleNextAttempt(
        UUID deliveryId,
        UUID tenantId,
        LocalDateTime nextAttemptAt
    ) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        delivery.setNextAttemptAt(nextAttemptAt);
        deliveryRepository.save(delivery);
        
        // Update last attempt if exists
        attemptRepository.findFirstByDeliveryIdOrderByAttemptNumberDesc(deliveryId)
            .ifPresent(attempt -> {
                attempt.setNextAttemptAt(nextAttemptAt);
                attempt.setNextAttemptScheduled(true);
                attemptRepository.save(attempt);
            });
        
        return attemptRepository.findFirstByDeliveryIdOrderByAttemptNumberDesc(deliveryId)
            .map(this::toResponse)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "No attempts found for delivery"
            ));
    }
    
    private DeliveryAttemptResponse toResponse(DeliveryAttempt attempt) {
        return new DeliveryAttemptResponse(
            attempt.getId(),
            attempt.getDeliveryId(),
            attempt.getFulfillmentId(),
            attempt.getTenantId(),
            attempt.getAttemptNumber(),
            attempt.getAttemptedAt(),
            attempt.getAttemptedByUserId(),
            attempt.getAttemptStatus().name(),
            attempt.getFailureReason(),
            attempt.getFailureCode(),
            attempt.getAttemptLatitude(),
            attempt.getAttemptLongitude(),
            attempt.getAttemptLocationDescription(),
            attempt.getPhotoUrls(),
            attempt.getNotes(),
            attempt.getNextAttemptAt(),
            attempt.getNextAttemptScheduled(),
            attempt.getCreatedAt(),
            attempt.getUpdatedAt()
        );
    }
}

