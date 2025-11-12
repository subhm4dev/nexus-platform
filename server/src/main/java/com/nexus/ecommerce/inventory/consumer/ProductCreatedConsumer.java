package com.nexus.ecommerce.inventory.consumer;

import com.nexus.ecommerce.inventory.entity.Location;
import com.nexus.ecommerce.inventory.entity.Stock;
import com.nexus.ecommerce.inventory.model.event.ProductCreatedEvent;
import com.nexus.ecommerce.inventory.repository.LocationRepository;
import com.nexus.ecommerce.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCreatedConsumer {

    private final StockRepository stockRepository;
    private final LocationRepository locationRepository;

    @KafkaListener(topics = "product-created", groupId = "inventory-service-group")
    @Transactional
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("Received ProductCreated event: productId={}, sku={}, tenantId={}", 
            event.productId(), event.sku(), event.tenantId());

        // Get all active locations for this tenant
        List<Location> locations = locationRepository.findByTenantIdAndActiveTrue(event.tenantId());

        // Create stock records for each location
        for (Location location : locations) {
            Stock stock = Stock.builder()
                .sku(event.sku())
                .locationId(location.getId())
                .qtyOnHand(0)
                .reservedQty(0)
                .tenantId(event.tenantId())
                .build();
            stockRepository.save(stock);
            log.info("Created stock record for SKU {} at location {}", event.sku(), location.getName());
        }
    }
}

