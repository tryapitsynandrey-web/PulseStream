package com.pulsestream.api.controller;

import com.pulsestream.api.dto.ActivityIngestDto;
import com.pulsestream.api.dto.OrderIngestDto;
import com.pulsestream.api.dto.PaymentIngestDto;
import com.pulsestream.api.dto.RefundIngestDto;
import com.pulsestream.application.port.in.IngestEventUseCase;
import com.pulsestream.domain.event.ActivityDetectedEvent;
import com.pulsestream.domain.event.OrderCreatedEvent;
import com.pulsestream.domain.event.PaymentConfirmedEvent;
import com.pulsestream.domain.event.RefundIssuedEvent;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventIngestionController {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionController.class);

    private final IngestEventUseCase ingestEventUseCase;

    public EventIngestionController(IngestEventUseCase ingestEventUseCase) {
        this.ingestEventUseCase = ingestEventUseCase;
    }

    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> ingestOrder(@Valid @RequestBody OrderIngestDto dto) {
        String eventId = UUID.randomUUID().toString();
        String orderId = StringUtils.hasText(dto.orderId()) ? dto.orderId() : UUID.randomUUID().toString();
        
        log.info("REST Ingesting Order: orderId={}, eventId={}", orderId, eventId);
        
        OrderCreatedEvent event = new OrderCreatedEvent(
                eventId,
                Instant.now(),
                orderId,
                dto.customerId(),
                dto.productId(),
                dto.quantity(),
                dto.price(),
                dto.price().multiply(java.math.BigDecimal.valueOf(dto.quantity())),
                "PENDING"
        );
        
        ingestEventUseCase.ingestOrder(event);
        return ResponseEntity.accepted().body(createAcceptedResponse(eventId, orderId));
    }

    @PostMapping("/payments")
    public ResponseEntity<Map<String, Object>> ingestPayment(@Valid @RequestBody PaymentIngestDto dto) {
        String eventId = UUID.randomUUID().toString();
        String paymentId = StringUtils.hasText(dto.paymentId()) ? dto.paymentId() : UUID.randomUUID().toString();

        log.info("REST Ingesting Payment: paymentId={}, eventId={}", paymentId, eventId);

        PaymentConfirmedEvent event = new PaymentConfirmedEvent(
                eventId,
                Instant.now(),
                paymentId,
                dto.orderId(),
                dto.amount(),
                dto.status(),
                dto.transactionRef()
        );

        ingestEventUseCase.ingestPayment(event);
        return ResponseEntity.accepted().body(createAcceptedResponse(eventId, paymentId));
    }

    @PostMapping("/refunds")
    public ResponseEntity<Map<String, Object>> ingestRefund(@Valid @RequestBody RefundIngestDto dto) {
        String eventId = UUID.randomUUID().toString();
        String refundId = StringUtils.hasText(dto.refundId()) ? dto.refundId() : UUID.randomUUID().toString();

        log.info("REST Ingesting Refund: refundId={}, eventId={}", refundId, eventId);

        RefundIssuedEvent event = new RefundIssuedEvent(
                eventId,
                Instant.now(),
                refundId,
                dto.paymentId(),
                dto.amount(),
                dto.reason(),
                dto.status()
        );

        ingestEventUseCase.ingestRefund(event);
        return ResponseEntity.accepted().body(createAcceptedResponse(eventId, refundId));
    }

    @PostMapping("/activity")
    public ResponseEntity<Map<String, Object>> ingestActivity(@Valid @RequestBody ActivityIngestDto dto) {
        String eventId = UUID.randomUUID().toString();
        String activityId = StringUtils.hasText(dto.activityId()) ? dto.activityId() : UUID.randomUUID().toString();

        log.info("REST Ingesting Activity: activityId={}, eventId={}", activityId, eventId);

        ActivityDetectedEvent event = new ActivityDetectedEvent(
                eventId,
                Instant.now(),
                activityId,
                dto.customerId(),
                dto.activityType(),
                dto.metadata()
        );

        ingestEventUseCase.ingestActivity(event);
        return ResponseEntity.accepted().body(createAcceptedResponse(eventId, activityId));
    }

    private Map<String, Object> createAcceptedResponse(String eventId, String resourceId) {
        return Map.of(
                "eventId", eventId,
                "resourceId", resourceId,
                "status", "ACCEPTED",
                "timestamp", Instant.now().toString()
        );
    }
}
