package com.pulsestream.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RefundIngestDto(
    String refundId,

    @NotBlank(message = "Payment ID is required")
    String paymentId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount,

    @NotBlank(message = "Reason is required")
    String reason,

    @NotBlank(message = "Status is required")
    String status
) {}
