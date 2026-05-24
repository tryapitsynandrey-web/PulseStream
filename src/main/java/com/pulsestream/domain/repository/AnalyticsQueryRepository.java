package com.pulsestream.domain.repository;

import com.pulsestream.application.port.in.MetricsQueryUseCase.ActivityMetric;
import com.pulsestream.application.port.in.MetricsQueryUseCase.ProductMetric;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface AnalyticsQueryRepository {
    BigDecimal sumRevenue(Instant start, Instant end);
    long countOrders(Instant start, Instant end);
    BigDecimal sumRefunds(Instant start, Instant end);
    List<ProductMetric> findTopProducts(int limit);
    long countUniqueCustomers(Instant start, Instant end);
    List<ActivityMetric> countActivitiesByType(Instant start, Instant end);
}
