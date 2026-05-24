package com.pulsestream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Ingestion payload representing a tracked client interface activity event.")
public record ActivityIngestDto(
    @Schema(description = "Optional activity event UUID. Auto-generated if omitted.", example = "act_7a12bc9f")
    String activityId,

    @Schema(description = "Identifier of the client generating the behavior trace.", example = "cust_9921", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Customer ID is required")
    String customerId,

    @Schema(description = "The catalog classification for the captured interaction.", example = "CLICK_BANNER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Activity type is required")
    String activityType,

    @Schema(description = "Associated payload context parameters as JSON or string serialized properties.", example = "{\"bannerId\": \"promo_summer_2024\"}")
    String metadata
) {}
