package com.pulsestream.domain.event;

import java.time.Instant;

public record ActivityDetectedEvent(
    String eventId,
    Instant occurredAt,
    String activityId,
    String customerId,
    String activityType,
    String metadata
) implements DomainEvent {

    @Override
    public String aggregateId() {
        return activityId;
    }
}
