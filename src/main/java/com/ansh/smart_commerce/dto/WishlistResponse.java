package com.ansh.smart_commerce.dto;

import com.ansh.smart_commerce.entity.Wishlist;

public class WishlistResponse {

    private Long wishlistId;
    private Long productId;
    private String productName;
    private double productPrice;
    private String imageUrl;

    public static WishlistResponse from(Wishlist wishlist) {
        WishlistResponse r = new WishlistResponse();
        r.wishlistId = wishlist.getId();
        r.productId = wishlist.getProduct().getId();
        r.productName = wishlist.getProduct().getName();
        r.productPrice = wishlist.getProduct().getCost();
        r.imageUrl = wishlist.getProduct().getImageUrl();
        return r;
    }

    public Long getWishlistId() { return wishlistId; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getProductPrice() { return productPrice; }
    public String getImageUrl() { return imageUrl; }
}
