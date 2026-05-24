package com.pulsestream.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;

public interface IngestEventUseCase {

    IngestionResult ingestOrder(OrderCommand command);
    IngestionResult ingestPayment(PaymentCommand command);
    IngestionResult ingestRefund(RefundCommand command);
    IngestionResult ingestActivity(ActivityCommand command);

    record OrderCommand(
        String eventId,
        String requestedOrderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal price
    ) {
        public OrderCommand(String requestedOrderId, String customerId, String productId, int quantity, BigDecimal price) {
            this(null, requestedOrderId, customerId, productId, quantity, price);
        }
    }

    record PaymentCommand(
        String eventId,
        String requestedPaymentId,
        String orderId,
        BigDecimal amount,
        String status,
        String transactionRef
    ) {
        public PaymentCommand(String requestedPaymentId, String orderId, BigDecimal amount, String status, String transactionRef) {
            this(null, requestedPaymentId, orderId, amount, status, transactionRef);
        }
    }

    record RefundCommand(
        String eventId,
        String requestedRefundId,
        String paymentId,
        BigDecimal amount,
        String reason,
        String status
    ) {
        public RefundCommand(String requestedRefundId, String paymentId, BigDecimal amount, String reason, String status) {
            this(null, requestedRefundId, paymentId, amount, reason, status);
        }
    }

    record ActivityCommand(
        String eventId,
        String requestedActivityId,
        String customerId,
        String activityType,
        String metadata
    ) {
        public ActivityCommand(String requestedActivityId, String customerId, String activityType, String metadata) {
            this(null, requestedActivityId, customerId, activityType, metadata);
        }
    }

    record IngestionResult(
        String eventId,
        String resourceId,
        String status,
        Instant timestamp
    ) {}
}
