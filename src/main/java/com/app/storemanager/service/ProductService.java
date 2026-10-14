package com.app.storemanager.service;

import com.app.storemanager.dto.ProductRequest;
import com.app.storemanager.dto.ProductResponse;

import java.util.List;

/**
 * Defines product-management use cases exposed to the web layer.
 *
 * <p>Implementations are responsible for business rules, transaction
 * boundaries, and conversion between API DTOs and persistence entities.</p>
 */
public interface ProductService {

    /**
     * Creates a product after validating that its SKU is not already used.
     *
     * @param request validated product data
     * @return the persisted product, including its generated ID and timestamps
     * @throws com.app.storemanager.exception.DuplicateSkuException if the SKU is already in use
     */
    ProductResponse createProduct(ProductRequest request);

    /**
     * Retrieves all products.
     *
     * @return products mapped to response DTOs; an empty list when none exist
     */
    List<ProductResponse> getAllProducts();

    /**
     * Retrieves one product by its database ID.
     *
     * @param id product ID
     * @return the matching product as a response DTO
     * @throws com.app.storemanager.exception.ProductNotFoundException if no product has the ID
     */
    ProductResponse getProductById(Long id);

    /**
     * Updates all mutable product fields.
     *
     * <p>The existing SKU may be submitted unchanged. Changing it is allowed
     * only when no other product uses the new SKU; the original creation
     * timestamp is preserved by the entity.</p>
     *
     * @param id ID of the product to update
     * @param request validated replacement data
     * @return the updated product as a response DTO
     * @throws com.app.storemanager.exception.ProductNotFoundException if no product has the ID
     * @throws com.app.storemanager.exception.DuplicateSkuException if another product uses the requested SKU
     */
    ProductResponse updateProduct(Long id, ProductRequest request);

    /**
     * Deletes a product by ID.
     *
     * @param id ID of the product to delete
     * @throws com.app.storemanager.exception.ProductNotFoundException if no product has the ID
     */
    void deleteProduct(Long id);
}
