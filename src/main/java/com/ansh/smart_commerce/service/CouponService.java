package com.ansh.smart_commerce.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ansh.smart_commerce.dto.CouponRequest;
import com.ansh.smart_commerce.entity.Coupon;
import com.ansh.smart_commerce.enums.DiscountType;
import com.ansh.smart_commerce.exception.CouponExpiredException;
import com.ansh.smart_commerce.repository.CouponRepository;

@Service
public class CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    public Coupon createCoupon(CouponRequest request) {
        log.info("Creating coupon with code: {}", request.getCode());
        Coupon coupon = new Coupon();
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinimumAmount(request.getMinimumAmount());
        coupon.setExpiryDate(request.getExpiryDate());
        coupon.setActive(true);
        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon {} created", saved.getCode());
        return saved;
    }

    public double applyDiscount(String code, double orderAmount) {
        log.info("Applying coupon '{}' to amount {}", code, orderAmount);

        Coupon coupon = couponRepository.findByCodeAndActiveTrue(code.toUpperCase())
                .orElseThrow(() -> {
                    log.warn("Coupon '{}' not found or inactive", code);
                    return new CouponExpiredException(code);
                });

        if (coupon.getExpiryDate().isBefore(LocalDate.now())) {
            log.warn("Coupon '{}' is expired", code);
            coupon.setActive(false);
            couponRepository.save(coupon);
            throw new CouponExpiredException(code);
        }

        if (orderAmount < coupon.getMinimumAmount()) {
            throw new IllegalArgumentException(
                    "Minimum order amount for coupon '" + code + "' is ₹" + coupon.getMinimumAmount());
        }

        double discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = orderAmount * (coupon.getDiscountValue() / 100.0);
        } else {
            discount = coupon.getDiscountValue();
        }

        double finalAmount = Math.max(0, orderAmount - discount);
        log.info("Coupon '{}' applied — discount: {}, final amount: {}", code, discount, finalAmount);
        return finalAmount;
    }

    @Transactional
    public void deactivateCoupon(Long couponId) {
        log.info("Deactivating coupon {}", couponId);
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found with id: " + couponId));
        coupon.setActive(false);
        couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public java.util.List<Coupon> getAllCoupons() {
        log.info("Fetching all coupons");
        return couponRepository.findAll();
    }
}
