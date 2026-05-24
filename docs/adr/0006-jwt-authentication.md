# ADR 0006: JWT Authentication and Role-Based Access Control

## Status
Approved

## Context
Endpoints inside PulseStream are highly critical. Financial ingestion routes (`POST /api/v1/events/*`) and business intelligence metrics endpoints (`GET /api/v1/metrics/*`) must not be accessible to anonymous users. We need a stateless, secure, and industry-standard authorization scheme.

## Decision
We select stateless **JSON Web Token (JWT)** authentication combined with **Spring Security** Role-Based Access Control (RBAC).

- **Stateless Session Management**: Configure Spring Security to use `SessionCreationPolicy.STATELESS`, completely avoiding server-side HTTP session storage.
- **Role Assignment**:
  - `ROLE_ADMIN`: Allowed to ingest events and query metrics.
  - `ROLE_ANALYST`: Allowed to query metrics (read-only).
  - `ROLE_USER`: Standard restricted role.
- **Harden JWT validation**: Inject a secure HS256 algorithm key (minimum 256-bit Base64) via `.env` configurations. Catch specific JWT parsing exception subclasses (`ExpiredJwtException`, `MalformedJwtException`, etc.) within `JwtTokenProvider` to provide detailed security audit logs without leaking raw stack traces.
- **Sanitized Failures**: Secure endpoints sanitize security failures, returning a standardized JSON body `{"error": "Unauthorized", "message": "..."}` with `401 Unauthorized` or `403 Forbidden` status.

## Consequences
- **Security Posture**: Strong, enterprise-grade protection on write and read vectors.
- **Stateless Scalability**: High request scalability because nodes do not share session memory; every request carries its own authenticated claims inside the HTTP authorization header.
- **Token Management**: Clients must manage token expiration and securely cache tokens to maintain active sessions.

## Alternatives Considered
- **OAuth2 / OIDC Server (e.g. Keycloak)**: Outstanding for distributed corporate architectures, but introduces excessive infrastructural footprint (running a separate identity container), which is overengineered for a compact portfolio repository.
- **Basic Authentication**: Extremely simple, but requires transmitting credentials on every request, increasing exposure risks.
