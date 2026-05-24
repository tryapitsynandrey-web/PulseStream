package com.pulsestream.infrastructure.persistence.adapter;

import com.pulsestream.domain.repository.IngestedEventRepository;
import com.pulsestream.infrastructure.persistence.entity.IngestedEventEntity;
import com.pulsestream.infrastructure.persistence.repository.SpringDataIngestedEventRepository;
import com.pulsestream.observability.metrics.IngestionMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@SuppressWarnings("null")
public class IngestedEventPersistenceAdapter implements IngestedEventRepository {

    private final SpringDataIngestedEventRepository repository;
    private final IngestionMetrics metrics;

    public IngestedEventPersistenceAdapter(SpringDataIngestedEventRepository repository, IngestionMetrics metrics) {
        this.repository = repository;
        this.metrics = metrics;
    }

    @Override
    public void save(String eventId, String eventType, Instant occurredAt, String jsonPayload) {
        Timer.Sample sample = metrics.startTimer();
        try {
            repository.save(new IngestedEventEntity(eventId, eventType, occurredAt, jsonPayload));
            metrics.stopPersistenceTimer(sample, "IngestedEvent", "SAVE");
        } catch (Exception e) {
            metrics.stopPersistenceTimer(sample, "IngestedEvent", "SAVE_FAILED");
            throw e;
        }
    }

    @Override
    public boolean existsById(String eventId) {
        return repository.existsById(eventId);
    }
}
