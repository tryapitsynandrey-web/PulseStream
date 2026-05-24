package com.pulsestream.application.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface MetricsQueryUseCase {
    BigDecimal getTotalRevenue(Instant start, Instant end);
    long getOrderCount(Instant start, Instant end);
    BigDecimal getRefundAmount(Instant start, Instant end);
    List<ProductMetric> getTopProducts(int limit);
    long getActiveCustomersCount(Instant start, Instant end);
    List<ActivityMetric> getCustomerActivityBreakdown(Instant start, Instant end);

    record ProductMetric(String productId, long quantitySold, BigDecimal revenue) {}
    record ActivityMetric(String activityType, long count) {}
}
