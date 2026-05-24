package com.pulsestream.domain.event;

import java.time.Instant;

public record ActivityDetectedEvent(
    String eventId,
    Instant occurredAt,
    String activityId,
    String customerId,
    String activityType,
    String metadata,
    Integer schemaVersion
) implements DomainEvent {

    public ActivityDetectedEvent {
        if (schemaVersion == null) {
            schemaVersion = 1;
        }
    }

    public ActivityDetectedEvent(String eventId, Instant occurredAt, String activityId, String customerId, String activityType, String metadata) {
        this(eventId, occurredAt, activityId, customerId, activityType, metadata, 1);
    }

    @Override
    public String aggregateId() {
        return activityId;
    }
}
