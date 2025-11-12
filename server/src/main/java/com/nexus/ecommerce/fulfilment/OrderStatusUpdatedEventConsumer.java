package com.nexus.ecommerce.fulfilment.consumer;

import com.nexus.ecommerce.fulfilment.entity.Fulfillment;
import com.nexus.ecommerce.fulfilment.repository.FulfillmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Consumer for OrderStatusUpdatedEvent
 * Updates fulfillment status based on order status changes
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusUpdatedEventConsumer {
    
    private final FulfillmentRepository fulfillmentRepository;
    
    @KafkaListener(topics = "order-status-updated", groupId = "fulfillment-service-group")
    @Transactional
    public void handleOrderStatusUpdated(Map<String, Object> eventMap) {
        try {
            log.info("Received OrderStatusUpdated event: {}", eventMap);
            
            UUID orderId = UUID.fromString(eventMap.get("order_id").toString());
            String status = eventMap.get("status").toString();
            UUID tenantId = UUID.fromString(eventMap.get("tenant_id").toString());
            
            log.info("Updating fulfillment status: orderId={}, orderStatus={}", orderId, status);
            
            // Find fulfillment for this order
            fulfillmentRepository.findByOrderIdAndTenantId(orderId, tenantId)
                .ifPresentOrElse(
                    fulfillment -> {
                        // Map order status to fulfillment status
                        Fulfillment.FulfillmentStatus fulfillmentStatus = mapOrderStatusToFulfillmentStatus(status);
                        
                        if (fulfillmentStatus != null && fulfillment.getStatus() != fulfillmentStatus) {
                            fulfillment.setStatus(fulfillmentStatus);
                            fulfillment.setUpdatedAt(LocalDateTime.now());
                            
                            if (fulfillmentStatus == Fulfillment.FulfillmentStatus.DELIVERED) {
                                fulfillment.setActualDelivery(LocalDateTime.now());
                            }
                            
                            fulfillmentRepository.save(fulfillment);
                            log.info("Fulfillment status updated: fulfillmentId={}, status={}", 
                                fulfillment.getId(), fulfillmentStatus);
                        }
                    },
                    () -> log.warn("Fulfillment not found for order: orderId={}", orderId)
                );
        } catch (Exception e) {
            log.error("Error processing OrderStatusUpdated event: {}", eventMap, e);
            // Don't throw - allow processing to continue for other events
        }
    }
    
    private Fulfillment.FulfillmentStatus mapOrderStatusToFulfillmentStatus(String orderStatus) {
        return switch (orderStatus) {
            case "SHIPPED" -> Fulfillment.FulfillmentStatus.IN_TRANSIT;
            case "DELIVERED" -> Fulfillment.FulfillmentStatus.DELIVERED;
            case "CANCELLED" -> Fulfillment.FulfillmentStatus.CANCELLED;
            default -> null; // No mapping for other statuses
        };
    }
}

