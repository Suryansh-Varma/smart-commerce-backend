package com.ansh.smart_commerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.dto.WishlistResponse;
import com.ansh.smart_commerce.service.WishlistService;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final com.ansh.smart_commerce.security.SecurityHelper securityHelper;

    public WishlistController(WishlistService wishlistService, com.ansh.smart_commerce.security.SecurityHelper securityHelper) {
        this.wishlistService = wishlistService;
        this.securityHelper = securityHelper;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            @RequestParam(required = false) Long userId,
            @RequestParam Long productId) {
        Long currentUserId = securityHelper.getCurrentUser().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Added to wishlist",
                        wishlistService.addToWishlist(currentUserId, productId)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @RequestParam(required = false) Long userId,
            @PathVariable Long productId) {
        Long currentUserId = securityHelper.getCurrentUser().getId();
        wishlistService.removeFromWishlist(currentUserId, productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", null));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> getWishlist(
            @PathVariable Long userId) {
        Long currentUserId = securityHelper.getCurrentUser().getId();
        return ResponseEntity.ok(
                ApiResponse.success("Wishlist retrieved", wishlistService.getWishlist(currentUserId)));
    }
}
