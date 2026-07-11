package com.ansh.smart_commerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class ApplyCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    @Positive(message = "Order amount must be positive")
    private double orderAmount;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getOrderAmount() { return orderAmount; }
    public void setOrderAmount(double orderAmount) { this.orderAmount = orderAmount; }
}
