package com.pulsestream.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void shouldCreateOrderSuccessfully() {
        Order order = new Order(
                "order-123",
                "customer-456",
                "product-789",
                2,
                BigDecimal.valueOf(15.50),
                "PENDING",
                Instant.now(),
                Instant.now()
        );

        assertEquals("order-123", order.getId());
        assertEquals("customer-456", order.getCustomerId());
        assertEquals("product-789", order.getProductId());
        assertEquals(2, order.getQuantity());
        assertEquals(BigDecimal.valueOf(15.50), order.getPrice());
        assertEquals(BigDecimal.valueOf(31.00), order.getTotalAmount());
        assertEquals("PENDING", order.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Order(
                "order-123",
                "customer-456",
                "product-789",
                0,
                BigDecimal.valueOf(15.50),
                "PENDING",
                Instant.now(),
                Instant.now()
        ));

        assertThrows(IllegalArgumentException.class, () -> new Order(
                "order-123",
                "customer-456",
                "product-789",
                -5,
                BigDecimal.valueOf(15.50),
                "PENDING",
                Instant.now(),
                Instant.now()
        ));
    }

    @Test
    void shouldCompleteOrderSuccessfully() {
        Order order = new Order(
                "order-123",
                "customer-456",
                "product-789",
                2,
                BigDecimal.valueOf(15.50),
                "PENDING",
                Instant.now(),
                Instant.now()
        );

        order.complete();
        assertEquals("COMPLETED", order.getStatus());
    }

    @Test
    void shouldCancelOrderSuccessfully() {
        Order order = new Order(
                "order-123",
                "customer-456",
                "product-789",
                2,
                BigDecimal.valueOf(15.50),
                "PENDING",
                Instant.now(),
                Instant.now()
        );

        order.cancel();
        assertEquals("CANCELLED", order.getStatus());
    }

    @Test
    void shouldThrowExceptionOnInvalidCompleteStatusTransition() {
        Order order = new Order(
                "order-123",
                "customer-456",
                "product-789",
                2,
                BigDecimal.valueOf(15.50),
                "CANCELLED",
                Instant.now(),
                Instant.now()
        );

        assertThrows(IllegalStateException.class, order::complete);
    }

    @Test
    void shouldThrowExceptionOnInvalidCancelStatusTransition() {
        Order order = new Order(
                "order-123",
                "customer-456",
                "product-789",
                2,
                BigDecimal.valueOf(15.50),
                "COMPLETED",
                Instant.now(),
                Instant.now()
        );

        assertThrows(IllegalStateException.class, order::cancel);
    }
}
