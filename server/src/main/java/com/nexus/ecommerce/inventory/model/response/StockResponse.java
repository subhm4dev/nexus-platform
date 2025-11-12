package com.nexus.ecommerce.inventory.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record StockResponse(
    @JsonProperty("stock_id")
    UUID stockId,
    
    String sku,
    
    @JsonProperty("location_id")
    UUID locationId,
    
    @JsonProperty("qty_on_hand")
    Integer qtyOnHand,
    
    @JsonProperty("reserved_qty")
    Integer reservedQty,
    
    @JsonProperty("available_qty")
    Integer availableQty
) {
}

