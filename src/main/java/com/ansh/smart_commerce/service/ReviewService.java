package com.ansh.smart_commerce.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ansh.smart_commerce.dto.ReviewRequest;
import com.ansh.smart_commerce.dto.ReviewResponse;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.Review;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.exception.ProductNotFound;
import com.ansh.smart_commerce.exception.ReviewNotAllowedException;
import com.ansh.smart_commerce.repository.OrderRepository;
import com.ansh.smart_commerce.repository.ProductRepository;
import com.ansh.smart_commerce.repository.ReviewRepository;
import com.ansh.smart_commerce.security.SecurityHelper;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final SecurityHelper securityHelper;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         OrderRepository orderRepository,
                         SecurityHelper securityHelper) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.securityHelper = securityHelper;
    }

    @Transactional
    public ReviewResponse addReview(ReviewRequest request) {
        User user = securityHelper.getCurrentUser();
        log.info("User {} submitting review for product {}", user.getId(), request.getProductId());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFound("Product not found with id: " + request.getProductId()));

        boolean hasPurchased = orderRepository.findByUser(user).stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED || o.getStatus() == OrderStatus.PENDING)
                .flatMap(o -> o.getOrderItems().stream())
                .anyMatch(item -> item.getProduct().getId() == product.getId());

        if (!hasPurchased) {
            log.warn("User {} attempted to review product {} without purchasing it",
                    user.getId(), request.getProductId());
            throw new ReviewNotAllowedException(
                    "You can only review products you have purchased");
        }

        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new ReviewNotAllowedException("You have already reviewed this product");
        }

        Review review = new Review(user, product, request.getRating(),
                request.getComment(), LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        log.info("Review {} saved for product {}", saved.getId(), product.getId());
        return ReviewResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(Long productId) {
        log.info("Fetching reviews for product {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFound("Product not found with id: " + productId));
        return reviewRepository.findByProduct(product).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public double getAverageRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFound("Product not found with id: " + productId));
        return reviewRepository.findAverageRatingByProduct(product);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        User user = securityHelper.getCurrentUser();
        log.info("Updating review {} by user {}", reviewId, user.getId());
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotAllowedException("Review not found with id: " + reviewId));
        
        if (review.getUser().getId() != user.getId()) {
            throw new ReviewNotAllowedException("You do not own this review");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        Review saved = reviewRepository.save(review);
        return ReviewResponse.from(saved);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        User user = securityHelper.getCurrentUser();
        log.info("Deleting review {} by user {}", reviewId, user.getId());
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotAllowedException("Review not found with id: " + reviewId));
        
        if (review.getUser().getId() != user.getId()) {
            throw new ReviewNotAllowedException("You do not own this review");
        }

        reviewRepository.delete(review);
    }
}
