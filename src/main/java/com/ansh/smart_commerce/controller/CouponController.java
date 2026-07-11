package com.ansh.smart_commerce.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ansh.smart_commerce.dto.ApiResponse;
import com.ansh.smart_commerce.dto.ApplyCouponRequest;
import com.ansh.smart_commerce.dto.CouponRequest;
import com.ansh.smart_commerce.entity.Coupon;
import com.ansh.smart_commerce.service.CouponService;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Coupon>> createCoupon(
            @Valid @RequestBody CouponRequest request) {
        Coupon coupon = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Coupon created", coupon));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Double>> applyCoupon(
            @Valid @RequestBody ApplyCouponRequest request) {
        double finalAmount = couponService.applyDiscount(request.getCode(), request.getOrderAmount());
        return ResponseEntity.ok(ApiResponse.success("Coupon applied", finalAmount));
    }

    @PatchMapping("/{couponId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long couponId) {
        couponService.deactivateCoupon(couponId);
        return ResponseEntity.ok(ApiResponse.success("Coupon deactivated", null));
    }
}
