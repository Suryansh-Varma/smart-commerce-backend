package com.ansh.smart_commerce.exception;

public class CouponExpiredException extends RuntimeException {

    public CouponExpiredException(String code) {
        super("Coupon '" + code + "' is expired or inactive");
    }
}
