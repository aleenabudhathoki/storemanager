package com.app.storemanager.controller;

import com.app.storemanager.dto.ProductRequest;
import com.app.storemanager.dto.ProductResponse;
import com.app.storemanager.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * HTTP controller for product CRUD operations.
 *
 * <p>The controller validates request bodies and delegates business rules to
 * {@link ProductService}; it does not expose JPA entities directly.</p>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Creates a product and returns its resource location.
     *
     * @param request validated product input
     * @return {@code 201 Created} with the saved product
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.created(URI.create("/api/products/" + response.id())).body(response);
    }

    /**
     * Retrieves all products.
     *
     * @return {@code 200 OK} containing product response DTOs
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Retrieves a product by ID.
     *
     * @param id product ID from the request path
     * @return {@code 200 OK} with the product, or an error response when absent
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Replaces the mutable fields of an existing product.
     *
     * @param id product ID from the request path
     * @param request validated replacement data
     * @return {@code 200 OK} with the updated product
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * Deletes a product by ID.
     *
     * @param id product ID from the request path
     * @return {@code 204 No Content} after successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
