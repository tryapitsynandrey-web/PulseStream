package com.pulsestream.observability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
}
