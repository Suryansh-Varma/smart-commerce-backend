package com.ansh.smart_commerce.exception;


public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found with ID: " + orderId);
    }
}
