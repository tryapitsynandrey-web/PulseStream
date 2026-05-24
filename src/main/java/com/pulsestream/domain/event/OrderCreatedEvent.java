package com.pulsestream.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
    String eventId,
    Instant occurredAt,
    String orderId,
    String customerId,
    String productId,
    int quantity,
    BigDecimal price,
    BigDecimal totalAmount,
    String status,
    Integer schemaVersion
) implements DomainEvent {

    public OrderCreatedEvent {
        if (schemaVersion == null) {
            schemaVersion = 1;
        }
    }

    public OrderCreatedEvent(String eventId, Instant occurredAt, String orderId, String customerId, String productId, int quantity, BigDecimal price, BigDecimal totalAmount, String status) {
        this(eventId, occurredAt, orderId, customerId, productId, quantity, price, totalAmount, status, 1);
    }

    @Override
    public String aggregateId() {
        return orderId;
    }
}
