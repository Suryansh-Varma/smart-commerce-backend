package com.ansh.smart_commerce.dto;

import java.time.LocalDateTime;

import com.ansh.smart_commerce.entity.Payment;
import com.ansh.smart_commerce.enums.PaymentMethod;
import com.ansh.smart_commerce.enums.PaymentStatus;

public class PaymentResponse {

    private Long paymentId;
    private Long orderId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
    private double amount;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payment payment) {
        PaymentResponse r = new PaymentResponse();
        r.paymentId = payment.getId();
        r.orderId = payment.getOrder().getId();
        r.paymentMethod = payment.getPaymentMethod();
        r.paymentStatus = payment.getPaymentStatus();
        r.transactionId = payment.getTransactionId();
        r.amount = payment.getAmount();
        r.createdAt = payment.getCreatedAt();
        return r;
    }

    public Long getPaymentId() { return paymentId; }
    public Long getOrderId() { return orderId; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public String getTransactionId() { return transactionId; }
    public double getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
