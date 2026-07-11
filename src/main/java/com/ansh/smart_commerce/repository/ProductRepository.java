package com.ansh.smart_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ansh.smart_commerce.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
}
