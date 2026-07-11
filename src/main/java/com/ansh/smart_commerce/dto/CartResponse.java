package com.ansh.smart_commerce.dto;

public class CartResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String imageUrl;
    private double price;
    private int quantity;
    private double subtotal;

    public CartResponse(
            Long id,
            Long productId,
            String productName,
            String imageUrl,
            double price,
            int quantity,
            double subtotal) {

        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }
}