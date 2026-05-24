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
    String transactionRef
) implements DomainEvent {

    @Override
    public String aggregateId() {
        return paymentId;
    }
}
