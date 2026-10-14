package com.app.storemanager.exception;

/**
 * Signals that a requested product does not exist.
 */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Creates an exception for a missing product ID.
     *
     * @param id missing product ID
     */
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
