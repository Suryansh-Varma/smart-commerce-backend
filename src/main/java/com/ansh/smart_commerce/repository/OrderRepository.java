package com.ansh.smart_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ansh.smart_commerce.entity.Order;
import com.ansh.smart_commerce.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}