package com.pulsestream.application.port.in;

import com.pulsestream.domain.event.ActivityDetectedEvent;
import com.pulsestream.domain.event.OrderCreatedEvent;
import com.pulsestream.domain.event.PaymentConfirmedEvent;
import com.pulsestream.domain.event.RefundIssuedEvent;

public interface IngestEventUseCase {
    void ingestOrder(OrderCreatedEvent event);
    void ingestPayment(PaymentConfirmedEvent event);
    void ingestRefund(RefundIssuedEvent event);
    void ingestActivity(ActivityDetectedEvent event);
}
