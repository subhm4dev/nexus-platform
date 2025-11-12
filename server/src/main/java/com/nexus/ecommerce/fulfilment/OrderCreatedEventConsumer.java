package com.nexus.ecommerce.fulfilment.consumer;

import com.nexus.ecommerce.fulfilment.service.FulfillmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Consumer for OrderCreatedEvent
 * Auto-creates fulfillment when an order is created
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventConsumer {
    
    private final FulfillmentService fulfillmentService;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "order-created", groupId = "fulfillment-service-group")
    @Transactional
    public void handleOrderCreated(Map<String, Object> eventMap) {
        try {
            log.info("Received OrderCreated event: {}", eventMap);
            
            // Extract fields from event map
            UUID orderId = UUID.fromString(eventMap.get("order_id").toString());
            UUID tenantId = UUID.fromString(eventMap.get("tenant_id").toString());
            UUID shippingAddressId = UUID.fromString(eventMap.get("shipping_address_id").toString());
            
            log.info("Auto-creating fulfillment: orderId={}, tenantId={}, shippingAddressId={}", 
                orderId, tenantId, shippingAddressId);
            
            // Auto-create fulfillment
            fulfillmentService.createFulfillmentFromOrder(orderId, tenantId, shippingAddressId);
            
            log.info("Fulfillment auto-created for order: orderId={}", orderId);
        } catch (Exception e) {
            log.error("Error processing OrderCreated event: {}", eventMap, e);
            // Don't throw - allow processing to continue for other events
        }
    }
}

