package com.pulsestream.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public final class MetricResponses {

    private MetricResponses() {}

    public record RevenueResponse(BigDecimal revenue, Instant start, Instant end) {}
    public record OrderCountResponse(long count, Instant start, Instant end) {}
    public record RefundAmountResponse(BigDecimal amount, Instant start, Instant end) {}
    public record ActiveCustomersResponse(long activeCustomers, Instant start, Instant end) {}
}
