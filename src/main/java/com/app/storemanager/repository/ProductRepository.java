package com.app.storemanager.repository;

import com.app.storemanager.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for persistent {@link Product} records.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Checks whether any product uses the supplied SKU.
     *
     * @param sku SKU to search for
     * @return {@code true} when the SKU is already in use
     */
    boolean existsBySku(String sku);

    /**
     * Checks whether another product uses the supplied SKU.
     *
     * @param sku SKU to search for
     * @param id ID of the product to exclude from the search
     * @return {@code true} when another product uses the SKU
     */
    boolean existsBySkuAndIdNot(String sku, Long id);
}
