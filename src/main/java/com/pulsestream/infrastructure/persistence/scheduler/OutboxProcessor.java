package com.pulsestream.infrastructure.persistence.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsestream.domain.event.*;
import com.pulsestream.infrastructure.messaging.producer.KafkaEventPublisher;
import com.pulsestream.infrastructure.persistence.entity.OutboxEventEntity;
import com.pulsestream.infrastructure.persistence.repository.SpringDataOutboxRepository;
import com.pulsestream.observability.metrics.IngestionMetrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@EnableScheduling
public class OutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);
    private static final int MAX_RETRIES = 5;

    private final SpringDataOutboxRepository outboxRepository;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ObjectMapper objectMapper;
    private final IngestionMetrics metrics;

    public OutboxProcessor(
            SpringDataOutboxRepository outboxRepository,
            KafkaEventPublisher kafkaEventPublisher,
            ObjectMapper objectMapper,
            IngestionMetrics metrics) {
        this.outboxRepository = outboxRepository;
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:100}")
    @Transactional
    public void processOutbox() {
        List<OutboxEventEntity> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(
                "PENDING",
                PageRequest.of(0, 50)
        );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to process", pendingEvents.size());
        Timer.Sample sample = metrics.startTimer();
        String batchStatus = "SUCCESS";

        try {
            for (OutboxEventEntity entity : pendingEvents) {
                // Restore correlation ID to MDC context
                if (entity.getCorrelationId() != null) {
                    MDC.put("correlationId", entity.getCorrelationId());
                } else {
                    MDC.remove("correlationId");
                }

                try {
                    DomainEvent domainEvent = deserializeEvent(entity.getPayload(), entity.getEventType());
                    kafkaEventPublisher.publish(domainEvent);

                    entity.setStatus("PROCESSED");
                    entity.setProcessedAt(Instant.now());
                    outboxRepository.save(entity);

                    log.debug("Successfully published outbox event {} of type {}", entity.getEventId(), entity.getEventType());
                } catch (Exception e) {
                    batchStatus = "PARTIAL_FAILURE";
                    log.error("Failed to process outbox event {}. Attempt={}", entity.getEventId(), entity.getRetryCount() + 1, e);
                    entity.setRetryCount(entity.getRetryCount() + 1);
                    entity.setErrorMessage(e.getMessage());

                    if (entity.getRetryCount() >= MAX_RETRIES) {
                        entity.setStatus("FAILED");
                        log.error("Outbox event {} exceeded maximum retries. Marked as FAILED.", entity.getEventId());
                    }
                    outboxRepository.save(entity);
                } finally {
                    MDC.remove("correlationId");
                }
            }
        } finally {
            metrics.stopOutboxTimer(sample, batchStatus);
        }
    }

    private DomainEvent deserializeEvent(String json, String type) throws Exception {
        return switch (type) {
            case "OrderCreatedEvent" -> objectMapper.readValue(json, OrderCreatedEvent.class);
            case "PaymentConfirmedEvent" -> objectMapper.readValue(json, PaymentConfirmedEvent.class);
            case "RefundIssuedEvent" -> objectMapper.readValue(json, RefundIssuedEvent.class);
            case "ActivityDetectedEvent" -> objectMapper.readValue(json, ActivityDetectedEvent.class);
            default -> throw new IllegalArgumentException("Unknown domain event type in outbox: " + type);
        };
    }
}
