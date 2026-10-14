package com.app.storemanager.exception;

/**
 * Signals that a product SKU conflicts with an existing product.
 */
public class DuplicateSkuException extends RuntimeException {

    /**
     * Creates an exception for a conflicting SKU.
     *
     * @param sku conflicting SKU
     */
    public DuplicateSkuException(String sku) {
        super("A product with SKU '" + sku + "' already exists");
    }
}
