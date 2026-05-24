package com.pulsestream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Ingestion payload representing a refund issued event.")
public record RefundIngestDto(
    @Schema(description = "Optional transaction UUID. Auto-generated if omitted.", example = "ref_9a87cd2b")
    String refundId,

    @Schema(description = "Identifier of the payment associated with the refund.", example = "pay_01h9fa38c8", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Payment ID is required")
    String paymentId,

    @Schema(description = "Total refund value requested.", example = "89.99", minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    BigDecimal amount,

    @Schema(description = "Stated justification for triggering the refund workflow.", example = "Damaged product on arrival", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Reason is required")
    String reason,

    @Schema(description = "Resulting status of the refund request evaluation.", example = "APPROVED", allowableValues = {"APPROVED", "REJECTED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Status is required")
    String status
) {}
