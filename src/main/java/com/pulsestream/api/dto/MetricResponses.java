package com.pulsestream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

public final class MetricResponses {

    private MetricResponses() {}

    @Schema(description = "Analytical payload representing calculated operational revenue.")
    public record RevenueResponse(
        @Schema(description = "Aggregate sum value representing completed sales values.", example = "15200.50")
        BigDecimal revenue,
        @Schema(description = "Query window beginning boundary timestamp.")
        Instant start,
        @Schema(description = "Query window closing boundary timestamp.")
        Instant end
    ) {}

    @Schema(description = "Analytical payload representing order volume count.")
    public record OrderCountResponse(
        @Schema(description = "Aggregate absolute count representing total processed orders.", example = "120")
        long count,
        @Schema(description = "Query window beginning boundary timestamp.")
        Instant start,
        @Schema(description = "Query window closing boundary timestamp.")
        Instant end
    ) {}

    @Schema(description = "Analytical payload representing total refund payout aggregate values.")
    public record RefundAmountResponse(
        @Schema(description = "Aggregate sum value representing approved refunded payments.", example = "450.00")
        BigDecimal amount,
        @Schema(description = "Query window beginning boundary timestamp.")
        Instant start,
        @Schema(description = "Query window closing boundary timestamp.")
        Instant end
    ) {}

    @Schema(description = "Analytical payload representing count of unique active buyers.")
    public record ActiveCustomersResponse(
        @Schema(description = "Distinct user identity count within the range.", example = "84")
        long activeCustomers,
        @Schema(description = "Query window beginning boundary timestamp.")
        Instant start,
        @Schema(description = "Query window closing boundary timestamp.")
        Instant end
    ) {}
}
