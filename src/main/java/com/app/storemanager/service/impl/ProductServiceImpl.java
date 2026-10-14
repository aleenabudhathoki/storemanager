package com.app.storemanager.service.impl;

import com.app.storemanager.dto.ProductRequest;
import com.app.storemanager.dto.ProductResponse;
import com.app.storemanager.entity.Product;
import com.app.storemanager.exception.DuplicateSkuException;
import com.app.storemanager.exception.ProductNotFoundException;
import com.app.storemanager.repository.ProductRepository;
import com.app.storemanager.service.ProductService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default transactional implementation of {@link ProductService}.
 *
 * <p>SKU checks provide an early, user-friendly conflict response. The
 * database uniqueness constraint remains authoritative, and violations from
 * concurrent writes are translated to {@link DuplicateSkuException}.</p>
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateSkuException(request.sku());
        }

        Product product = new Product(
                request.sku(),
                request.name(),
                request.description(),
                request.price(),
                request.quantity()
        );

        try {
            return toResponse(productRepository.saveAndFlush(product));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateSkuException(request.sku());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return toResponse(findProduct(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);

        if (!request.sku().equals(product.getSku())
                && productRepository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new DuplicateSkuException(request.sku());
        }

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());

        try {
            return toResponse(productRepository.saveAndFlush(product));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateSkuException(request.sku());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        productRepository.delete(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
