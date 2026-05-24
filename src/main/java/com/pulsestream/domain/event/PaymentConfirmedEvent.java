package com.pulsestream.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentConfirmedEvent(
    String eventId,
    Instant occurredAt,
    String paymentId,
    String orderId,
    BigDecimal amount,
    String status,
    String transactionRef,
    Integer schemaVersion
) implements DomainEvent {

    public PaymentConfirmedEvent {
        if (schemaVersion == null) {
            schemaVersion = 1;
        }
    }

    public PaymentConfirmedEvent(String eventId, Instant occurredAt, String paymentId, String orderId, BigDecimal amount, String status, String transactionRef) {
        this(eventId, occurredAt, paymentId, orderId, amount, status, transactionRef, 1);
    }

    @Override
    public String aggregateId() {
        return paymentId;
    }
}
