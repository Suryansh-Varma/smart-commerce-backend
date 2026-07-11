package com.ansh.smart_commerce.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ansh.smart_commerce.dto.DashboardResponse;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.repository.OrderRepository;
import com.ansh.smart_commerce.repository.ProductRepository;
import com.ansh.smart_commerce.repository.UserRepository;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public AdminService(UserRepository userRepository,
                        OrderRepository orderRepository,
                        ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        log.info("Generating admin dashboard summary");

        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();

        double totalRevenue = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED)
                .mapToDouble(o -> o.getTotalAmount())
                .sum();

        long totalProductsSold = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED)
                .flatMap(o -> o.getOrderItems().stream())
                .mapToLong(item -> item.getQuantity())
                .sum();

        List<DashboardResponse.LowStockProduct> lowStock = productRepository.findAll().stream()
                .filter(p -> p.getStock() <= LOW_STOCK_THRESHOLD)
                .map(p -> new DashboardResponse.LowStockProduct(p.getId(), p.getName(), p.getStock()))
                .toList();

        long pendingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .count();

        log.info("Dashboard: users={}, orders={}, revenue={}, pending={}",
                totalUsers, totalOrders, totalRevenue, pendingOrders);

        return new DashboardResponse(totalUsers, totalOrders, totalRevenue,
                totalProductsSold, lowStock, pendingOrders);
    }
}
