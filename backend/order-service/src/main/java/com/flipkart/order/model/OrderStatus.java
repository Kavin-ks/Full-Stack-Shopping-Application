package com.flipkart.order.model;

/** Lifecycle of an order, in the order the tracker shows it. */
public enum OrderStatus {
    PLACED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED;

    public boolean isCancellable() {
        return this == PLACED || this == SHIPPED;
    }
}
