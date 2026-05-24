package com.pulsestream.domain.exception;

public class DuplicateEventException extends RuntimeException {
    private final String eventId;

    public DuplicateEventException(String eventId) {
        super("Duplicate event ingestion detected. Event ID '" + eventId + "' has already been processed.");
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }
}
