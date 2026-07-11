package com.ansh.smart_commerce.dto;

import java.util.List;

public class DashboardResponse {

    private long totalUsers;
    private long totalOrders;
    private double totalRevenue;
    private long totalProductsSold;
    private List<LowStockProduct> lowStockProducts;
    private long pendingOrders;

    public DashboardResponse() {}

    public DashboardResponse(long totalUsers, long totalOrders, double totalRevenue,
                             long totalProductsSold, List<LowStockProduct> lowStockProducts,
                             long pendingOrders) {
        this.totalUsers = totalUsers;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.totalProductsSold = totalProductsSold;
        this.lowStockProducts = lowStockProducts;
        this.pendingOrders = pendingOrders;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getTotalOrders() { return totalOrders; }
    public double getTotalRevenue() { return totalRevenue; }
    public long getTotalProductsSold() { return totalProductsSold; }
    public List<LowStockProduct> getLowStockProducts() { return lowStockProducts; }
    public long getPendingOrders() { return pendingOrders; }

    public static class LowStockProduct {
        private Long productId;
        private String name;
        private int stock;

        public LowStockProduct(Long productId, String name, int stock) {
            this.productId = productId;
            this.name = name;
            this.stock = stock;
        }

        public Long getProductId() { return productId; }
        public String getName() { return name; }
        public int getStock() { return stock; }
    }
}
