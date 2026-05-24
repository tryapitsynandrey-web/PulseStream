package com.pulsestream.application.service;

import com.pulsestream.application.port.in.MetricsQueryUseCase;
import com.pulsestream.domain.repository.AnalyticsQueryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsService implements MetricsQueryUseCase {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
    private final AnalyticsQueryRepository analyticsQueryRepository;

    public AnalyticsService(AnalyticsQueryRepository analyticsQueryRepository) {
        this.analyticsQueryRepository = analyticsQueryRepository;
    }

    @Override
    public BigDecimal getTotalRevenue(Instant start, Instant end) {
        log.debug("Calculating total revenue from {} to {}", start, end);
        BigDecimal rev = analyticsQueryRepository.sumRevenue(start, end);
        return rev != null ? rev : BigDecimal.ZERO;
    }

    @Override
    public long getOrderCount(Instant start, Instant end) {
        log.debug("Counting orders from {} to {}", start, end);
        return analyticsQueryRepository.countOrders(start, end);
    }

    @Override
    public BigDecimal getRefundAmount(Instant start, Instant end) {
        log.debug("Calculating total refunds from {} to {}", start, end);
        BigDecimal ref = analyticsQueryRepository.sumRefunds(start, end);
        return ref != null ? ref : BigDecimal.ZERO;
    }

    @Override
    public List<ProductMetric> getTopProducts(int limit) {
        log.debug("Fetching top {} products", limit);
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }
        return analyticsQueryRepository.findTopProducts(limit);
    }

    @Override
    public long getActiveCustomersCount(Instant start, Instant end) {
        log.debug("Calculating active customer counts from {} to {}", start, end);
        return analyticsQueryRepository.countUniqueCustomers(start, end);
    }

    @Override
    public List<ActivityMetric> getCustomerActivityBreakdown(Instant start, Instant end) {
        log.debug("Fetching activity breakdown from {} to {}", start, end);
        return analyticsQueryRepository.countActivitiesByType(start, end);
    }

    @Override
    public Page<IngestedEventInfo> getIngestedEvents(Pageable pageable) {
        log.debug("Fetching page of ingested events: {}", pageable);
        return analyticsQueryRepository.findIngestedEvents(pageable);
    }
}
