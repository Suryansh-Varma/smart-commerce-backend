package com.ansh.smart_commerce.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ansh.smart_commerce.dto.WishlistResponse;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.entity.Wishlist;
import com.ansh.smart_commerce.exception.ProductNotFound;
import com.ansh.smart_commerce.exception.UserNotFoundException;
import com.ansh.smart_commerce.repository.ProductRepository;
import com.ansh.smart_commerce.repository.UserRepository;
import com.ansh.smart_commerce.repository.WishlistRepository;

@Service
public class WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistService.class);

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public WishlistResponse addToWishlist(Long userId, Long productId) {
        log.info("Adding product {} to wishlist for user {}", productId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFound("Product not found with id: " + productId));

        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            log.warn("Product {} already in wishlist for user {}", productId, userId);
            throw new IllegalStateException("Product is already in your wishlist");
        }

        Wishlist saved = wishlistRepository.save(new Wishlist(user, product));
        log.info("Wishlist entry {} created", saved.getId());
        return WishlistResponse.from(saved);
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        log.info("Removing product {} from wishlist for user {}", productId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFound("Product not found with id: " + productId));

        Wishlist entry = wishlistRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new IllegalStateException("Product not found in wishlist"));

        wishlistRepository.delete(entry);
        log.info("Product {} removed from wishlist for user {}", productId, userId);
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> getWishlist(Long userId) {
        log.info("Fetching wishlist for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return wishlistRepository.findByUser(user).stream()
                .map(WishlistResponse::from)
                .toList();
    }
}
