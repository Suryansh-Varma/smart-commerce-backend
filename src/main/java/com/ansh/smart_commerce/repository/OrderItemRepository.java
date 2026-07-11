package com.ansh.smart_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ansh.smart_commerce.entity.OrderItem;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {
}