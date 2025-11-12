package com.nexus.ecommerce.fulfilment.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.ecommerce.fulfilment.entity.Delivery;
import com.nexus.ecommerce.fulfilment.entity.Fulfillment;
import com.nexus.ecommerce.fulfilment.event.FulfillmentCreatedEvent;
import com.nexus.ecommerce.fulfilment.model.request.AssignDriverRequest;
import com.nexus.ecommerce.fulfilment.model.request.CreateFulfillmentRequest;
import com.nexus.ecommerce.fulfilment.model.request.UpdateFulfillmentStatusRequest;
import com.nexus.ecommerce.fulfilment.model.response.DeliveryResponse;
import com.nexus.ecommerce.fulfilment.model.response.FulfillmentResponse;
import com.nexus.ecommerce.fulfilment.repository.DeliveryRepository;
import com.nexus.ecommerce.fulfilment.repository.DriverRepository;
import com.nexus.ecommerce.fulfilment.repository.FulfillmentRepository;
import com.nexus.ecommerce.fulfilment.service.DeliveryService;
import com.nexus.ecommerce.fulfilment.service.FulfillmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fulfillment Service Implementation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FulfillmentServiceImpl implements FulfillmentService {
    
    private final FulfillmentRepository fulfillmentRepository;
    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;
    private final DeliveryService deliveryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String FULFILLMENT_CREATED_TOPIC = "fulfillment-created";
    
    @Override
    @Transactional
    public FulfillmentResponse createFulfillment(UUID tenantId, CreateFulfillmentRequest request) {
        log.info("Creating fulfillment: orderId={}, tenantId={}", request.orderId(), tenantId);
        
        // Check if fulfillment already exists for this order
        fulfillmentRepository.findByOrderIdAndTenantId(request.orderId(), tenantId)
            .ifPresent(f -> {
                throw new BusinessException(
                    ErrorCode.INVALID_OPERATION,
                    "Fulfillment already exists for order: " + request.orderId()
                );
            });
        
        // Create fulfillment
        Fulfillment fulfillment = Fulfillment.builder()
            .orderId(request.orderId())
            .tenantId(tenantId)
            .status(Fulfillment.FulfillmentStatus.PENDING)
            .pickupLocation(request.pickupLocation())
            .deliveryAddressId(request.deliveryAddressId())
            .estimatedDelivery(request.estimatedDelivery())
            .createdAt(LocalDateTime.now())
            .build();
        
        Fulfillment savedFulfillment = fulfillmentRepository.save(fulfillment);
        
        log.info("Fulfillment created: fulfillmentId={}, orderId={}", savedFulfillment.getId(), request.orderId());
        
        // Publish FulfillmentCreated event
        try {
            FulfillmentCreatedEvent event = FulfillmentCreatedEvent.of(
                savedFulfillment.getId(),
                savedFulfillment.getOrderId(),
                savedFulfillment.getTenantId(),
                savedFulfillment.getStatus(),
                savedFulfillment.getDeliveryAddressId(),
                savedFulfillment.getCreatedAt()
            );
            kafkaTemplate.send(FULFILLMENT_CREATED_TOPIC, savedFulfillment.getId().toString(), event);
            log.info("Published FulfillmentCreated event: fulfillmentId={}", savedFulfillment.getId());
        } catch (Exception e) {
            log.error("Failed to publish FulfillmentCreated event: fulfillmentId={}", savedFulfillment.getId(), e);
        }
        
        return toResponse(savedFulfillment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public FulfillmentResponse getFulfillmentById(UUID fulfillmentId, UUID tenantId, List<String> userRoles) {
        log.debug("Getting fulfillment: fulfillmentId={}", fulfillmentId);
        
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Fulfillment not found: " + fulfillmentId
            ));
        
        // Verify tenant
        if (!fulfillment.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Fulfillment belongs to different tenant"
            );
        }
        
        // TODO: Add access control check based on order ownership
        
        return toResponse(fulfillment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public FulfillmentResponse getFulfillmentByOrderId(UUID orderId, UUID tenantId, List<String> userRoles) {
        log.debug("Getting fulfillment by orderId: orderId={}", orderId);
        
        Fulfillment fulfillment = fulfillmentRepository.findByOrderIdAndTenantId(orderId, tenantId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Fulfillment not found for order: " + orderId
            ));
        
        return toResponse(fulfillment);
    }
    
    @Override
    @Transactional
    public FulfillmentResponse assignDriver(UUID fulfillmentId, UUID tenantId, List<String> userRoles, AssignDriverRequest request) {
        log.info("Assigning driver to fulfillment: fulfillmentId={}, driverId={}", fulfillmentId, request.driverId());
        
        // Check authorization (ADMIN/STAFF only)
        boolean isAuthorized = userRoles != null && (
            userRoles.contains("ADMIN") || 
            userRoles.contains("STAFF")
        );
        
        if (!isAuthorized) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Only ADMIN or STAFF can assign drivers"
            );
        }
        
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Fulfillment not found: " + fulfillmentId
            ));
        
        // Verify tenant
        if (!fulfillment.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Fulfillment belongs to different tenant"
            );
        }
        
        // Verify driver exists and is available
        driverRepository.findById(request.driverId())
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Driver not found: " + request.driverId()
            ));
        
        fulfillment.setAssignedDriverId(request.driverId());
        fulfillment.setStatus(Fulfillment.FulfillmentStatus.ASSIGNED);
        fulfillment.setUpdatedAt(LocalDateTime.now());
        
        Fulfillment savedFulfillment = fulfillmentRepository.save(fulfillment);
        
        log.info("Driver assigned: fulfillmentId={}, driverId={}", fulfillmentId, request.driverId());
        
        return toResponse(savedFulfillment);
    }
    
    @Override
    @Transactional
    public FulfillmentResponse assignProvider(
        UUID fulfillmentId, 
        UUID tenantId, 
        List<String> userRoles, 
        String providerCode,
        boolean isIntercity
    ) {
        log.info("Assigning provider to fulfillment: fulfillmentId={}, providerCode={}, isIntercity={}", 
            fulfillmentId, providerCode, isIntercity);
        
        // Check authorization (ADMIN/STAFF only)
        boolean isAuthorized = userRoles != null && (
            userRoles.contains("ADMIN") || 
            userRoles.contains("STAFF")
        );
        
        if (!isAuthorized) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Only ADMIN or STAFF can assign providers"
            );
        }
        
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Fulfillment not found: " + fulfillmentId
            ));
        
        // Verify tenant
        if (!fulfillment.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Fulfillment belongs to different tenant"
            );
        }
        
        // Create delivery with provider
        deliveryService.createDeliveryWithProvider(fulfillmentId, tenantId, providerCode, isIntercity);
        
        // Update fulfillment status
        fulfillment.setStatus(Fulfillment.FulfillmentStatus.ASSIGNED);
        fulfillment.setUpdatedAt(LocalDateTime.now());
        
        Fulfillment savedFulfillment = fulfillmentRepository.save(fulfillment);
        
        log.info("Provider assigned: fulfillmentId={}, providerCode={}", fulfillmentId, providerCode);
        
        return toResponse(savedFulfillment);
    }
    
    @Override
    @Transactional
    public FulfillmentResponse updateStatus(UUID fulfillmentId, UUID tenantId, List<String> userRoles, UpdateFulfillmentStatusRequest request) {
        log.info("Updating fulfillment status: fulfillmentId={}, newStatus={}", fulfillmentId, request.status());
        
        Fulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
            .orElseThrow(() -> new BusinessException(
                ErrorCode.RESOURCE_NOT_FOUND,
                "Fulfillment not found: " + fulfillmentId
            ));
        
        // Verify tenant
        if (!fulfillment.getTenantId().equals(tenantId)) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Fulfillment belongs to different tenant"
            );
        }
        
        // Check authorization (ADMIN/STAFF/DRIVER)
        boolean isAuthorized = userRoles != null && (
            userRoles.contains("ADMIN") || 
            userRoles.contains("STAFF") ||
            userRoles.contains("DRIVER")
        );
        
        if (!isAuthorized) {
            throw new BusinessException(
                ErrorCode.ACCESS_DENIED,
                "Only ADMIN, STAFF, or DRIVER can update fulfillment status"
            );
        }
        
        fulfillment.setStatus(request.status());
        fulfillment.setUpdatedAt(LocalDateTime.now());
        
        if (request.status() == Fulfillment.FulfillmentStatus.DELIVERED) {
            fulfillment.setActualDelivery(LocalDateTime.now());
        }
        
        Fulfillment savedFulfillment = fulfillmentRepository.save(fulfillment);
        
        log.info("Fulfillment status updated: fulfillmentId={}, status={}", fulfillmentId, request.status());
        
        return toResponse(savedFulfillment);
    }
    
    @Override
    @Transactional
    public void createFulfillmentFromOrder(UUID orderId, UUID tenantId, UUID deliveryAddressId) {
        log.info("Auto-creating fulfillment from order: orderId={}, tenantId={}", orderId, tenantId);
        
        // Check if fulfillment already exists
        if (fulfillmentRepository.findByOrderIdAndTenantId(orderId, tenantId).isPresent()) {
            log.debug("Fulfillment already exists for order: {}", orderId);
            return;
        }
        
        // Create fulfillment
        Fulfillment fulfillment = Fulfillment.builder()
            .orderId(orderId)
            .tenantId(tenantId)
            .status(Fulfillment.FulfillmentStatus.PENDING)
            .deliveryAddressId(deliveryAddressId)
            .createdAt(LocalDateTime.now())
            .build();
        
        fulfillmentRepository.save(fulfillment);
        
        log.info("Auto-created fulfillment: fulfillmentId={}, orderId={}", fulfillment.getId(), orderId);
    }
    
    @Override
    public boolean canAccessFulfillment(UUID currentUserId, UUID fulfillmentUserId, List<String> userRoles) {
        // Admin/Staff can access any fulfillment
        if (userRoles != null && (userRoles.contains("ADMIN") || userRoles.contains("STAFF"))) {
            return true;
        }
        
        // Users can access their own fulfillments (via order ownership)
        return currentUserId.equals(fulfillmentUserId);
    }
    
    private FulfillmentResponse toResponse(Fulfillment fulfillment) {
        List<DeliveryResponse> deliveries = fulfillment.getDeliveries().stream()
            .map(this::toDeliveryResponse)
            .collect(Collectors.toList());
        
        return new FulfillmentResponse(
            fulfillment.getId(),
            fulfillment.getOrderId(),
            fulfillment.getTenantId(),
            fulfillment.getStatus(),
            fulfillment.getAssignedDriverId(),
            fulfillment.getPickupLocation(),
            fulfillment.getDeliveryAddressId(),
            fulfillment.getEstimatedDelivery(),
            fulfillment.getActualDelivery(),
            deliveries,
            fulfillment.getCreatedAt(),
            fulfillment.getUpdatedAt()
        );
    }
    
    private DeliveryResponse toDeliveryResponse(Delivery delivery) {
        return new DeliveryResponse(
            delivery.getId(),
            delivery.getFulfillmentId(),
            delivery.getDriverId(),
            delivery.getTenantId(),
            delivery.getCurrentLocation(),
            delivery.getLatitude(),
            delivery.getLongitude(),
            delivery.getStatus(),
            delivery.getTrackingNumber(),
            delivery.getTrackingHistory().stream()
                .map(th -> new com.nexus.ecommerce.fulfilment.model.response.TrackingHistoryResponse(
                    th.getId(),
                    th.getDeliveryId(),
                    th.getLatitude(),
                    th.getLongitude(),
                    th.getLocationDescription(),
                    th.getStatus(),
                    th.getUpdatedBy(),
                    th.getCreatedAt()
                ))
                .collect(Collectors.toList()),
            delivery.getCreatedAt(),
            delivery.getUpdatedAt()
        );
    }
}

