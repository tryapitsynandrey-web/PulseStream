package com.pulsestream.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivityIngestDto(
    String activityId,

    @NotBlank(message = "Customer ID is required")
    String customerId,

    @NotBlank(message = "Activity type is required")
    String activityType,

    String metadata
) {}
