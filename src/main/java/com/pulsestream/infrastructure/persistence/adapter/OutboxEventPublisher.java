package com.pulsestream.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsestream.application.port.out.EventPublisher;
import com.pulsestream.domain.event.DomainEvent;
import com.pulsestream.infrastructure.persistence.entity.OutboxEventEntity;
import com.pulsestream.infrastructure.persistence.repository.SpringDataOutboxRepository;
import org.slf4j.MDC;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Primary
public class OutboxEventPublisher implements EventPublisher {

    private final SpringDataOutboxRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(SpringDataOutboxRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            String correlationId = MDC.get("correlationId");
            String outboxId = UUID.randomUUID().toString();

            OutboxEventEntity outboxRecord = new OutboxEventEntity(
                outboxId,
                event.eventId(),
                event.getClass().getSimpleName(),
                jsonPayload,
                "PENDING",
                Instant.now(),
                correlationId
            );

            repository.save(outboxRecord);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event for outbox: " + event.eventId(), e);
        }
    }
}
