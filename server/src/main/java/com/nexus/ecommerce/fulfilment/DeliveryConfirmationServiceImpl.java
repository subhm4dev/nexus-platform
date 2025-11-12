package com.nexus.ecommerce.fulfilment.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.ecommerce.fulfilment.entity.Delivery;
import com.nexus.ecommerce.fulfilment.entity.DeliveryConfirmation;
import com.nexus.ecommerce.fulfilment.model.request.ConfirmDeliveryRequest;
import com.nexus.ecommerce.fulfilment.model.response.DeliveryConfirmationResponse;
import com.nexus.ecommerce.fulfilment.repository.AgeVerificationRepository;
import com.nexus.ecommerce.fulfilment.repository.DeliveryConfirmationRepository;
import com.nexus.ecommerce.fulfilment.repository.DeliveryRepository;
import com.nexus.ecommerce.fulfilment.service.DeliveryConfirmationService;
import com.nexus.ecommerce.fulfilment.service.ProximityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Delivery Confirmation Service Implementation
 * Handles dual-confirmation delivery system with proximity verification
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryConfirmationServiceImpl implements DeliveryConfirmationService {
    
    private final DeliveryConfirmationRepository confirmationRepository;
    private final DeliveryRepository deliveryRepository;
    private final ProximityService proximityService;
    private final AgeVerificationRepository ageVerificationRepository;
    
    private static final int DEFAULT_PROXIMITY_RADIUS = 50; // meters
    private static final int CONFIRMATION_TIMEOUT_MINUTES = 5;
    private static final int MAX_RESCHEDULE_COUNT = 3;
    
    @Override
    @Transactional
    public DeliveryConfirmationResponse agentConfirmDelivery(
        UUID deliveryId,
        UUID agentUserId,
        UUID tenantId,
        ConfirmDeliveryRequest request
    ) {
        log.info("Agent confirming delivery: deliveryId={}, agentUserId={}", deliveryId, agentUserId);
        
        // Validate location accuracy
        if (!proximityService.isValidLocationAccuracy(request.locationAccuracy().doubleValue())) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Location accuracy must be <= " + proximityService.getMaxLocationAccuracy() + " meters"
            );
        }
        
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        // Verify tenant
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Delivery belongs to different tenant");
        }
        
        // Get or create confirmation
        DeliveryConfirmation confirmation = confirmationRepository
            .findByDeliveryId(deliveryId)
            .orElseGet(() -> createNewConfirmation(delivery, tenantId));
        
        // Update agent confirmation
        confirmation.setAgentConfirmed(true);
        confirmation.setAgentConfirmedAt(LocalDateTime.now());
        confirmation.setAgentLatitude(request.latitude());
        confirmation.setAgentLongitude(request.longitude());
        confirmation.setAgentLocationAccuracy(request.locationAccuracy());
        confirmation.setAgentUserId(agentUserId);
        
        // Check if customer already confirmed
        if (Boolean.TRUE.equals(confirmation.getCustomerConfirmed())) {
            // Both confirmed - verify proximity
            verifyAndCompleteDelivery(confirmation, delivery, request);
        } else {
            // Wait for customer confirmation
            confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.AGENT_CONFIRMED);
        }
        
        confirmation = confirmationRepository.save(confirmation);
        
        log.info("Agent confirmed delivery: deliveryId={}, status={}", 
            deliveryId, confirmation.getConfirmationStatus());
        
        return toResponse(confirmation, false, 0, false);
    }
    
    @Override
    @Transactional
    public DeliveryConfirmationResponse customerConfirmDelivery(
        UUID deliveryId,
        UUID customerUserId,
        UUID tenantId,
        ConfirmDeliveryRequest request
    ) {
        log.info("Customer confirming delivery: deliveryId={}, customerUserId={}", deliveryId, customerUserId);
        
        // Similar logic to agent confirmation
        if (!proximityService.isValidLocationAccuracy(request.locationAccuracy().doubleValue())) {
            throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "Location accuracy must be <= " + proximityService.getMaxLocationAccuracy() + " meters"
            );
        }
        
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Delivery belongs to different tenant");
        }
        
        DeliveryConfirmation confirmation = confirmationRepository
            .findByDeliveryId(deliveryId)
            .orElseGet(() -> createNewConfirmation(delivery, tenantId));
        
        confirmation.setCustomerConfirmed(true);
        confirmation.setCustomerConfirmedAt(LocalDateTime.now());
        confirmation.setCustomerLatitude(request.latitude());
        confirmation.setCustomerLongitude(request.longitude());
        confirmation.setCustomerLocationAccuracy(request.locationAccuracy());
        confirmation.setCustomerUserId(customerUserId);
        
        if (Boolean.TRUE.equals(confirmation.getAgentConfirmed())) {
            verifyAndCompleteDelivery(confirmation, delivery, request);
        } else {
            confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.CUSTOMER_CONFIRMED);
        }
        
        confirmation = confirmationRepository.save(confirmation);
        
        return toResponse(confirmation, false, 0, false);
    }
    
    @Override
    @Transactional
    public DeliveryConfirmationResponse agentMarkUnavailable(
        UUID deliveryId,
        UUID agentUserId,
        UUID tenantId,
        ConfirmDeliveryRequest request,
        String reason
    ) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Delivery not found"));
        
        DeliveryConfirmation confirmation = confirmationRepository
            .findByDeliveryId(deliveryId)
            .orElseGet(() -> createNewConfirmation(delivery, tenantId));
        
        confirmation.setAgentMarkedUnavailable(true);
        confirmation.setAgentUnavailableAt(LocalDateTime.now());
        confirmation.setAgentUnavailableReason(reason);
        
        if (Boolean.TRUE.equals(confirmation.getCustomerMarkedUnavailable())) {
            handleBothUnavailable(confirmation, delivery);
        } else {
            confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.AGENT_UNAVAILABLE);
        }
        
        confirmation = confirmationRepository.save(confirmation);
        return toResponse(confirmation, false, 0, false);
    }
    
    @Override
    @Transactional
    public DeliveryConfirmationResponse customerMarkUnavailable(
        UUID deliveryId,
        UUID customerUserId,
        UUID tenantId,
        ConfirmDeliveryRequest request,
        String reason
    ) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Delivery not found"));
        
        DeliveryConfirmation confirmation = confirmationRepository
            .findByDeliveryId(deliveryId)
            .orElseGet(() -> createNewConfirmation(delivery, tenantId));
        
        confirmation.setCustomerMarkedUnavailable(true);
        confirmation.setCustomerUnavailableAt(LocalDateTime.now());
        confirmation.setCustomerUnavailableReason(reason);
        
        if (Boolean.TRUE.equals(confirmation.getAgentMarkedUnavailable())) {
            handleBothUnavailable(confirmation, delivery);
        } else {
            confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.CUSTOMER_UNAVAILABLE);
        }
        
        confirmation = confirmationRepository.save(confirmation);
        return toResponse(confirmation, false, 0, false);
    }
    
    @Override
    public DeliveryConfirmationResponse getConfirmationStatus(
        UUID deliveryId,
        UUID userId,
        UUID tenantId,
        boolean isAgent
    ) {
        DeliveryConfirmation confirmation = confirmationRepository
            .findByDeliveryIdAndTenantId(deliveryId, tenantId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Confirmation not found"));
        
        // Calculate time remaining, can confirm, etc.
        boolean canConfirm = !isAgent ? 
            !Boolean.TRUE.equals(confirmation.getCustomerConfirmed()) :
            !Boolean.TRUE.equals(confirmation.getAgentConfirmed());
        
        int timeRemaining = calculateTimeRemaining(confirmation);
        boolean isInProximity = false; // Would need current location to calculate
        
        return toResponse(confirmation, canConfirm, timeRemaining, isInProximity);
    }
    
    @Override
    @Transactional
    public void processReschedules(UUID tenantId) {
        LocalDateTime now = LocalDateTime.now();
        var pending = confirmationRepository.findPendingConfirmationsForReschedule(tenantId, now);
        
        for (DeliveryConfirmation conf : pending) {
            if (conf.getRescheduleCount() < MAX_RESCHEDULE_COUNT) {
                conf.setRescheduleCount(conf.getRescheduleCount() + 1);
                conf.setLastRescheduleAt(now);
                conf.setNextAttemptAt(now.plusDays(1));
                conf.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.PENDING);
                confirmationRepository.save(conf);
                log.info("Rescheduled delivery: deliveryId={}, attempt={}", 
                    conf.getDeliveryId(), conf.getRescheduleCount());
            }
        }
    }
    
    @Override
    @Transactional
    public void processAutoReturns(UUID tenantId) {
        var forReturn = confirmationRepository.findDeliveriesForAutoReturn(tenantId);
        
        for (DeliveryConfirmation conf : forReturn) {
            conf.setAutoReturnInitiated(true);
            conf.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.RETURNED);
            confirmationRepository.save(conf);
            
            // Update delivery status
            Delivery delivery = deliveryRepository.findById(conf.getDeliveryId())
                .orElse(null);
            if (delivery != null) {
                delivery.setStatus(Delivery.DeliveryStatus.RETURNED);
                deliveryRepository.save(delivery);
            }
            
            log.info("Auto-returned delivery: deliveryId={}", conf.getDeliveryId());
        }
    }
    
    // Helper methods
    
    private DeliveryConfirmation createNewConfirmation(Delivery delivery, UUID tenantId) {
        return DeliveryConfirmation.builder()
            .delivery(delivery)
            .deliveryId(delivery.getId())
            .tenantId(tenantId)
            .confirmationStatus(DeliveryConfirmation.ConfirmationStatus.PENDING)
            .build();
    }
    
    private void verifyAndCompleteDelivery(
        DeliveryConfirmation confirmation,
        Delivery delivery,
        ConfirmDeliveryRequest request
    ) {
        // Get delivery address coordinates (would need to fetch from address service)
        // For now, use agent and customer locations
        double agentLat = confirmation.getAgentLatitude().doubleValue();
        double agentLon = confirmation.getAgentLongitude().doubleValue();
        double customerLat = confirmation.getCustomerLatitude().doubleValue();
        double customerLon = confirmation.getCustomerLongitude().doubleValue();
        
        // Use delivery address if available, otherwise use customer location as reference
        double deliveryLat = delivery.getDeliveryAddressLatitude() != null ?
            delivery.getDeliveryAddressLatitude().doubleValue() : customerLat;
        double deliveryLon = delivery.getDeliveryAddressLongitude() != null ?
            delivery.getDeliveryAddressLongitude().doubleValue() : customerLon;
        
        int proximityRadius = delivery.getProximityRadiusMeters() != null ?
            delivery.getProximityRadiusMeters() : DEFAULT_PROXIMITY_RADIUS;
        
        // Verify proximity between parties (flexible location)
        ProximityService.ProximityResult result = proximityService.verifyAndRecordLocation(
            agentLat, agentLon,
            customerLat, customerLon,
            deliveryLat, deliveryLon,
            proximityRadius
        );
        
        if (result.partiesInProximity()) {
            // Check age verification if required
            if (Boolean.TRUE.equals(confirmation.getRequiresAgeVerification())) {
                // Check if age is verified (for customer OR alternate recipient)
                boolean ageVerified = checkAgeVerification(confirmation);
                
                if (!ageVerified) {
                    // Age not verified - block delivery
                    confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.CONFLICT);
                    log.warn("Delivery blocked: age verification required but not completed. deliveryId={}", 
                        delivery.getId());
                    return;
                }
            }
            
            // Both confirmed and in proximity - DELIVERED
            confirmation.setProximityVerified(true);
            confirmation.setDistanceBetweenParties(result.distanceBetweenParties());
            confirmation.setDistanceToDeliveryAddress(result.distanceFromScheduled());
            confirmation.setProximityVerifiedAt(LocalDateTime.now());
            confirmation.setActualDeliveryLatitude(result.actualDeliveryLat());
            confirmation.setActualDeliveryLongitude(result.actualDeliveryLon());
            confirmation.setDeliveryLocationType(result.locationType());
            confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.BOTH_CONFIRMED);
            
            // Update delivery status
            delivery.setStatus(Delivery.DeliveryStatus.DELIVERED);
            deliveryRepository.save(delivery);
            
            log.info("Delivery completed: deliveryId={}, distance={}m", 
                delivery.getId(), result.distanceBetweenParties());
        } else {
            // Both confirmed but not in proximity - CONFLICT
            confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.CONFLICT);
            log.warn("Delivery conflict: parties not in proximity, distance={}m", 
                result.distanceBetweenParties());
        }
    }
    
    private void handleBothUnavailable(DeliveryConfirmation confirmation, Delivery delivery) {
        int rescheduleCount = confirmation.getRescheduleCount();
        
        if (rescheduleCount < MAX_RESCHEDULE_COUNT) {
            // Reschedule
            confirmation.setRescheduleCount(rescheduleCount + 1);
            confirmation.setLastRescheduleAt(LocalDateTime.now());
            confirmation.setNextAttemptAt(LocalDateTime.now().plusDays(1));
            confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.BOTH_UNAVAILABLE);
            log.info("Rescheduling delivery: deliveryId={}, attempt={}", 
                delivery.getId(), rescheduleCount + 1);
        } else {
            // Auto-return
            confirmation.setAutoReturnInitiated(true);
            confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.RETURNED);
            delivery.setStatus(Delivery.DeliveryStatus.RETURNED);
            deliveryRepository.save(delivery);
            log.info("Auto-returning delivery after {} attempts: deliveryId={}", 
                rescheduleCount, delivery.getId());
        }
    }
    
    private int calculateTimeRemaining(DeliveryConfirmation confirmation) {
        if (confirmation.getAgentConfirmedAt() != null) {
            LocalDateTime expiry = confirmation.getAgentConfirmedAt()
                .plusMinutes(CONFIRMATION_TIMEOUT_MINUTES);
            long seconds = java.time.Duration.between(LocalDateTime.now(), expiry).getSeconds();
            return (int) Math.max(0, seconds);
        }
        return 0;
    }
    
    /**
     * Check if age verification is completed for the delivery
     * Supports both customer and alternate recipient verification
     */
    private boolean checkAgeVerification(DeliveryConfirmation confirmation) {
        // Check if age verification exists and is verified
        return ageVerificationRepository.findVerifiedByConfirmationId(confirmation.getId())
            .map(av -> Boolean.TRUE.equals(av.getAgeVerified()) && 
                 av.getVerificationStatus() == com.nexus.ecommerce.fulfilment.entity.AgeVerification.VerificationStatus.VERIFIED)
            .orElse(false);
    }
    
    private DeliveryConfirmationResponse toResponse(
        DeliveryConfirmation confirmation,
        boolean canConfirm,
        int timeRemaining,
        boolean isInProximity
    ) {
        return DeliveryConfirmationResponse.from(
            confirmation,
            canConfirm,
            timeRemaining,
            isInProximity
        );
    }
}

