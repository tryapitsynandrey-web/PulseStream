package com.pulsestream.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Payment {
    private final String id;
    private final String orderId;
    private final BigDecimal amount;
    private String status;
    private final String transactionRef;
    private final Instant createdAt;
    private Instant updatedAt;

    public Payment(String id, String orderId, BigDecimal amount, String status, String transactionRef, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Payment ID cannot be null");
        this.orderId = Objects.requireNonNull(orderId, "Order ID cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.transactionRef = Objects.requireNonNull(transactionRef, "Transaction reference cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated timestamp cannot be null");
    }

    public void fail() {
        this.status = "FAILED";
        this.updatedAt = Instant.now();
    }

    public void succeed() {
        this.status = "SUCCESS";
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getTransactionRef() { return transactionRef; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
