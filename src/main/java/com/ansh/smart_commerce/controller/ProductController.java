package com.ansh.smart_commerce.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> addProduct(@Valid @RequestBody Product product) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
                !("root".equalsIgnoreCase(authentication.getName()) || 
                  "root@techheaven.com".equalsIgnoreCase(authentication.getName()) ||
                  "root@teachheaven.com".equalsIgnoreCase(authentication.getName()))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only root user can create or modify product data", null));
        }
        Product saved = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added successfully", saved));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved successfully", productService.getAllProducts()));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<Product>>> getLowStockProducts() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
                !("root".equalsIgnoreCase(authentication.getName()) || 
                  "root@techheaven.com".equalsIgnoreCase(authentication.getName()) ||
                  "root@teachheaven.com".equalsIgnoreCase(authentication.getName()))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only root user can view low stock notifications", null));
        }
        return ResponseEntity.ok(
                ApiResponse.success("Low stock products retrieved", productService.getLowStockProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getByProductId(@PathVariable long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Product retrieved successfully", productService.getProductById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable long id,
            @Valid @RequestBody Product updatedProduct) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
                !("root".equalsIgnoreCase(authentication.getName()) || 
                  "root@techheaven.com".equalsIgnoreCase(authentication.getName()) ||
                  "root@teachheaven.com".equalsIgnoreCase(authentication.getName()))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only root user can create or modify product data", null));
        }
        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", productService.updateProduct(id, updatedProduct)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable long id) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
                !("root".equalsIgnoreCase(authentication.getName()) || 
                  "root@techheaven.com".equalsIgnoreCase(authentication.getName()) ||
                  "root@teachheaven.com".equalsIgnoreCase(authentication.getName()))) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only root user can create or modify product data", null));
        }
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}
