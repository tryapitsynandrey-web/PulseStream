package com.pulsestream.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public class Order {
    private final String id;
    private final String customerId;
    private final String productId;
    private final int quantity;
    private final BigDecimal price;
    private final BigDecimal totalAmount;
    private String status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Order(String id, String customerId, String productId, int quantity, BigDecimal price, String status, Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Order ID cannot be null");
        this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
        this.productId = Objects.requireNonNull(productId, "Product ID cannot be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = quantity;
        this.price = Objects.requireNonNull(price, "Price cannot be null");
        this.totalAmount = price.multiply(BigDecimal.valueOf(quantity));
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Updated timestamp cannot be null");
    }

    public void complete() {
        if ("CANCELLED".equals(this.status)) {
            throw new IllegalStateException("Cannot complete a cancelled order");
        }
        this.status = "COMPLETED";
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if ("COMPLETED".equals(this.status)) {
            throw new IllegalStateException("Cannot cancel a completed order");
        }
        this.status = "CANCELLED";
        this.updatedAt = Instant.now();
    }

    // Getters
    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
