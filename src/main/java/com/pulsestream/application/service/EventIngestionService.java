package com.pulsestream.application.service;

import com.pulsestream.application.port.in.IngestEventUseCase;
import com.pulsestream.application.port.out.EventPublisher;
import com.pulsestream.domain.event.ActivityDetectedEvent;
import com.pulsestream.domain.event.OrderCreatedEvent;
import com.pulsestream.domain.event.PaymentConfirmedEvent;
import com.pulsestream.domain.event.RefundIssuedEvent;
import com.pulsestream.domain.exception.DuplicateEventException;
import com.pulsestream.domain.repository.IngestedEventRepository;
import com.pulsestream.observability.metrics.IngestionMetrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

@Service
public class EventIngestionService implements IngestEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionService.class);

    private final EventPublisher eventPublisher;
    private final IngestedEventRepository ingestedEventRepository;
    private final IngestionMetrics ingestionMetrics;

    public EventIngestionService(
            EventPublisher eventPublisher,
            IngestedEventRepository ingestedEventRepository,
            IngestionMetrics ingestionMetrics) {
        this.eventPublisher = eventPublisher;
        this.ingestedEventRepository = ingestedEventRepository;
        this.ingestionMetrics = ingestionMetrics;
    }

    @Override
    @Transactional
    public IngestionResult ingestOrder(OrderCommand command) {
        Timer.Sample sample = ingestionMetrics.startTimer();
        String eventId = StringUtils.hasText(command.eventId()) ? command.eventId() : UUID.randomUUID().toString();

        if (ingestedEventRepository.existsById(eventId)) {
            ingestionMetrics.incrementFailedIngestion("order-created", "DUPLICATE_EVENT");
            ingestionMetrics.stopIngestionTimer(sample, "order-created", "DUPLICATE_FAILED");
            throw new DuplicateEventException(eventId);
        }

        String orderId = resolveId(command.requestedOrderId());
        Instant now = Instant.now();

        log.info("Ingesting OrderCreatedEvent: orderId={}, eventId={}", orderId, eventId);

        try {
            if (!StringUtils.hasText(command.customerId())) {
                throw new IllegalArgumentException("Customer ID cannot be blank");
            }
            if (!StringUtils.hasText(command.productId())) {
                throw new IllegalArgumentException("Product ID cannot be blank");
            }
            if (command.quantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero");
            }
            if (command.price() == null || command.price().signum() <= 0) {
                throw new IllegalArgumentException("Price must be greater than zero");
            }

            OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                now,
                orderId,
                command.customerId(),
                command.productId(),
                command.quantity(),
                command.price(),
                command.price().multiply(java.math.BigDecimal.valueOf(command.quantity())),
                "PENDING"
            );

            eventPublisher.publish(event);
            ingestionMetrics.incrementIngestedEvent("order-created");
            ingestionMetrics.stopIngestionTimer(sample, "order-created", "SUCCESS");
            return new IngestionResult(eventId, orderId, "ACCEPTED", now);
        } catch (IllegalArgumentException e) {
            ingestionMetrics.incrementFailedIngestion("order-created", "VALIDATION_ERROR");
            ingestionMetrics.stopIngestionTimer(sample, "order-created", "VALIDATION_FAILED");
            throw e;
        } catch (DuplicateEventException e) {
            throw e;
        } catch (Exception e) {
            ingestionMetrics.incrementFailedIngestion("order-created", "INTERNAL_ERROR");
            ingestionMetrics.stopIngestionTimer(sample, "order-created", "INTERNAL_FAILED");
            throw e;
        }
    }

    @Override
    @Transactional
    public IngestionResult ingestPayment(PaymentCommand command) {
        Timer.Sample sample = ingestionMetrics.startTimer();
        String eventId = StringUtils.hasText(command.eventId()) ? command.eventId() : UUID.randomUUID().toString();

        if (ingestedEventRepository.existsById(eventId)) {
            ingestionMetrics.incrementFailedIngestion("payment-confirmed", "DUPLICATE_EVENT");
            ingestionMetrics.stopIngestionTimer(sample, "payment-confirmed", "DUPLICATE_FAILED");
            throw new DuplicateEventException(eventId);
        }

        String paymentId = resolveId(command.requestedPaymentId());
        Instant now = Instant.now();

        log.info("Ingesting PaymentConfirmedEvent: paymentId={}, eventId={}", paymentId, eventId);

        try {
            if (!StringUtils.hasText(command.orderId())) {
                throw new IllegalArgumentException("Order ID cannot be blank");
            }
            if (command.amount() == null || command.amount().signum() <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
            }
            if (!StringUtils.hasText(command.status())) {
                throw new IllegalArgumentException("Status cannot be blank");
            }

            PaymentConfirmedEvent event = new PaymentConfirmedEvent(
                eventId,
                now,
                paymentId,
                command.orderId(),
                command.amount(),
                command.status(),
                command.transactionRef()
            );

            eventPublisher.publish(event);
            ingestionMetrics.incrementIngestedEvent("payment-confirmed");
            ingestionMetrics.stopIngestionTimer(sample, "payment-confirmed", "SUCCESS");
            return new IngestionResult(eventId, paymentId, "ACCEPTED", now);
        } catch (IllegalArgumentException e) {
            ingestionMetrics.incrementFailedIngestion("payment-confirmed", "VALIDATION_ERROR");
            ingestionMetrics.stopIngestionTimer(sample, "payment-confirmed", "VALIDATION_FAILED");
            throw e;
        } catch (DuplicateEventException e) {
            throw e;
        } catch (Exception e) {
            ingestionMetrics.incrementFailedIngestion("payment-confirmed", "INTERNAL_ERROR");
            ingestionMetrics.stopIngestionTimer(sample, "payment-confirmed", "INTERNAL_FAILED");
            throw e;
        }
    }

    @Override
    @Transactional
    public IngestionResult ingestRefund(RefundCommand command) {
        Timer.Sample sample = ingestionMetrics.startTimer();
        String eventId = StringUtils.hasText(command.eventId()) ? command.eventId() : UUID.randomUUID().toString();

        if (ingestedEventRepository.existsById(eventId)) {
            ingestionMetrics.incrementFailedIngestion("refund-issued", "DUPLICATE_EVENT");
            ingestionMetrics.stopIngestionTimer(sample, "refund-issued", "DUPLICATE_FAILED");
            throw new DuplicateEventException(eventId);
        }

        String refundId = resolveId(command.requestedRefundId());
        Instant now = Instant.now();

        log.info("Ingesting RefundIssuedEvent: refundId={}, eventId={}", refundId, eventId);

        try {
            if (!StringUtils.hasText(command.paymentId())) {
                throw new IllegalArgumentException("Payment ID cannot be blank");
            }
            if (command.amount() == null || command.amount().signum() <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
            }
            if (!StringUtils.hasText(command.status())) {
                throw new IllegalArgumentException("Status cannot be blank");
            }

            RefundIssuedEvent event = new RefundIssuedEvent(
                eventId,
                now,
                refundId,
                command.paymentId(),
                command.amount(),
                command.reason(),
                command.status()
            );

            eventPublisher.publish(event);
            ingestionMetrics.incrementIngestedEvent("refund-issued");
            ingestionMetrics.stopIngestionTimer(sample, "refund-issued", "SUCCESS");
            return new IngestionResult(eventId, refundId, "ACCEPTED", now);
        } catch (IllegalArgumentException e) {
            ingestionMetrics.incrementFailedIngestion("refund-issued", "VALIDATION_ERROR");
            ingestionMetrics.stopIngestionTimer(sample, "refund-issued", "VALIDATION_FAILED");
            throw e;
        } catch (DuplicateEventException e) {
            throw e;
        } catch (Exception e) {
            ingestionMetrics.incrementFailedIngestion("refund-issued", "INTERNAL_ERROR");
            ingestionMetrics.stopIngestionTimer(sample, "refund-issued", "INTERNAL_FAILED");
            throw e;
        }
    }

    @Override
    @Transactional
    public IngestionResult ingestActivity(ActivityCommand command) {
        Timer.Sample sample = ingestionMetrics.startTimer();
        String eventId = StringUtils.hasText(command.eventId()) ? command.eventId() : UUID.randomUUID().toString();

        if (ingestedEventRepository.existsById(eventId)) {
            ingestionMetrics.incrementFailedIngestion("activity-detected", "DUPLICATE_EVENT");
            ingestionMetrics.stopIngestionTimer(sample, "activity-detected", "DUPLICATE_FAILED");
            throw new DuplicateEventException(eventId);
        }

        String activityId = resolveId(command.requestedActivityId());
        Instant now = Instant.now();

        log.info("Ingesting ActivityDetectedEvent: activityId={}, eventId={}", activityId, eventId);

        try {
            if (!StringUtils.hasText(command.customerId())) {
                throw new IllegalArgumentException("Customer ID cannot be blank");
            }
            if (!StringUtils.hasText(command.activityType())) {
                throw new IllegalArgumentException("Activity type cannot be blank");
            }

            ActivityDetectedEvent event = new ActivityDetectedEvent(
                eventId,
                now,
                activityId,
                command.customerId(),
                command.activityType(),
                command.metadata()
            );

            eventPublisher.publish(event);
            ingestionMetrics.incrementIngestedEvent("activity-detected");
            ingestionMetrics.stopIngestionTimer(sample, "activity-detected", "SUCCESS");
            return new IngestionResult(eventId, activityId, "ACCEPTED", now);
        } catch (IllegalArgumentException e) {
            ingestionMetrics.incrementFailedIngestion("activity-detected", "VALIDATION_ERROR");
            ingestionMetrics.stopIngestionTimer(sample, "activity-detected", "VALIDATION_FAILED");
            throw e;
        } catch (DuplicateEventException e) {
            throw e;
        } catch (Exception e) {
            ingestionMetrics.incrementFailedIngestion("activity-detected", "INTERNAL_ERROR");
            ingestionMetrics.stopIngestionTimer(sample, "activity-detected", "INTERNAL_FAILED");
            throw e;
        }
    }

    private String resolveId(String requestedId) {
        return StringUtils.hasText(requestedId) ? requestedId : UUID.randomUUID().toString();
    }
}
