package com.pulsestream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Ingestion payload representing a new customer order event.")
public record OrderIngestDto(
    @Schema(description = "Optional transaction UUID. Auto-generated if omitted.", example = "8a31fa76-7bc2-468f-9a4f-51bc4b57422f")
    String orderId,

    @Schema(description = "Identifier of the client making the purchase.", example = "cust_9921", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Customer ID is required")
    String customerId,

    @Schema(description = "Identifier of the purchased commodity.", example = "prod_headphones_x", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Product ID is required")
    String productId,

    @Schema(description = "Positive integer indicating volume purchased.", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "Quantity must be at least 1")
    int quantity,

    @Schema(description = "Positive unit price of the commodity.", example = "89.99", minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    BigDecimal price
) {}
