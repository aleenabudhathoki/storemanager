package com.app.storemanager.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Read-only product representation returned by the REST API.
 *
 * <p>This DTO intentionally separates the API contract from the JPA entity.</p>
 */
public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
