package com.pulsestream.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsestream.domain.event.ActivityDetectedEvent;
import com.pulsestream.domain.event.OrderCreatedEvent;
import com.pulsestream.domain.event.PaymentConfirmedEvent;
import com.pulsestream.domain.event.RefundIssuedEvent;
import com.pulsestream.domain.model.CustomerActivity;
import com.pulsestream.domain.model.Order;
import com.pulsestream.domain.model.Payment;
import com.pulsestream.domain.model.Refund;
import com.pulsestream.domain.repository.CustomerActivityRepository;
import com.pulsestream.domain.repository.IngestedEventRepository;
import com.pulsestream.domain.repository.OrderRepository;
import com.pulsestream.domain.repository.PaymentRepository;
import com.pulsestream.domain.repository.RefundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class KafkaEventConsumers {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumers.class);
    private static final String CORRELATION_ID_KEY = "correlationId";

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final CustomerActivityRepository customerActivityRepository;
    private final IngestedEventRepository ingestedEventRepository;
    private final ObjectMapper objectMapper;

    public KafkaEventConsumers(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            CustomerActivityRepository customerActivityRepository,
            IngestedEventRepository ingestedEventRepository,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.customerActivityRepository = customerActivityRepository;
        this.ingestedEventRepository = ingestedEventRepository;
        this.objectMapper = objectMapper;
    }

    private void setupCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        } else {
            MDC.put(CORRELATION_ID_KEY, UUID.randomUUID().toString());
        }
    }

    private void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_KEY);
    }

    @KafkaListener(topics = "order-created", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeOrderCreated(
            @Payload String payload,
            @Header(name = "X-Correlation-Id", required = false) String correlationId) {
        setupCorrelationId(correlationId);
        log.info("Received OrderCreated event payload from Kafka");
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
            log.info("Processing OrderCreatedEvent {} with schema version {}", event.eventId(), event.schemaVersion());
            saveAuditLog(event.eventId(), "OrderCreatedEvent", event.occurredAt(), payload);

            Order order = new Order(
                event.orderId(),
                event.customerId(),
                event.productId(),
                event.quantity(),
                event.price(),
                event.status(),
                event.occurredAt(),
                event.occurredAt()
            );
            orderRepository.save(order);
            log.info("Successfully persisted order {} from event", order.getId());
        } catch (Exception e) {
            log.error("Failed to process OrderCreated event. Bubbling up to trigger DLQ processing.", e);
            throw new RuntimeException("Error processing OrderCreated event", e);
        } finally {
            clearCorrelationId();
        }
    }

    @KafkaListener(topics = "payment-confirmed", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumePaymentConfirmed(
            @Payload String payload,
            @Header(name = "X-Correlation-Id", required = false) String correlationId) {
        setupCorrelationId(correlationId);
        log.info("Received PaymentConfirmed event payload from Kafka");
        try {
            PaymentConfirmedEvent event = objectMapper.readValue(payload, PaymentConfirmedEvent.class);
            log.info("Processing PaymentConfirmedEvent {} with schema version {}", event.eventId(), event.schemaVersion());
            saveAuditLog(event.eventId(), "PaymentConfirmedEvent", event.occurredAt(), payload);

            Payment payment = new Payment(
                event.paymentId(),
                event.orderId(),
                event.amount(),
                event.status(),
                event.transactionRef(),
                event.occurredAt(),
                event.occurredAt()
            );
            paymentRepository.save(payment);

            if ("SUCCESS".equalsIgnoreCase(event.status())) {
                orderRepository.findById(event.orderId()).ifPresent(order -> {
                    order.complete();
                    orderRepository.save(order);
                    log.info("Order {} status marked as COMPLETED following payment success", order.getId());
                });
            }

            log.info("Successfully persisted payment {} from event", payment.getId());
        } catch (Exception e) {
            log.error("Failed to process PaymentConfirmed event. Bubbling up to trigger DLQ processing.", e);
            throw new RuntimeException("Error processing PaymentConfirmed event", e);
        } finally {
            clearCorrelationId();
        }
    }

    @KafkaListener(topics = "refund-issued", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeRefundIssued(
            @Payload String payload,
            @Header(name = "X-Correlation-Id", required = false) String correlationId) {
        setupCorrelationId(correlationId);
        log.info("Received RefundIssued event payload from Kafka");
        try {
            RefundIssuedEvent event = objectMapper.readValue(payload, RefundIssuedEvent.class);
            log.info("Processing RefundIssuedEvent {} with schema version {}", event.eventId(), event.schemaVersion());
            saveAuditLog(event.eventId(), "RefundIssuedEvent", event.occurredAt(), payload);

            Refund refund = new Refund(
                event.refundId(),
                event.paymentId(),
                event.amount(),
                event.reason(),
                event.status(),
                event.occurredAt(),
                event.occurredAt()
            );
            refundRepository.save(refund);

            if ("APPROVED".equalsIgnoreCase(event.status())) {
                paymentRepository.findById(event.paymentId()).ifPresent(payment -> {
                    orderRepository.findById(payment.getOrderId()).ifPresent(order -> {
                        order.cancel();
                        orderRepository.save(order);
                        log.info("Order {} status marked as CANCELLED following refund approval", order.getId());
                    });
                });
            }

            log.info("Successfully persisted refund {} from event", refund.getId());
        } catch (Exception e) {
            log.error("Failed to process RefundIssued event. Bubbling up to trigger DLQ processing.", e);
            throw new RuntimeException("Error processing RefundIssued event", e);
        } finally {
            clearCorrelationId();
        }
    }

    @KafkaListener(topics = "activity-detected", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeActivityDetected(
            @Payload String payload,
            @Header(name = "X-Correlation-Id", required = false) String correlationId) {
        setupCorrelationId(correlationId);
        log.info("Received ActivityDetected event payload from Kafka");
        try {
            ActivityDetectedEvent event = objectMapper.readValue(payload, ActivityDetectedEvent.class);
            log.info("Processing ActivityDetectedEvent {} with schema version {}", event.eventId(), event.schemaVersion());
            saveAuditLog(event.eventId(), "ActivityDetectedEvent", event.occurredAt(), payload);

            CustomerActivity activity = new CustomerActivity(
                event.activityId(),
                event.customerId(),
                event.activityType(),
                event.metadata(),
                event.occurredAt()
            );
            customerActivityRepository.save(activity);
            log.info("Successfully persisted customer activity {} from event", activity.getId());
        } catch (Exception e) {
            log.error("Failed to process ActivityDetected event. Bubbling up to trigger DLQ processing.", e);
            throw new RuntimeException("Error processing ActivityDetected event", e);
        } finally {
            clearCorrelationId();
        }
    }

    @KafkaListener(topics = "order-created.DLT", groupId = "pulsestream-dlq-group")
    public void consumeOrderCreatedDlt(String payload) {
        log.error("CRITICAL DLQ ALERT: Dead letter queue received failed OrderCreated payload: {}", payload);
    }

    @KafkaListener(topics = "payment-confirmed.DLT", groupId = "pulsestream-dlq-group")
    public void consumePaymentConfirmedDlt(String payload) {
        log.error("CRITICAL DLQ ALERT: Dead letter queue received failed PaymentConfirmed payload: {}", payload);
    }

    @KafkaListener(topics = "refund-issued.DLT", groupId = "pulsestream-dlq-group")
    public void consumeRefundIssuedDlt(String payload) {
        log.error("CRITICAL DLQ ALERT: Dead letter queue received failed RefundIssued payload: {}", payload);
    }

    @KafkaListener(topics = "activity-detected.DLT", groupId = "pulsestream-dlq-group")
    public void consumeActivityDetectedDlt(String payload) {
        log.error("CRITICAL DLQ ALERT: Dead letter queue received failed ActivityDetected payload: {}", payload);
    }

    private void saveAuditLog(String eventId, String type, Instant occurredAt, String jsonPayload) {
        try {
            ingestedEventRepository.save(eventId, type, occurredAt, jsonPayload);
            log.debug("Saved audit log for event: {}", eventId);
        } catch (Exception e) {
            log.error("Failed to persist event audit log for ID: {}", eventId, e);
            throw e; // Bubble up to force retry/DLQ of the parent event if audit fails
        }
    }
}
