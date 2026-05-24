package com.pulsestream.infrastructure.messaging;

import com.pulsestream.AbstractIntegrationTest;
import com.pulsestream.application.port.out.EventPublisher;
import com.pulsestream.domain.event.OrderCreatedEvent;
import com.pulsestream.domain.model.Order;
import com.pulsestream.domain.repository.OrderRepository;
import com.pulsestream.infrastructure.persistence.repository.SpringDataIngestedEventRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SpringDataIngestedEventRepository ingestedEventRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

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

    @Test
    void shouldRouteMalformedEventToDeadLetterQueue() {
        // Send a payload containing an invalid timestamp which Jackson cannot parse to an Instant,
        // causing a deserialization failure in the consumer.
        String poisonedPayload = "{\"eventId\":\"err-123\", \"occurredAt\":\"bad-timestamp-string\", \"orderId\":\"order-fail\"}";

        // Subscribing to the DLT topic using standard Kafka consumer to check if it is routed
        Consumer<String, String> consumer = consumerFactory.createConsumer("test-dlt-group", "unique-suffix");
        consumer.subscribe(Collections.singletonList("order-created.DLT"));

        // Send the bad payload directly to order-created topic
        kafkaTemplate.send("order-created", "fail-key", poisonedPayload);

        // Poll for records on DLT topic to verify it lands there
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(15));
        consumer.close();

        assertFalse(records.isEmpty(), "Unparseable event was not routed to the DLT topic!");
        assertEquals(poisonedPayload, records.iterator().next().value(), "DLT payload did not match the sent poisoned pill!");
    }
}
