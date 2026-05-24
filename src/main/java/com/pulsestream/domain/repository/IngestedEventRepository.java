package com.pulsestream.domain.repository;


public interface IngestedEventRepository {
    void save(String eventId, String eventType, java.time.Instant occurredAt, String jsonPayload);
    boolean existsById(String eventId);
}
