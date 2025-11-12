package com.nexus.ecommerce.fulfilment.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.ecommerce.fulfilment.entity.AlternateRecipient;
import com.nexus.ecommerce.fulfilment.entity.Delivery;
import com.nexus.ecommerce.fulfilment.model.request.ShareDeliveryLinkRequest;
import com.nexus.ecommerce.fulfilment.model.response.AlternateRecipientResponse;
import com.nexus.ecommerce.fulfilment.model.response.ShareLinkResponse;
import com.nexus.ecommerce.fulfilment.repository.AlternateRecipientRepository;
import com.nexus.ecommerce.fulfilment.repository.DeliveryRepository;
import com.nexus.ecommerce.fulfilment.service.AlternateRecipientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Alternate Recipient Service Implementation
 * Handles sharing delivery links with alternate recipients
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlternateRecipientServiceImpl implements AlternateRecipientService {
    
    private final AlternateRecipientRepository recipientRepository;
    private final DeliveryRepository deliveryRepository;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    private static final int DEFAULT_EXPIRY_HOURS = 24;
    
    @Override
    @Transactional
    public ShareLinkResponse shareDeliveryLink(
        UUID deliveryId,
        UUID customerUserId,
        UUID tenantId,
        ShareDeliveryLinkRequest request
    ) {
        log.info("Sharing delivery link: deliveryId={}, customerUserId={}, recipients={}", 
            deliveryId, customerUserId, request.alternateRecipients().size());
        
        Delivery delivery = deliveryRepository.findById(deliveryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Delivery not found"));
        
        if (!delivery.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Delivery belongs to different tenant");
        }
        
        int expiryHours = request.expiryHours() != null ? request.expiryHours() : DEFAULT_EXPIRY_HOURS;
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expiryHours);
        
        List<ShareLinkResponse.SharedLinkInfo> sharedLinks = request.alternateRecipients().stream()
            .map(recipientInfo -> {
                String shareToken = generateShareToken();
                String shareLink = baseUrl + "/api/v1/public/delivery/share/" + shareToken;
                
                AlternateRecipient recipient = AlternateRecipient.builder()
                    .delivery(delivery)
                    .deliveryId(deliveryId)
                    .tenantId(tenantId)
                    .customerUserId(customerUserId)
                    .alternateUserId(recipientInfo.userId())
                    .alternatePhoneNumber(recipientInfo.phoneNumber())
                    .alternateName(recipientInfo.name())
                    .alternateEmail(recipientInfo.email())
                    .shareToken(shareToken)
                    .shareLink(shareLink)
                    .sharedByUserId(customerUserId)
                    .sharedVia(request.shareMethod())
                    .status(AlternateRecipient.RecipientStatus.ACTIVE)
                    .expiresAt(expiresAt)
                    .build();
                
                recipient = recipientRepository.save(recipient);
                
                // TODO: Send link via SMS/Email/WhatsApp based on shareMethod
                
                return new ShareLinkResponse.SharedLinkInfo(
                    recipient.getId(),
                    recipient.getAlternateName(),
                    recipient.getAlternatePhoneNumber(),
                    recipient.getShareToken(),
                    recipient.getShareLink(),
                    recipient.getSharedVia(),
                    recipient.getStatus().name()
                );
            })
            .collect(Collectors.toList());
        
        log.info("Shared delivery link with {} recipients: deliveryId={}", 
            sharedLinks.size(), deliveryId);
        
        return new ShareLinkResponse(
            deliveryId,
            sharedLinks,
            sharedLinks.size(),
            "Delivery link shared successfully"
        );
    }
    
    @Override
    public AlternateRecipientResponse getByShareToken(String shareToken) {
        AlternateRecipient recipient = recipientRepository.findByShareToken(shareToken)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Invalid share token"));
        
        // Check if expired
        if (recipient.getExpiresAt() != null && recipient.getExpiresAt().isBefore(LocalDateTime.now())) {
            recipient.setStatus(AlternateRecipient.RecipientStatus.EXPIRED);
            recipientRepository.save(recipient);
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Share link has expired");
        }
        
        // Check if revoked
        if (recipient.getStatus() == AlternateRecipient.RecipientStatus.REVOKED) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Share link has been revoked");
        }
        
        return toResponse(recipient);
    }
    
    @Override
    public List<AlternateRecipientResponse> getByDeliveryId(UUID deliveryId, UUID tenantId) {
        List<AlternateRecipient> recipients = recipientRepository.findByDeliveryId(deliveryId);
        return recipients.stream()
            .filter(r -> r.getTenantId().equals(tenantId))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void revokeShareLink(UUID recipientId, UUID userId, UUID tenantId, String reason) {
        AlternateRecipient recipient = recipientRepository.findById(recipientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Recipient not found"));
        
        if (!recipient.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "Recipient belongs to different tenant");
        }
        
        recipient.setStatus(AlternateRecipient.RecipientStatus.REVOKED);
        recipient.setRevokedAt(LocalDateTime.now());
        recipient.setRevokedByUserId(userId);
        recipient.setRevokeReason(reason);
        
        recipientRepository.save(recipient);
        log.info("Revoked share link: recipientId={}, reason={}", recipientId, reason);
    }
    
    @Override
    public List<AlternateRecipientLocation> getActiveRecipientsForProximity(UUID deliveryId) {
        LocalDateTime now = LocalDateTime.now();
        List<AlternateRecipient> active = recipientRepository.findActiveByDeliveryId(deliveryId, now);
        
        return active.stream()
            .filter(r -> r.getConfirmedLatitude() != null && r.getConfirmedLongitude() != null)
            .map(r -> new AlternateRecipientLocation(
                r.getId(),
                r.getConfirmedLatitude(),
                r.getConfirmedLongitude(),
                r.getConfirmedLocationAccuracy(),
                r.getAlternateName(),
                r.getAlternatePhoneNumber()
            ))
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public AlternateRecipientResponse recordConfirmation(
        UUID recipientId,
        double latitude,
        double longitude,
        double locationAccuracy
    ) {
        AlternateRecipient recipient = recipientRepository.findById(recipientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Recipient not found"));
        
        recipient.setConfirmedLatitude(BigDecimal.valueOf(latitude));
        recipient.setConfirmedLongitude(BigDecimal.valueOf(longitude));
        recipient.setConfirmedLocationAccuracy(BigDecimal.valueOf(locationAccuracy));
        recipient.setConfirmedAt(LocalDateTime.now());
        recipient.setConfirmedByUserId(recipient.getAlternateUserId());
        recipient.setStatus(AlternateRecipient.RecipientStatus.CONFIRMED);
        
        recipient = recipientRepository.save(recipient);
        log.info("Alternate recipient confirmed: recipientId={}", recipientId);
        
        return toResponse(recipient);
    }
    
    @Override
    @Transactional
    public void processExpiredLinks() {
        LocalDateTime now = LocalDateTime.now();
        List<AlternateRecipient> expired = recipientRepository.findExpiredRecipients(now);
        
        for (AlternateRecipient recipient : expired) {
            recipient.setStatus(AlternateRecipient.RecipientStatus.EXPIRED);
            recipientRepository.save(recipient);
        }
        
        if (!expired.isEmpty()) {
            log.info("Processed {} expired share links", expired.size());
        }
    }
    
    // Helper methods
    
    private String generateShareToken() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }
    
    private AlternateRecipientResponse toResponse(AlternateRecipient recipient) {
        return new AlternateRecipientResponse(
            recipient.getId(),
            recipient.getDeliveryId(),
            recipient.getTenantId(),
            recipient.getCustomerUserId(),
            recipient.getAlternateUserId(),
            recipient.getAlternatePhoneNumber(),
            recipient.getAlternateName(),
            recipient.getAlternateEmail(),
            recipient.getShareToken(),
            recipient.getShareLink(),
            recipient.getSharedAt(),
            recipient.getSharedByUserId(),
            recipient.getSharedVia(),
            recipient.getStatus().name(),
            recipient.getConfirmedAt(),
            recipient.getConfirmedByUserId(),
            recipient.getConfirmedLatitude(),
            recipient.getConfirmedLongitude(),
            recipient.getConfirmedLocationAccuracy(),
            recipient.getProximityVerified(),
            recipient.getDistanceToAgent(),
            recipient.getExpiresAt(),
            recipient.getRevokedAt(),
            recipient.getRevokedByUserId(),
            recipient.getRevokeReason(),
            recipient.getCreatedAt(),
            recipient.getUpdatedAt()
        );
    }
}

