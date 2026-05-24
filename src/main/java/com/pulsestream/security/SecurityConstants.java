package com.pulsestream.security;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_ANALYST = "ANALYST";
    public static final String ROLE_USER = "USER";

    public static final String[] PUBLIC_MATCHERS = {
        "/api/v1/auth/token",
        "/actuator/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    public static final String EVENTS_INGESTION_MATCHER = "/api/v1/events/**";
    public static final String METRICS_ANALYTICS_MATCHER = "/api/v1/metrics/**";
}
