package com.pulsestream.api.controller;

import com.pulsestream.api.dto.MetricResponses.ActiveCustomersResponse;
import com.pulsestream.api.dto.MetricResponses.OrderCountResponse;
import com.pulsestream.api.dto.MetricResponses.RefundAmountResponse;
import com.pulsestream.api.dto.MetricResponses.RevenueResponse;
import com.pulsestream.application.port.in.MetricsQueryUseCase;
import com.pulsestream.application.port.in.MetricsQueryUseCase.ActivityMetric;
import com.pulsestream.application.port.in.MetricsQueryUseCase.ProductMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/metrics")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final MetricsQueryUseCase metricsQueryUseCase;

    public AnalyticsController(MetricsQueryUseCase metricsQueryUseCase) {
        this.metricsQueryUseCase = metricsQueryUseCase;
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueResponse> getRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        
        Instant startRange = getStartOrDefault(start);
        Instant endRange = getEndOrDefault(end);
        
        log.info("Request for total revenue from {} to {}", startRange, endRange);
        BigDecimal rev = metricsQueryUseCase.getTotalRevenue(startRange, endRange);
        return ResponseEntity.ok(new RevenueResponse(rev, startRange, endRange));
    }

    @GetMapping("/orders")
    public ResponseEntity<OrderCountResponse> getOrderCount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        Instant startRange = getStartOrDefault(start);
        Instant endRange = getEndOrDefault(end);

        log.info("Request for order counts from {} to {}", startRange, endRange);
        long count = metricsQueryUseCase.getOrderCount(startRange, endRange);
        return ResponseEntity.ok(new OrderCountResponse(count, startRange, endRange));
    }

    @GetMapping("/refunds")
    public ResponseEntity<RefundAmountResponse> getRefundAmount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        Instant startRange = getStartOrDefault(start);
        Instant endRange = getEndOrDefault(end);

        log.info("Request for refund amounts from {} to {}", startRange, endRange);
        BigDecimal amount = metricsQueryUseCase.getRefundAmount(startRange, endRange);
        return ResponseEntity.ok(new RefundAmountResponse(amount, startRange, endRange));
    }

    @GetMapping("/top-products")
    public ResponseEntity<List<ProductMetric>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {

        log.info("Request for top products with limit: {}", limit);
        List<ProductMetric> products = metricsQueryUseCase.getTopProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/customer-activity")
    public ResponseEntity<List<ActivityMetric>> getCustomerActivities(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        Instant startRange = getStartOrDefault(start);
        Instant endRange = getEndOrDefault(end);

        log.info("Request for customer activity counts from {} to {}", startRange, endRange);
        List<ActivityMetric> breakdown = metricsQueryUseCase.getCustomerActivityBreakdown(startRange, endRange);
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/active-customers")
    public ResponseEntity<ActiveCustomersResponse> getActiveCustomers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        Instant startRange = getStartOrDefault(start);
        Instant endRange = getEndOrDefault(end);

        log.info("Request for active customer counts from {} to {}", startRange, endRange);
        long count = metricsQueryUseCase.getActiveCustomersCount(startRange, endRange);
        return ResponseEntity.ok(new ActiveCustomersResponse(count, startRange, endRange));
    }

    private Instant getStartOrDefault(Instant start) {
        return start != null ? start : Instant.now().minus(30, ChronoUnit.DAYS);
    }

    private Instant getEndOrDefault(Instant end) {
        return end != null ? end : Instant.now();
    }
}
