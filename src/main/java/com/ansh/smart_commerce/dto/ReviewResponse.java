package com.ansh.smart_commerce.dto;

import java.time.LocalDateTime;

import com.ansh.smart_commerce.entity.Review;

public class ReviewResponse {

    private Long reviewId;
    private Long userId;
    private String userName;
    private Long productId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        ReviewResponse r = new ReviewResponse();
        r.reviewId = review.getId();
        r.userId = review.getUser().getId();
        r.userName = review.getUser().getName();
        r.productId = review.getProduct().getId();
        r.rating = review.getRating();
        r.comment = review.getComment();
        r.createdAt = review.getCreatedAt();
        return r;
    }

    public Long getReviewId() { return reviewId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public Long getProductId() { return productId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
