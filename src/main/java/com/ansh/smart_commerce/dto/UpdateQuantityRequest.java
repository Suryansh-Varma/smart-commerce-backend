package com.ansh.smart_commerce.dto;

import jakarta.validation.constraints.Min;

public class UpdateQuantityRequest {

    @Min(value = 0, message = "Quantity cannot be negative")
    private int quantity;

    public UpdateQuantityRequest() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}