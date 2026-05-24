package com.pulsestream.application.service;

import com.pulsestream.application.port.in.IngestEventUseCase;
import com.pulsestream.application.port.out.EventPublisher;
import com.pulsestream.domain.event.OrderCreatedEvent;
import com.pulsestream.domain.repository.IngestedEventRepository;
import com.pulsestream.observability.metrics.IngestionMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventIngestionServiceTest {

    private EventPublisher eventPublisher;
    private IngestedEventRepository ingestedEventRepository;
    private IngestionMetrics ingestionMetrics;
    private EventIngestionService eventIngestionService;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(EventPublisher.class);
        ingestedEventRepository = mock(IngestedEventRepository.class);
        ingestionMetrics = mock(IngestionMetrics.class);
        eventIngestionService = new EventIngestionService(eventPublisher, ingestedEventRepository, ingestionMetrics);
    }

    @Test
    void shouldIngestValidOrderEventSuccessfully() {
        IngestEventUseCase.OrderCommand command = new IngestEventUseCase.OrderCommand(
                "event-1",
                "order-1",
                "customer-1",
                "product-1",
                2,
                BigDecimal.valueOf(10.00)
        );

        IngestEventUseCase.IngestionResult result = eventIngestionService.ingestOrder(command);

        verify(eventPublisher, times(1)).publish(any(OrderCreatedEvent.class));
        verify(ingestionMetrics, times(1)).incrementIngestedEvent("order-created");
        assertNotNull(result);
        assertEquals("ACCEPTED", result.status());
    }

    @Test
    void shouldThrowExceptionWhenOrderEventHasEmptyCustomerId() {
        IngestEventUseCase.OrderCommand command = new IngestEventUseCase.OrderCommand(
                "event-2",
                "order-1",
                "",
                "product-1",
                2,
                BigDecimal.valueOf(10.00)
        );

        assertThrows(IllegalArgumentException.class, () -> eventIngestionService.ingestOrder(command));
        verify(eventPublisher, never()).publish(any());
        verify(ingestionMetrics, never()).incrementIngestedEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderEventHasNegativePrice() {
        IngestEventUseCase.OrderCommand command = new IngestEventUseCase.OrderCommand(
                "event-3",
                "order-1",
                "customer-1",
                "product-1",
                2,
                BigDecimal.valueOf(-1.00)
        );

        assertThrows(IllegalArgumentException.class, () -> eventIngestionService.ingestOrder(command));
        verify(eventPublisher, never()).publish(any());
        verify(ingestionMetrics, never()).incrementIngestedEvent(any());
    }
}
