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
    String status,
    Integer schemaVersion
) implements DomainEvent {

    public RefundIssuedEvent {
        if (schemaVersion == null) {
            schemaVersion = 1;
        }
    }

    public RefundIssuedEvent(String eventId, Instant occurredAt, String refundId, String paymentId, BigDecimal amount, String reason, String status) {
        this(eventId, occurredAt, refundId, paymentId, amount, reason, status, 1);
    }

    @Override
    public String aggregateId() {
        return refundId;
    }
}
