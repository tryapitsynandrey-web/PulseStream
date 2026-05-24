package com.pulsestream.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class IngestionMetrics {

    private final MeterRegistry meterRegistry;

    public IngestionMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementIngestedEvent(String eventType) {
        Counter.builder("pulsestream.events.ingested.total")
                .description("Total number of business events ingested by PulseStream")
                .tag("eventType", eventType)
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopIngestionTimer(Timer.Sample sample, String eventType, String status) {
        if (sample != null) {
            sample.stop(Timer.builder("pulsestream.events.ingestion.duration")
                    .description("Duration of the ingestion process from request to Kafka publication")
                    .tag("eventType", eventType)
                    .tag("status", status)
                    .publishPercentiles(0.95, 0.99)
                    .register(meterRegistry));
        }
    }

    public void incrementFailedIngestion(String eventType, String failureType) {
        Counter.builder("pulsestream.events.ingestion.failed.total")
                .description("Total number of failed business event ingestions")
                .tag("eventType", eventType)
                .tag("failureType", failureType)
                .register(meterRegistry)
                .increment();
    }

    public void stopPersistenceTimer(Timer.Sample sample, String entityType, String operation) {
        if (sample != null) {
            sample.stop(Timer.builder("pulsestream.persistence.duration")
                    .description("Duration of database persistence operations")
                    .tag("entityType", entityType)
                    .tag("operation", operation)
                    .publishPercentiles(0.95, 0.99)
                    .register(meterRegistry));
        }
    }

    public void stopOutboxTimer(Timer.Sample sample, String status) {
        if (sample != null) {
            sample.stop(Timer.builder("pulsestream.outbox.processing.duration")
                    .description("Duration of outbox event polling and processing")
                    .tag("status", status)
                    .publishPercentiles(0.95, 0.99)
                    .register(meterRegistry));
        }
    }
}
