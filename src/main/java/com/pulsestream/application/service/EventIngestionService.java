package com.pulsestream.application.service;

import com.pulsestream.application.port.in.IngestEventUseCase;
import com.pulsestream.application.port.out.EventPublisher;
import com.pulsestream.domain.event.ActivityDetectedEvent;
import com.pulsestream.domain.event.OrderCreatedEvent;
import com.pulsestream.domain.event.PaymentConfirmedEvent;
import com.pulsestream.domain.event.RefundIssuedEvent;
import com.pulsestream.observability.metrics.IngestionMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EventIngestionService implements IngestEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionService.class);
    private final EventPublisher eventPublisher;
    private final IngestionMetrics ingestionMetrics;

    public EventIngestionService(EventPublisher eventPublisher, IngestionMetrics ingestionMetrics) {
        this.eventPublisher = eventPublisher;
        this.ingestionMetrics = ingestionMetrics;
    }

    @Override
    public void ingestOrder(OrderCreatedEvent event) {
        log.info("Ingesting OrderCreatedEvent: id={}, customer={}", event.orderId(), event.customerId());
        validateNotBlank(event.orderId(), "Order ID");
        validateNotBlank(event.customerId(), "Customer ID");
        validateNotBlank(event.productId(), "Product ID");
        if (event.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (event.price() == null || event.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        eventPublisher.publish(event);
        ingestionMetrics.incrementIngestedEvent("order-created");
    }

    @Override
    public void ingestPayment(PaymentConfirmedEvent event) {
        log.info("Ingesting PaymentConfirmedEvent: id={}, order={}", event.paymentId(), event.orderId());
        validateNotBlank(event.paymentId(), "Payment ID");
        validateNotBlank(event.orderId(), "Order ID");
        if (event.amount() == null || event.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        validateNotBlank(event.status(), "Status");
        eventPublisher.publish(event);
        ingestionMetrics.incrementIngestedEvent("payment-confirmed");
    }

    @Override
    public void ingestRefund(RefundIssuedEvent event) {
        log.info("Ingesting RefundIssuedEvent: id={}, payment={}", event.refundId(), event.paymentId());
        validateNotBlank(event.refundId(), "Refund ID");
        validateNotBlank(event.paymentId(), "Payment ID");
        if (event.amount() == null || event.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        validateNotBlank(event.status(), "Status");
        eventPublisher.publish(event);
        ingestionMetrics.incrementIngestedEvent("refund-issued");
    }

    @Override
    public void ingestActivity(ActivityDetectedEvent event) {
        log.info("Ingesting ActivityDetectedEvent: id={}, customer={}, type={}", event.activityId(), event.customerId(), event.activityType());
        validateNotBlank(event.activityId(), "Activity ID");
        validateNotBlank(event.customerId(), "Customer ID");
        validateNotBlank(event.activityType(), "Activity Type");
        eventPublisher.publish(event);
        ingestionMetrics.incrementIngestedEvent("activity-detected");
    }

    private void validateNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
    }
}
