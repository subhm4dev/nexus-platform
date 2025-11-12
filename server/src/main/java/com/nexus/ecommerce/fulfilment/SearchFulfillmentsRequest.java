package com.nexus.ecommerce.fulfilment.model.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request for searching fulfillments
 */
public record SearchFulfillmentsRequest(
    String query, // Search term (order ID, tracking number, customer name)
    List<String> statuses, // Filter by status
    LocalDate startDate,
    LocalDate endDate,
    UUID driverId,
    UUID providerId,
    String priority, // URGENT, HIGH, NORMAL, LOW
    Integer page,
    Integer size,
    String sortBy, // created_at, status, priority
    String sortDirection // ASC, DESC
) {}

