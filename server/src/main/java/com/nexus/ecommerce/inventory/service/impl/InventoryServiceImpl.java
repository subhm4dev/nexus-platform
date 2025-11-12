package com.nexus.ecommerce.inventory.service.impl;

import com.nexus.ecommerce.inventory.entity.Reservation;
import com.nexus.ecommerce.inventory.entity.Stock;
import com.nexus.ecommerce.inventory.entity.StockAdjustment;
import com.nexus.ecommerce.inventory.model.request.BatchStockRequest;
import com.nexus.ecommerce.inventory.model.request.ReservationRequest;
import com.nexus.ecommerce.inventory.model.request.ReleaseReservationRequest;
import com.nexus.ecommerce.inventory.model.request.StockAdjustmentRequest;
import com.nexus.ecommerce.inventory.model.response.StockResponse;
import com.nexus.ecommerce.inventory.repository.ReservationRepository;
import com.nexus.ecommerce.inventory.repository.StockAdjustmentRepository;
import com.nexus.ecommerce.inventory.repository.StockRepository;
import com.nexus.ecommerce.inventory.service.InventoryService;
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
public class InventoryServiceImpl implements InventoryService {

    private final StockRepository stockRepository;
    private final StockAdjustmentRepository adjustmentRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public StockResponse adjustStock(UUID userId, UUID tenantId, List<String> roles, StockAdjustmentRequest request) {
        if (!hasSellerOrAdminRole(roles)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Only SELLER and ADMIN can adjust stock");
        }

        Stock stock = stockRepository.findBySkuAndLocationIdAndTenantIdForUpdate(
            request.sku(), request.locationId(), tenantId
        ).orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Stock not found"));

        int newQty = stock.getQtyOnHand() + request.delta();
        if (newQty < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Insufficient stock. Cannot go negative.");
        }

        stock.setQtyOnHand(newQty);
        Stock saved = stockRepository.save(stock);

        StockAdjustment adjustment = StockAdjustment.builder()
            .stockId(saved.getId())
            .delta(request.delta())
            .reason(request.reason())
            .orderId(request.orderId())
            .userId(userId)
            .timestamp(LocalDateTime.now())
            .build();
        adjustmentRepository.save(adjustment);

        return toResponse(saved);
    }

    @Override
    public StockResponse getStock(String sku, UUID locationId, UUID tenantId) {
        Stock stock = stockRepository.findBySkuAndLocationIdAndTenantId(sku, locationId, tenantId)
            .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Stock not found"));
        return toResponse(stock);
    }

    @Override
    public List<StockResponse> getBatchStock(UUID tenantId, BatchStockRequest request) {
        return request.items().stream()
            .map(item -> {
                try {
                    return getStock(item.sku(), item.locationId(), tenantId);
                } catch (BusinessException e) {
                    return null; // Skip missing stock
                }
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public List<StockResponse> getProductLocations(String sku, UUID tenantId) {
        return stockRepository.findBySkuAndTenantIdAndQtyOnHandGreaterThan(sku, tenantId, 0).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void reserveInventory(UUID userId, UUID tenantId, ReservationRequest request) {
        for (ReservationRequest.ReservationItem item : request.items()) {
            Stock stock = stockRepository.findBySkuAndLocationIdAndTenantIdForUpdate(
                item.sku(), item.locationId(), tenantId
            ).orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Stock not found: " + item.sku()));

            int available = stock.getQtyOnHand() - stock.getReservedQty();
            if (available < item.quantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Insufficient stock for SKU: " + item.sku());
            }

            stock.setReservedQty(stock.getReservedQty() + item.quantity());
            stockRepository.save(stock);

            Reservation reservation = Reservation.builder()
                .orderId(request.orderId())
                .sku(item.sku())
                .locationId(item.locationId())
                .quantity(item.quantity())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .status("PENDING")
                .tenantId(tenantId)
                .build();
            reservationRepository.save(reservation);
        }
    }

    @Override
    @Transactional
    public void releaseReservation(UUID userId, UUID tenantId, ReleaseReservationRequest request) {
        List<Reservation> reservations = reservationRepository.findByOrderIdAndTenantId(request.orderId(), tenantId);
        
        for (Reservation reservation : reservations) {
            if (!"PENDING".equals(reservation.getStatus())) {
                continue;
            }

            Stock stock = stockRepository.findBySkuAndLocationIdAndTenantIdForUpdate(
                reservation.getSku(), reservation.getLocationId(), tenantId
            ).orElse(null);

            if (stock != null) {
                stock.setReservedQty(Math.max(0, stock.getReservedQty() - reservation.getQuantity()));
                stockRepository.save(stock);
            }

            reservation.setStatus("CANCELLED");
            reservationRepository.save(reservation);
        }
    }

    private boolean hasSellerOrAdminRole(List<String> roles) {
        return roles != null && (roles.contains("SELLER") || roles.contains("ADMIN"));
    }

    private StockResponse toResponse(Stock stock) {
        return new StockResponse(
            stock.getId(),
            stock.getSku(),
            stock.getLocationId(),
            stock.getQtyOnHand(),
            stock.getReservedQty(),
            stock.getQtyOnHand() - stock.getReservedQty()
        );
    }
}

