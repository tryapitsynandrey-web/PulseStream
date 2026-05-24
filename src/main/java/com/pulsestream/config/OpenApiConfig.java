package com.pulsestream.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "PulseStream API",
        version = "1.0.0",
        description = "Production-grade event-driven business analytics platform built with Spring Boot, Kafka, and PostgreSQL.",
        contact = @Contact(
            name = "PulseStream Support",
            email = "support@pulsestream.com"
        )
    ),
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Authenticate using a JWT token generated from /api/v1/auth/token."
)
public class OpenApiConfig {
}
