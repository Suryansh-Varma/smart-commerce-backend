package com.ansh.smart_commerce.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.dto.ReviewRequest;
import com.ansh.smart_commerce.dto.ReviewResponse;
import com.ansh.smart_commerce.service.ReviewService;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted", reviewService.addReview(request)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(
            @PathVariable Long productId) {
        return ResponseEntity.ok(
                ApiResponse.success("Reviews retrieved", reviewService.getProductReviews(productId)));
    }

    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(@PathVariable Long productId) {
        return ResponseEntity.ok(
                ApiResponse.success("Average rating", reviewService.getAverageRating(productId)));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Review updated", reviewService.updateReview(reviewId, request)));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted", null));
    }
}
