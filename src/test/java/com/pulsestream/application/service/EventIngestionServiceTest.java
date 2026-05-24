package com.pulsestream.application.service;

import com.pulsestream.application.port.out.EventPublisher;
import com.pulsestream.domain.event.OrderCreatedEvent;
import com.pulsestream.observability.metrics.IngestionMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventIngestionServiceTest {

    private EventPublisher eventPublisher;
    private IngestionMetrics ingestionMetrics;
    private EventIngestionService eventIngestionService;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(EventPublisher.class);
        ingestionMetrics = mock(IngestionMetrics.class);
        eventIngestionService = new EventIngestionService(eventPublisher, ingestionMetrics);
    }

    @Test
    void shouldIngestValidOrderEventSuccessfully() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                "evt-1",
                Instant.now(),
                "order-1",
                "customer-1",
                "product-1",
                2,
                BigDecimal.valueOf(10.00),
                BigDecimal.valueOf(20.00),
                "PENDING"
        );

        eventIngestionService.ingestOrder(event);

        verify(eventPublisher, times(1)).publish(event);
        verify(ingestionMetrics, times(1)).incrementIngestedEvent("order-created");
    }

    @Test
    void shouldThrowExceptionWhenOrderEventHasEmptyCustomerId() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                "evt-1",
                Instant.now(),
                "order-1",
                "",
                "product-1",
                2,
                BigDecimal.valueOf(10.00),
                BigDecimal.valueOf(20.00),
                "PENDING"
        );

        assertThrows(IllegalArgumentException.class, () -> eventIngestionService.ingestOrder(event));
        verify(eventPublisher, never()).publish(any());
        verify(ingestionMetrics, never()).incrementIngestedEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderEventHasNegativePrice() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                "evt-1",
                Instant.now(),
                "order-1",
                "customer-1",
                "product-1",
                2,
                BigDecimal.valueOf(-1.00),
                BigDecimal.valueOf(-2.00),
                "PENDING"
        );

        assertThrows(IllegalArgumentException.class, () -> eventIngestionService.ingestOrder(event));
        verify(eventPublisher, never()).publish(any());
        verify(ingestionMetrics, never()).incrementIngestedEvent(any());
    }
}
