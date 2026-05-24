package com.pulsestream.api.controller;

import com.pulsestream.api.dto.MetricResponses.ActiveCustomersResponse;
import com.pulsestream.api.dto.MetricResponses.OrderCountResponse;
import com.pulsestream.api.dto.MetricResponses.RefundAmountResponse;
import com.pulsestream.api.dto.MetricResponses.RevenueResponse;
import com.pulsestream.application.port.in.MetricsQueryUseCase;
import com.pulsestream.application.port.in.MetricsQueryUseCase.ActivityMetric;
import com.pulsestream.application.port.in.MetricsQueryUseCase.ProductMetric;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.pulsestream.application.port.in.MetricsQueryUseCase.IngestedEventInfo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/metrics")
@Tag(name = "Analytics & Metrics Engine", description = "Endpoints for querying business intelligence metrics, aggregated revenue, and tracking operations. Access restricted to ADMIN or ANALYST.")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final MetricsQueryUseCase metricsQueryUseCase;

    public AnalyticsController(MetricsQueryUseCase metricsQueryUseCase) {
        this.metricsQueryUseCase = metricsQueryUseCase;
    }

    @GetMapping("/revenue")
    @Operation(
        summary = "Query aggregate completed revenue",
        description = "Returns the sum total of all completed order events within the specified time window. Access restricted to ADMIN or ANALYST."
    )
    @ApiResponse(responseCode = "200", description = "Query executed successfully.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient user privileges.")
    public ResponseEntity<RevenueResponse> getRevenue(
            @Parameter(description = "Beginning ISO-8601 boundary timestamp. Defaults to 30 days ago.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @Parameter(description = "Closing ISO-8601 boundary timestamp. Defaults to now.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        Instant startRange = getStartOrDefault(start);
        Instant endRange = getEndOrDefault(end);

        log.info("Request for total revenue from {} to {}", startRange, endRange);
        BigDecimal rev = metricsQueryUseCase.getTotalRevenue(startRange, endRange);
        return ResponseEntity.ok(new RevenueResponse(rev, startRange, endRange));
    }

    @GetMapping("/orders")
    @Operation(
        summary = "Query aggregate order count",
        description = "Returns the total count of order events ingested within the specified time window. Access restricted to ADMIN or ANALYST."
    )
    @ApiResponse(responseCode = "200", description = "Query executed successfully.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient user privileges.")
    public ResponseEntity<OrderCountResponse> getOrderCount(
            @Parameter(description = "Beginning ISO-8601 boundary timestamp. Defaults to 30 days ago.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @Parameter(description = "Closing ISO-8601 boundary timestamp. Defaults to now.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        Instant startRange = getStartOrDefault(start);
        Instant endRange = getEndOrDefault(end);

        log.info("Request for order counts from {} to {}", startRange, endRange);
        long count = metricsQueryUseCase.getOrderCount(startRange, endRange);
        return ResponseEntity.ok(new OrderCountResponse(count, startRange, endRange));
    }

    @GetMapping("/refunds")
    @Operation(
        summary = "Query aggregate refund payouts",
        description = "Returns the sum total of all refund events processed within the specified time window. Access restricted to ADMIN or ANALYST."
    )
    @ApiResponse(responseCode = "200", description = "Query executed successfully.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient user privileges.")
    public ResponseEntity<RefundAmountResponse> getRefundAmount(
            @Parameter(description = "Beginning ISO-8601 boundary timestamp. Defaults to 30 days ago.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @Parameter(description = "Closing ISO-8601 boundary timestamp. Defaults to now.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        Instant startRange = getStartOrDefault(start);
        Instant endRange = getEndOrDefault(end);

        log.info("Request for refund amounts from {} to {}", startRange, endRange);
        BigDecimal amount = metricsQueryUseCase.getRefundAmount(startRange, endRange);
        return ResponseEntity.ok(new RefundAmountResponse(amount, startRange, endRange));
    }

    @GetMapping("/top-products")
    @Operation(
        summary = "Query highest-performing products",
        description = "Returns a ranked collection of items by completed total revenue. Access restricted to ADMIN or ANALYST."
    )
    @ApiResponse(responseCode = "200", description = "Query executed successfully.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient user privileges.")
    public ResponseEntity<List<ProductMetric>> getTopProducts(
            @Parameter(description = "Maximum length of items returned. Defaults to 5.")
            @RequestParam(defaultValue = "5") int limit) {

        log.info("Request for top products with limit: {}", limit);
        List<ProductMetric> products = metricsQueryUseCase.getTopProducts(limit);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/customer-activity")
    @Operation(
        summary = "Query client interaction distribution",
        description = "Returns an aggregate trace count matching specific action events grouped by type. Access restricted to ADMIN or ANALYST."
    )
    @ApiResponse(responseCode = "200", description = "Query executed successfully.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient user privileges.")
    public ResponseEntity<List<ActivityMetric>> getCustomerActivities(
            @Parameter(description = "Beginning ISO-8601 boundary timestamp. Defaults to 30 days ago.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @Parameter(description = "Closing ISO-8601 boundary timestamp. Defaults to now.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        Instant startRange = getStartOrDefault(start);
        Instant endRange = getEndOrDefault(end);

        log.info("Request for customer activity counts from {} to {}", startRange, endRange);
        List<ActivityMetric> breakdown = metricsQueryUseCase.getCustomerActivityBreakdown(startRange, endRange);
        return ResponseEntity.ok(breakdown);
    }

    @GetMapping("/active-customers")
    @Operation(
        summary = "Query unique active customer count",
        description = "Returns the count of distinct clients that successfully registered activities or order completions within the window. Access restricted to ADMIN or ANALYST."
    )
    @ApiResponse(responseCode = "200", description = "Query executed successfully.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient user privileges.")
    public ResponseEntity<ActiveCustomersResponse> getActiveCustomers(
            @Parameter(description = "Beginning ISO-8601 boundary timestamp. Defaults to 30 days ago.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @Parameter(description = "Closing ISO-8601 boundary timestamp. Defaults to now.")
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

    @GetMapping("/events")
    @Operation(
        summary = "Query ingested events paginated log",
        description = "Returns a paginated list of all ingested event records in chronological order. Access restricted to ADMIN or ANALYST."
    )
    @ApiResponse(responseCode = "200", description = "Query executed successfully.")
    @ApiResponse(responseCode = "401", description = "Missing or malformed Authorization session token.")
    @ApiResponse(responseCode = "403", description = "Access denied due to insufficient privileges.")
    public ResponseEntity<Page<IngestedEventInfo>> getIngestedEvents(
            @Parameter(description = "Zero-based page index. Defaults to 0.")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Size of page to return. Defaults to 10.")
            @RequestParam(defaultValue = "10") int size) {

        log.info("Request for paginated ingested events. Page={}, Size={}", page, size);
        Page<IngestedEventInfo> result = metricsQueryUseCase.getIngestedEvents(PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }
}
