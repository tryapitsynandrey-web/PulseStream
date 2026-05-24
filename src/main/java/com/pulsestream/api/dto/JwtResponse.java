package com.pulsestream.api.dto;

public record JwtResponse(
    String accessToken,
    String tokenType
) {
    public JwtResponse(String accessToken) {
        this(accessToken, "Bearer");
    }
}
