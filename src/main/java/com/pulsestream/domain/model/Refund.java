package com.pulsestream.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Refund {
    private final String id;
    private final String paymentId;
    private final BigDecimal amount;
    private final String reason;
    private String status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Refund(String id, String paymentId, BigDecimal amount, String reason, String status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Refund ID cannot be null");
        this.paymentId = Objects.requireNonNull(paymentId, "Payment ID cannot be null");
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.reason = Objects.requireNonNull(reason, "Reason cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated timestamp cannot be null");
    }

    public void approve() {
        this.status = "APPROVED";
        this.updatedAt = Instant.now();
    }

    public void reject() {
        this.status = "REJECTED";
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Refund refund = (Refund) o;
        return Objects.equals(id, refund.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
