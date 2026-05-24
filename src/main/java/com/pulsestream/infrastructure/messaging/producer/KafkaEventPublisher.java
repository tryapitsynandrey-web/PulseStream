package com.pulsestream.infrastructure.messaging.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsestream.application.port.out.EventPublisher;
import com.pulsestream.domain.event.ActivityDetectedEvent;
import com.pulsestream.domain.event.DomainEvent;
import com.pulsestream.domain.event.OrderCreatedEvent;
import com.pulsestream.domain.event.PaymentConfirmedEvent;
import com.pulsestream.domain.event.RefundIssuedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import io.github.resilience4j.retry.annotation.Retry;
import java.nio.charset.StandardCharsets;

@Component
@Retry(name = "kafkaPublish")
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = determineTopic(event);
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            log.info("Publishing event {} to Kafka topic '{}' with key '{}'", event.eventId(), topic, event.aggregateId());

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.aggregateId(), jsonPayload);
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                record.headers().add(new RecordHeader("X-Correlation-Id", correlationId.getBytes(StandardCharsets.UTF_8)));
            }

            kafkaTemplate.send(record);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize domain event: {}", event.eventId(), e);
            throw new RuntimeException("Serialization failure for event: " + event.eventId(), e);
        }
    }

    private String determineTopic(DomainEvent event) {
        return switch (event) {
            case OrderCreatedEvent o -> "order-created";
            case PaymentConfirmedEvent p -> "payment-confirmed";
            case RefundIssuedEvent r -> "refund-issued";
            case ActivityDetectedEvent a -> "activity-detected";
        };
    }
}
