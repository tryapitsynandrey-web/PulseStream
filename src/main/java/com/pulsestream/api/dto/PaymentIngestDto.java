package com.pulsestream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Ingestion payload representing a payment confirmation event.")
public record PaymentIngestDto(
    @Schema(description = "Optional transaction UUID. Auto-generated if omitted.", example = "pay_01h9fa38c8")
    String paymentId,

    @Schema(description = "Identifier of the order associated with the payment.", example = "8a31fa76-7bc2-468f-9a4f-51bc4b57422f", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Order ID is required")
    String orderId,

    @Schema(description = "Payment quantity total.", example = "179.98", minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount,

    @Schema(description = "Resulting status of the payment gateway processing step.", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Status is required")
    String status,

    @Schema(description = "External payment gateway transaction reference tag.", example = "tx_ref_398a1f87b2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Transaction reference is required")
    String transactionRef
) {}
