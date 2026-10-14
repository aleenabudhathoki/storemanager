package com.app.storemanager.service;

import com.app.storemanager.dto.ProductRequest;
import com.app.storemanager.dto.ProductResponse;
import com.app.storemanager.entity.Product;
import com.app.storemanager.exception.DuplicateSkuException;
import com.app.storemanager.exception.ProductNotFoundException;
import com.app.storemanager.repository.ProductRepository;
import com.app.storemanager.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    void createProductReturnsSavedProduct() {
        ProductRequest request = request("COF-001");
        Product product = product(request);
        when(productRepository.existsBySku("COF-001")).thenReturn(false);
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertEquals("COF-001", response.sku());
        assertEquals("Arabica Coffee Beans", response.name());
        verify(productRepository).saveAndFlush(any(Product.class));
    }

    @Test
    void createProductRejectsDuplicateSku() {
        ProductRequest request = request("COF-001");
        when(productRepository.existsBySku("COF-001")).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void getProductByIdReturnsExistingProduct() {
        Product product = product(request("COF-001"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertEquals("COF-001", response.sku());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductByIdRejectsMissingProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void updateProductUpdatesExistingProduct() {
        Product product = product(request("COF-001"));
        ProductRequest update = new ProductRequest(
                "COF-002", "Updated Coffee", "Updated description", new BigDecimal("24.99"), 25
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot("COF-002", 1L)).thenReturn(false);
        when(productRepository.saveAndFlush(product)).thenReturn(product);

        ProductResponse response = productService.updateProduct(1L, update);

        assertEquals("COF-002", response.sku());
        assertEquals("Updated Coffee", response.name());
        assertEquals(new BigDecimal("24.99"), response.price());
    }

    @Test
    void updateProductAllowsUnchangedSku() {
        Product product = product(request("COF-001"));
        ProductRequest update = new ProductRequest(
                "COF-001", "Updated Coffee", "Updated description", new BigDecimal("24.99"), 25
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.saveAndFlush(product)).thenReturn(product);

        productService.updateProduct(1L, update);

        verify(productRepository, never()).existsBySkuAndIdNot(any(String.class), any(Long.class));
        verify(productRepository).saveAndFlush(product);
    }

    @Test
    void updateProductRejectsConflictingSku() {
        Product product = product(request("COF-001"));
        ProductRequest update = new ProductRequest(
                "COF-002", "Updated Coffee", "Updated description", new BigDecimal("24.99"), 25
        );
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuAndIdNot("COF-002", 1L)).thenReturn(true);

        assertThrows(DuplicateSkuException.class, () -> productService.updateProduct(1L, update));
        verify(productRepository, never()).saveAndFlush(any(Product.class));
    }

    @Test
    void deleteProductDeletesExistingProduct() {
        Product product = product(request("COF-001"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProductRejectsMissingProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.deleteProduct(99L));
        verify(productRepository, never()).delete(any(Product.class));
    }

    private ProductRequest request(String sku) {
        return new ProductRequest(
                sku, "Arabica Coffee Beans", "Premium coffee beans, 1kg", new BigDecimal("19.99"), 50
        );
    }

    private Product product(ProductRequest request) {
        return new Product(
                request.sku(), request.name(), request.description(), request.price(), request.quantity()
        );
    }
}
