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
import com.pulsestream.domain.repository.OrderRepository;
import com.pulsestream.domain.repository.PaymentRepository;
import com.pulsestream.domain.repository.RefundRepository;
import com.pulsestream.infrastructure.persistence.entity.IngestedEventEntity;
import com.pulsestream.infrastructure.persistence.repository.SpringDataIngestedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class KafkaEventConsumers {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumers.class);

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final CustomerActivityRepository customerActivityRepository;
    private final SpringDataIngestedEventRepository ingestedEventRepository;
    private final ObjectMapper objectMapper;

    public KafkaEventConsumers(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            RefundRepository refundRepository,
            CustomerActivityRepository customerActivityRepository,
            SpringDataIngestedEventRepository ingestedEventRepository,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.customerActivityRepository = customerActivityRepository;
        this.ingestedEventRepository = ingestedEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order-created", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeOrderCreated(String payload) {
        log.info("Received OrderCreated event payload from Kafka");
        try {
            OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);
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
            log.error("Failed to process OrderCreated event", e);
        }
    }

    @KafkaListener(topics = "payment-confirmed", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumePaymentConfirmed(String payload) {
        log.info("Received PaymentConfirmed event payload from Kafka");
        try {
            PaymentConfirmedEvent event = objectMapper.readValue(payload, PaymentConfirmedEvent.class);
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

            // Update order status if payment succeeded
            if ("SUCCESS".equalsIgnoreCase(event.status())) {
                orderRepository.findById(event.orderId()).ifPresent(order -> {
                    order.complete();
                    orderRepository.save(order);
                    log.info("Order {} status marked as COMPLETED following payment success", order.getId());
                });
            }

            log.info("Successfully persisted payment {} from event", payment.getId());
        } catch (Exception e) {
            log.error("Failed to process PaymentConfirmed event", e);
        }
    }

    @KafkaListener(topics = "refund-issued", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeRefundIssued(String payload) {
        log.info("Received RefundIssued event payload from Kafka");
        try {
            RefundIssuedEvent event = objectMapper.readValue(payload, RefundIssuedEvent.class);
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

            // If refund is approved, find the payment and mark order as cancelled
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
            log.error("Failed to process RefundIssued event", e);
        }
    }

    @KafkaListener(topics = "activity-detected", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeActivityDetected(String payload) {
        log.info("Received ActivityDetected event payload from Kafka");
        try {
            ActivityDetectedEvent event = objectMapper.readValue(payload, ActivityDetectedEvent.class);
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
            log.error("Failed to process ActivityDetected event", e);
        }
    }

    private void saveAuditLog(String eventId, String type, Instant occurredAt, String jsonPayload) {
        try {
            IngestedEventEntity audit = new IngestedEventEntity(eventId, type, occurredAt, jsonPayload);
            ingestedEventRepository.save(audit);
            log.debug("Saved audit log for event: {}", eventId);
        } catch (Exception e) {
            log.error("Failed to persist event audit log for ID: {}", eventId, e);
        }
    }
}
