package com.pulsestream.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record RefundIssuedEvent(
    String eventId,
    Instant occurredAt,
    String refundId,
    String paymentId,
    BigDecimal amount,
    String reason,
    String status
) implements DomainEvent {

    @Override
    public String aggregateId() {
        return refundId;
    }
}
