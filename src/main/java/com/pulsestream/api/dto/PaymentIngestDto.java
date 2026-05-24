package com.pulsestream.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentIngestDto(
    String paymentId,

    @NotBlank(message = "Order ID is required")
    String orderId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount,

    @NotBlank(message = "Status is required")
    String status,

    @NotBlank(message = "Transaction reference is required")
    String transactionRef
) {}
