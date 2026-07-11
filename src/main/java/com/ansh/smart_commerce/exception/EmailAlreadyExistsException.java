package com.ansh.smart_commerce.exception;

public class EmailAlreadyExistsException
        extends RuntimeException {

    public EmailAlreadyExistsException(
            String message) {

        super(message);
    }
}