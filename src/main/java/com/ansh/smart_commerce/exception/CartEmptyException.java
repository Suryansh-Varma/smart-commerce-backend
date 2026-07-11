package com.ansh.smart_commerce.exception;


public class CartEmptyException extends RuntimeException {

    public CartEmptyException(String message) {
        super(message);
    }
}
