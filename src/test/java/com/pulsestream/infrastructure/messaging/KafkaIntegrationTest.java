package com.pulsestream.infrastructure.messaging;

import com.pulsestream.AbstractIntegrationTest;
import com.pulsestream.application.port.out.EventPublisher;
import com.pulsestream.domain.event.OrderCreatedEvent;
import com.pulsestream.domain.model.Order;
import com.pulsestream.domain.repository.OrderRepository;
import com.pulsestream.infrastructure.persistence.repository.SpringDataIngestedEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SpringDataIngestedEventRepository ingestedEventRepository;

    @Test
    void shouldPublishToKafkaAndConsumeAsynchronously() throws InterruptedException {
        String orderId = "order-kafka-abc";
        String eventId = "event-kafka-123";
        Instant occurredAt = Instant.now();

        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                occurredAt,
                orderId,
                "cust-kafka",
                "prod-kafka",
                5,
                BigDecimal.valueOf(100.00),
                BigDecimal.valueOf(500.00),
                "PENDING"
        );

        // Verify order does not exist initially
        Optional<Order> initialOrder = orderRepository.findById(orderId);
        assertFalse(initialOrder.isPresent());

        // Publish event to Kafka
        eventPublisher.publish(event);

        // Await asynchronous consumer processing (max 10 seconds check loop)
        boolean processed = false;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(500);
            Optional<Order> fetchedOrder = orderRepository.findById(orderId);
            if (fetchedOrder.isPresent()) {
                processed = true;
                assertEquals("PENDING", fetchedOrder.get().getStatus());
                break;
            }
        }

        assertTrue(processed, "Kafka consumer failed to process the OrderCreatedEvent within timeout");

        // Verify audit log exists
        assertTrue(ingestedEventRepository.existsById(eventId), "Ingested event audit log was not saved to database");
    }
}
