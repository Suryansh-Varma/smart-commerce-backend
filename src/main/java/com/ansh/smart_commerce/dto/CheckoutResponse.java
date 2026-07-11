package com.ansh.smart_commerce.dto;

import java.time.LocalDate;

import com.ansh.smart_commerce.enums.PaymentMethod;
import com.ansh.smart_commerce.enums.PaymentStatus;

public class CheckoutResponse {

    private Long orderId;
    private Long paymentId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private double totalAmount;
    private LocalDate estimatedDeliveryDate;

    public CheckoutResponse() {}

    public CheckoutResponse(Long orderId, Long paymentId, PaymentMethod paymentMethod,
                            PaymentStatus paymentStatus, double totalAmount, LocalDate estimatedDeliveryDate) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.totalAmount = totalAmount;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public Long getOrderId() { return orderId; }
    public Long getPaymentId() { return paymentId; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public double getTotalAmount() { return totalAmount; }
    public LocalDate getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
}
