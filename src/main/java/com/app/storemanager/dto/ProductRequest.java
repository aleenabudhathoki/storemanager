package com.app.storemanager.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Validated client input for creating or updating a product.
 *
 * <p>Validation rules mirror the product API constraints, including positive
 * prices, non-negative quantities, and maximum lengths for textual fields.</p>
 */
public record ProductRequest(
        @NotBlank(message = "SKU is required")
        @Size(max = 50, message = "SKU must not exceed 50 characters")
        String sku,

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
        BigDecimal price,

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be zero or greater")
        Integer quantity
) {
}
