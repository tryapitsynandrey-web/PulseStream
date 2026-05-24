package com.pulsestream.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload carrying the generated bearer JWT access token.")
public record JwtResponse(
    @Schema(description = "Calculated JSON Web Token string valid for 1 hour.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,

    @Schema(description = "Standard prefix classification for the token payload.", example = "Bearer")
    String tokenType
) {
    public JwtResponse(String accessToken) {
        this(accessToken, "Bearer");
    }
}
