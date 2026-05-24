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
    String status
) implements DomainEvent {

    @Override
    public String aggregateId() {
        return orderId;
    }
}
