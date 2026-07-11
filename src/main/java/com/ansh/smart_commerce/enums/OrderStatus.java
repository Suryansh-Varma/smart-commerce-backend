package com.ansh.smart_commerce.enums;

/**
 * Represents the lifecycle of an order in Smart Commerce.
 *
 * State transitions:
 *   PENDING  → CONFIRMED (payment/processing success)
 *   PENDING  → CANCELLED (user cancels before processing)
 *   CONFIRMED → CANCELLED (admin/system cancels)
 *
 * Stored as a String in DB (not ordinal) so that reordering
 * enum values never corrupts existing data.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
