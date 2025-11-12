package com.nexus.ecommerce.fulfilment.service.impl;

import com.nexus.ecommerce.fulfilment.entity.Delivery;
import com.nexus.ecommerce.fulfilment.entity.ProofOfDelivery;
import com.nexus.ecommerce.fulfilment.model.request.UploadPODRequest;
import com.nexus.ecommerce.fulfilment.model.response.ProofOfDeliveryResponse;
import com.nexus.ecommerce.fulfilment.repository.DeliveryRepository;
import com.nexus.ecommerce.fulfilment.repository.ProofOfDeliveryRepository;
import com.nexus.ecommerce.fulfilment.service.ProofOfDeliveryService;
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
public class ProofOfDeliveryServiceImpl implements ProofOfDeliveryService {
    
    private final ProofOfDeliveryRepository podRepository;
    private final DeliveryRepository deliveryRepository;
    
    @Override
    @Transactional
    public ProofOfDeliveryResponse uploadPOD(
        UUID deliveryId,
        UUID driverId,
        UUID tenantId,
        UploadPODRequest request
    ) {
        log.info("Uploading POD: deliveryId={}, driverId={}, podType={}",
            deliveryId, driverId, request.podType());
        
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        // Check if POD already exists
        ProofOfDelivery pod = podRepository.findByDeliveryId(deliveryId)
            .orElse(ProofOfDelivery.builder()
                .delivery(delivery)
                .deliveryId(deliveryId)
                .fulfillmentId(delivery.getFulfillmentId())
                .tenantId(tenantId)
                .build());
        
        // Update POD based on type
        pod.setPodType(ProofOfDelivery.PODType.valueOf(request.podType()));
        
        if (request.photoUrls() != null && !request.photoUrls().isEmpty()) {
            pod.setPhotoUrls(request.photoUrls());
            pod.setPhotoTakenAt(LocalDateTime.now());
            pod.setPhotoTakenByUserId(driverId);
        }
        
        if (request.signatureData() != null) {
            pod.setSignatureData(request.signatureData());
            pod.setSignatureUrl(request.signatureUrl());
            pod.setSignatureTakenAt(LocalDateTime.now());
            pod.setSignatureTakenByUserId(driverId);
            pod.setRecipientName(request.recipientName());
        }
        
        if (request.videoUrl() != null) {
            pod.setVideoUrl(request.videoUrl());
            pod.setVideoTakenAt(LocalDateTime.now());
        }
        
        pod.setPodLatitude(request.podLatitude());
        pod.setPodLongitude(request.podLongitude());
        pod.setPodLocationDescription(request.podLocationDescription());
        pod.setReceivedByName(request.receivedByName());
        pod.setReceivedByPhone(request.receivedByPhone());
        pod.setReceivedByRelation(request.receivedByRelation());
        pod.setIsAlternateRecipient(request.isAlternateRecipient());
        pod.setAlternateRecipientId(request.alternateRecipientId());
        pod.setPodStatus(ProofOfDelivery.PODStatus.COMPLETED);
        
        ProofOfDelivery saved = podRepository.save(pod);
        
        log.info("POD uploaded: podId={}, deliveryId={}", saved.getId(), deliveryId);
        
        return toResponse(saved);
    }
    
    @Override
    public ProofOfDeliveryResponse getPODByDeliveryId(UUID deliveryId, UUID tenantId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        return podRepository.findByDeliveryId(deliveryId)
            .map(this::toResponse)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "POD not found for delivery: " + deliveryId
            ));
    }
    
    @Override
    @Transactional
    public ProofOfDeliveryResponse verifyOTP(
        UUID deliveryId,
        String otpCode,
        UUID tenantId
    ) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Delivery not found: " + deliveryId
            ));
        
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        ProofOfDelivery pod = podRepository.findByDeliveryId(deliveryId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "POD not found for delivery: " + deliveryId
            ));
        
        // Verify OTP (in real implementation, this would validate against stored OTP)
        if (pod.getOtpCode() != null && pod.getOtpCode().equals(otpCode)) {
            pod.setOtpVerified(true);
            pod.setOtpVerifiedAt(LocalDateTime.now());
            pod.setPodStatus(ProofOfDelivery.PODStatus.COMPLETED);
        } else {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Invalid OTP");
        }
        
        ProofOfDelivery saved = podRepository.save(pod);
        
        return toResponse(saved);
    }
    
    private ProofOfDeliveryResponse toResponse(ProofOfDelivery pod) {
        return new ProofOfDeliveryResponse(
            pod.getId(),
            pod.getDeliveryId(),
            pod.getFulfillmentId(),
            pod.getDeliveryConfirmationId(),
            pod.getTenantId(),
            pod.getPodType().name(),
            pod.getPhotoUrls(),
            pod.getPhotoTakenAt(),
            pod.getPhotoTakenByUserId(),
            pod.getSignatureUrl(),
            pod.getSignatureData(),
            pod.getSignatureTakenAt(),
            pod.getSignatureTakenByUserId(),
            pod.getRecipientName(),
            pod.getOtpVerified(),
            pod.getOtpCode(),
            pod.getOtpVerifiedAt(),
            pod.getOtpPhoneNumber(),
            pod.getVideoUrl(),
            pod.getVideoTakenAt(),
            pod.getPodLatitude(),
            pod.getPodLongitude(),
            pod.getPodLocationDescription(),
            pod.getReceivedByName(),
            pod.getReceivedByPhone(),
            pod.getReceivedByRelation(),
            pod.getIsAlternateRecipient(),
            pod.getAlternateRecipientId(),
            pod.getPodStatus().name(),
            pod.getVerifiedAt(),
            pod.getVerifiedByUserId(),
            pod.getCreatedAt(),
            pod.getUpdatedAt()
        );
    }
}

