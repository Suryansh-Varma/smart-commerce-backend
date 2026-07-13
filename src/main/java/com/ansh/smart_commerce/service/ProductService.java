package com.ansh.smart_commerce.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ansh.smart_commerce.entity.Order;
import com.ansh.smart_commerce.entity.OrderItem;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.exception.ProductNotFound;
import com.ansh.smart_commerce.repository.OrderRepository;
import com.ansh.smart_commerce.repository.ProductRepository;
import org.springframework.context.annotation.Lazy;

@Service
@Transactional
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public ProductService(ProductRepository productRepository, 
                          @Lazy OrderService orderService,
                          OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
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
    public List<Product> getLowStockProducts() {
        log.info("Fetching low stock products");
        return productRepository.findAll().stream()
                .filter(p -> p.getStock() <= 5 || !p.isAvailable())
                .toList();
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
        
        boolean wasAvailable = existing.isAvailable() && existing.getStock() > 0;
        
        existing.setStock(updatedProduct.getStock());
        existing.setCategory(updatedProduct.getCategory());
        existing.setAvailable(updatedProduct.isAvailable());
        
        boolean isNowUnavailable = !existing.isAvailable() || existing.getStock() == 0;
        
        if (wasAvailable && isNowUnavailable) {
            cancelPendingOrdersForProduct(existing);
        }
        
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

    private void cancelPendingOrdersForProduct(Product product) {
        log.info("Product {} is now unavailable. Cancelling pending orders...", product.getId());
        List<Order> pendingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .filter(o -> o.getOrderItems().stream().anyMatch(item -> item.getProduct().getId() == product.getId()))
                .toList();
                
        for (Order order : pendingOrders) {
            log.info("Auto-cancelling order {} due to product unavailability", order.getId());
            try {
                orderService.cancelOrder(order.getId());
            } catch (Exception e) {
                log.error("Failed to auto-cancel order " + order.getId(), e);
            }
        }
    }
}
