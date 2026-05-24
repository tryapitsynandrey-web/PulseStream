package com.pulsestream.infrastructure.persistence;

import com.pulsestream.AbstractIntegrationTest;
import com.pulsestream.domain.model.Order;
import com.pulsestream.domain.repository.AnalyticsQueryRepository;
import com.pulsestream.domain.repository.OrderRepository;
import com.pulsestream.infrastructure.persistence.entity.UserEntity;
import com.pulsestream.infrastructure.persistence.repository.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
class PostgresqlIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AnalyticsQueryRepository analyticsQueryRepository;

    @Test
    void shouldVerifyPreSeededUsersExist() {
        Optional<UserEntity> admin = userRepository.findByUsername("admin");
        assertTrue(admin.isPresent());
        assertEquals("ROLE_ADMIN", admin.get().getRole());

        Optional<UserEntity> analyst = userRepository.findByUsername("analyst");
        assertTrue(analyst.isPresent());
        assertEquals("ROLE_ANALYST", analyst.get().getRole());
    }

    @Test
    void shouldSaveAndQueryOrdersForMetricsCalculation() {
        Instant now = Instant.now();
        
        Order completedOrder = new Order(
                "order-cmp-1",
                "cust-1",
                "prod-1",
                3,
                BigDecimal.valueOf(10.00),
                "COMPLETED",
                now,
                now
        );

        Order pendingOrder = new Order(
                "order-pnd-2",
                "cust-2",
                "prod-2",
                1,
                BigDecimal.valueOf(50.00),
                "PENDING",
                now,
                now
        );

        orderRepository.save(completedOrder);
        orderRepository.save(pendingOrder);

        // Fetch using repository
        Optional<Order> fetched = orderRepository.findById("order-cmp-1");
        assertTrue(fetched.isPresent());
        assertEquals("COMPLETED", fetched.get().getStatus());

        // Perform Metrics calculation
        Instant start = now.minus(1, ChronoUnit.HOURS);
        Instant end = now.plus(1, ChronoUnit.HOURS);

        BigDecimal revenue = analyticsQueryRepository.sumRevenue(start, end);
        long count = analyticsQueryRepository.countOrders(start, end);

        // Revenue counts COMPLETED orders only: 10.00 * 3 = 30.00
        assertEquals(0, BigDecimal.valueOf(30.00).compareTo(revenue));
        // Total order count counts both PENDING and COMPLETED orders: 2
        assertEquals(2, count);
    }
}
