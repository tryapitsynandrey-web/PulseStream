package com.pulsestream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User credentials payload for authentication.")
public record LoginRequest(
    @Schema(description = "Account username.", example = "admin")
    @NotBlank(message = "Username is required")
    String username,
    
    @Schema(description = "Account password.", example = "password")
    @NotBlank(message = "Password is required")
    String password
) {}
