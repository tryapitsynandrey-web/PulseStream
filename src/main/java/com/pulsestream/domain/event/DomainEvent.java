package com.pulsestream.domain.event;

import java.time.Instant;

public sealed interface DomainEvent 
    permits OrderCreatedEvent, PaymentConfirmedEvent, RefundIssuedEvent, ActivityDetectedEvent {
    
    String eventId();
    Instant occurredAt();
    String aggregateId();

    default Integer schemaVersion() {
        return 1;
    }
}
