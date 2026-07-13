package com.ansh.smart_commerce.dto.ai;

public class ProductSuggestion {

    private long id;
    private String name;
    private double cost;
    private String category;
    private String imageUrl;
    private int stock;
    private String reason; // AI-generated explanation of why this product matches

    public ProductSuggestion() {
    }

    public ProductSuggestion(long id, String name, double cost, String category, String imageUrl, int stock, String reason) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.category = category;
        this.imageUrl = imageUrl;
        this.stock = stock;
        this.reason = reason;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
