package com.ansh.smart_commerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.Review;
import com.ansh.smart_commerce.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProduct(Product product);

    Optional<Review> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product = :product")
    double findAverageRatingByProduct(@Param("product") Product product);
}
