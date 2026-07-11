package com.ansh.smart_commerce.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ansh.smart_commerce.dto.CartResponse;
import com.ansh.smart_commerce.entity.CartItem;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.exception.ProductNotFound;
import com.ansh.smart_commerce.exception.UserNotFoundException;
import com.ansh.smart_commerce.dto.CartRequest;
import com.ansh.smart_commerce.repository.CartRepository;
import com.ansh.smart_commerce.repository.ProductRepository;
import com.ansh.smart_commerce.repository.UserRepository;

@Service
@Transactional
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       UserRepository userRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public CartResponse addToCart(CartRequest request) {
        log.info("Adding product {} to cart for user {}", request.getProductId(), request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", request.getUserId());
                    return new UserNotFoundException(request.getUserId());
                });

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", request.getProductId());
                    return new ProductNotFound("Product not found with id: " + request.getProductId());
                });

        Optional<CartItem> existing = cartRepository.findByUserAndProduct(user, product);

        CartItem cartItem;
        if (existing.isPresent()) {
            cartItem = existing.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = new CartItem(user, product, request.getQuantity());
        }

        CartItem saved = cartRepository.save(cartItem);
        log.info("Cart updated for user {}: product={}, qty={}", user.getId(), product.getName(), saved.getQuantity());

        return new CartResponse(
                saved.getId(),
                saved.getProduct().getId(),
                saved.getProduct().getName(),
                saved.getProduct().getImageUrl(),
                saved.getProduct().getCost(),
                saved.getQuantity(),
                saved.getProduct().getCost() * saved.getQuantity()
        );
    }

    @Transactional(readOnly = true)
    public List<CartResponse> getCart(Long userId) {
        log.info("Fetching cart for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", userId);
                    return new UserNotFoundException(userId);
                });
        return cartRepository.findByUser(user).stream()
                .map(item -> new CartResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getImageUrl(),
                        item.getProduct().getCost(),
                        item.getQuantity(),
                        item.getProduct().getCost() * item.getQuantity()
                ))
                .toList();
    }

    public void updateQuantity(Long cartId, int quantity) {
        log.info("Updating cart item {} to quantity {}", cartId, quantity);
        CartItem cartItem = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartId));

        if (quantity == 0) {
            cartRepository.delete(cartItem);
            log.info("Cart item {} removed (quantity set to 0)", cartId);
            return;
        }

        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
        log.info("Cart item {} updated to quantity {}", cartId, quantity);
    }

    public void removeItem(Long cartId) {
        log.info("Removing cart item {}", cartId);
        CartItem cartItem = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with id: " + cartId));
        cartRepository.delete(cartItem);
        log.info("Cart item {} removed", cartId);
    }
}