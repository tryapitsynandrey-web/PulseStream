package com.pulsestream.api.controller;

import com.pulsestream.api.dto.ActivityIngestDto;
import com.pulsestream.api.dto.OrderIngestDto;
import com.pulsestream.api.dto.PaymentIngestDto;
import com.pulsestream.api.dto.RefundIngestDto;
import com.pulsestream.application.port.in.IngestEventUseCase;
import com.pulsestream.application.port.in.IngestEventUseCase.IngestionResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Event Ingestion Gateway", description = "Endpoints to ingest various operational business events asynchronously into the system.")
public class EventIngestionController {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionController.class);

    private final IngestEventUseCase ingestEventUseCase;

    public EventIngestionController(IngestEventUseCase ingestEventUseCase) {
        this.ingestEventUseCase = ingestEventUseCase;
    }

    @PostMapping("/orders")
    @Operation(
        summary = "Ingest OrderCreated event",
        description = "Processes a client purchase payload, generates a unique event sequence, and publishes it asynchronously to Kafka. Access restricted to ADMIN."
    )
    @ApiResponse(responseCode = "202", description = "Order event successfully accepted and queued for persistence.")
    @ApiResponse(responseCode = "400", description = "Invalid schema payload or validation constraints violated.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient user privileges.")
    @ApiResponse(responseCode = "409", description = "Duplicate event ingestion detected.")
    public ResponseEntity<Map<String, Object>> ingestOrder(
            @RequestHeader(name = "X-Event-Id", required = false) String eventId,
            @Valid @RequestBody OrderIngestDto dto) {
        log.info("POST /api/v1/events/orders received with X-Event-Id={}", eventId);
        IngestionResult result = ingestEventUseCase.ingestOrder(new IngestEventUseCase.OrderCommand(
            eventId,
            dto.orderId(),
            dto.customerId(),
            dto.productId(),
            dto.quantity(),
            dto.price()
        ));
        return ResponseEntity.accepted().body(toResponse(result));
    }

    @PostMapping("/payments")
    @Operation(
        summary = "Ingest PaymentConfirmed event",
        description = "Ingests payment gateway logs, updates corresponding order statuses, and publishes payment metadata to Kafka. Access restricted to ADMIN."
    )
    @ApiResponse(responseCode = "202", description = "Payment event accepted and scheduled for event streaming.")
    @ApiResponse(responseCode = "400", description = "Invalid schema payload or validation constraints violated.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient user privileges.")
    @ApiResponse(responseCode = "409", description = "Duplicate event ingestion detected.")
    public ResponseEntity<Map<String, Object>> ingestPayment(
            @RequestHeader(name = "X-Event-Id", required = false) String eventId,
            @Valid @RequestBody PaymentIngestDto dto) {
        log.info("POST /api/v1/events/payments received with X-Event-Id={}", eventId);
        IngestionResult result = ingestEventUseCase.ingestPayment(new IngestEventUseCase.PaymentCommand(
            eventId,
            dto.paymentId(),
            dto.orderId(),
            dto.amount(),
            dto.status(),
            dto.transactionRef()
        ));
        return ResponseEntity.accepted().body(toResponse(result));
    }

    @PostMapping("/refunds")
    @Operation(
        summary = "Ingest RefundIssued event",
        description = "Triggers a business refund event log, transitions related payment logs, and publishes onto Kafka streams. Access restricted to ADMIN."
    )
    @ApiResponse(responseCode = "202", description = "Refund event accepted and mapped to message queue.")
    @ApiResponse(responseCode = "400", description = "Invalid schema payload or validation constraints violated.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient user privileges.")
    @ApiResponse(responseCode = "409", description = "Duplicate event ingestion detected.")
    public ResponseEntity<Map<String, Object>> ingestRefund(
            @RequestHeader(name = "X-Event-Id", required = false) String eventId,
            @Valid @RequestBody RefundIngestDto dto) {
        log.info("POST /api/v1/events/refunds received with X-Event-Id={}", eventId);
        IngestionResult result = ingestEventUseCase.ingestRefund(new IngestEventUseCase.RefundCommand(
            eventId,
            dto.refundId(),
            dto.paymentId(),
            dto.amount(),
            dto.reason(),
            dto.status()
        ));
        return ResponseEntity.accepted().body(toResponse(result));
    }

    @PostMapping("/activity")
    @Operation(
        summary = "Ingest ActivityDetected event",
        description = "Persists non-financial customer interaction activities such as clicks or banner events, and publishes tracking metadata. Access restricted to ADMIN."
    )
    @ApiResponse(responseCode = "202", description = "Customer interaction activity accepted.")
    @ApiResponse(responseCode = "400", description = "Invalid schema payload or validation constraints violated.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient user privileges.")
    @ApiResponse(responseCode = "409", description = "Duplicate event ingestion detected.")
    public ResponseEntity<Map<String, Object>> ingestActivity(
            @RequestHeader(name = "X-Event-Id", required = false) String eventId,
            @Valid @RequestBody ActivityIngestDto dto) {
        log.info("POST /api/v1/events/activity received with X-Event-Id={}", eventId);
        IngestionResult result = ingestEventUseCase.ingestActivity(new IngestEventUseCase.ActivityCommand(
            eventId,
            dto.activityId(),
            dto.customerId(),
            dto.activityType(),
            dto.metadata()
        ));
        return ResponseEntity.accepted().body(toResponse(result));
    }

    private Map<String, Object> toResponse(IngestionResult result) {
        return Map.of(
            "eventId", result.eventId(),
            "resourceId", result.resourceId(),
            "status", result.status(),
            "timestamp", result.timestamp().toString()
        );
    }
}
