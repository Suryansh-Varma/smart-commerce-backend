package com.ansh.smart_commerce.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.exception.ProductNotFound;
import com.ansh.smart_commerce.repository.ProductRepository;

@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product addProduct(Product product) {
        log.info("Adding new product: {}", product.getName());
        Product saved = productRepository.save(product);
        log.info("Product saved with id: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(long id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", id);
                    return new ProductNotFound("Product not found with id: " + id);
                });
    }

    public Product updateProduct(long id, Product updatedProduct) {
        log.info("Updating product with id: {}", id);
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed — product not found with id: {}", id);
                    return new ProductNotFound("Product not found with id: " + id);
                });
        existing.setName(updatedProduct.getName());
        existing.setCost(updatedProduct.getCost());
        existing.setStock(updatedProduct.getStock());
        existing.setCategory(updatedProduct.getCategory());
        log.info("Product updated successfully with id: {}", id);
        return productRepository.save(existing);
    }

    public void deleteProduct(long id) {
        log.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            log.warn("Delete failed — product not found with id: {}", id);
            throw new ProductNotFound("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted with id: {}", id);
    }
}
