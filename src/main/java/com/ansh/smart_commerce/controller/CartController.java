package com.ansh.smart_commerce.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.dto.CartRequest;
import com.ansh.smart_commerce.dto.CartResponse;
import com.ansh.smart_commerce.dto.UpdateQuantityRequest;
import com.ansh.smart_commerce.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final com.ansh.smart_commerce.security.SecurityHelper securityHelper;

    public CartController(CartService cartService, com.ansh.smart_commerce.security.SecurityHelper securityHelper) {
        this.cartService = cartService;
        this.securityHelper = securityHelper;
    }

    // Add item to cart
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestBody CartRequest request) {

        request.setUserId(securityHelper.getCurrentUser().getId());
        CartResponse cartResponse = cartService.addToCart(request);

        return ResponseEntity.ok(
                ApiResponse.success("Item added to cart", cartResponse)
        );
    }

    // Get user's cart
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CartResponse>>> getCart(
            @PathVariable Long userId) {

        Long currentUserId = securityHelper.getCurrentUser().getId();
        List<CartResponse> cart = cartService.getCart(currentUserId);

        return ResponseEntity.ok(
                ApiResponse.success("Cart retrieved successfully", cart)
        );
    }

    // Update quantity
    @PutMapping("/{cartId}")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(
            @PathVariable Long cartId,
            @RequestBody UpdateQuantityRequest request) {

        cartService.updateQuantity(cartId, request.getQuantity());

        return ResponseEntity.ok(
                ApiResponse.success("Item quantity updated", null)
        );
    }

    // Remove item
    @DeleteMapping("/{cartId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Long cartId) {

        cartService.removeItem(cartId);

        return ResponseEntity.ok(
                ApiResponse.success("Item removed from cart", null)
        );
    }
}