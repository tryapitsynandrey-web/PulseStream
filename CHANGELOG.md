# Changelog

All notable changes to the **PulseStream** platform will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-05-24

### Added
- **Transactional Outbox Pattern**: Implemented the transactional outbox pattern to resolve dual-write problems. Added `outbox_events` table, `OutboxEventEntity`, transaction-aware `OutboxEventPublisher`, and background polling `OutboxProcessor` scheduler.
- **Idempotency Engine**: Added client-supplied `X-Event-Id` transaction tracking validation. Duplicate requests are caught and mapped to HTTP `409 Conflict` responses via a custom `DuplicateEventException`.
- **Event Schema Versioning**: Added `schemaVersion` parameter contracts to all record domain events. Implemented backward-compatible compact constructors defaulting missing version parameters to `1` automatically.
- **Analytical Pagination**: Added a paginated event log endpoint `GET /api/v1/metrics/events` allowing historic log query plans to remain fast and scale-proof.
- **Resilience4j Fault Tolerance**: Configured programmatic Circuit Breakers and Retries on PostgreSQL JPA analytics adapters and Apache Kafka message publishers.
- **cncf Distributed Observability**: Integrated Jaeger tracing and an OpenTelemetry Collector trace forwarding pipeline. 
- **Auto-Provisioned Dashboards**: Configured auto-provisioned Grafana dashboards tracking SLA percentiles (p95/p99) and database/outbox latencies on startup.
- **k6 Load Benchmarks**: Created realistic stress-testing scripts for ingestion and analytics metrics endpoints.
- **GitHub Hardening**: Added bug report, feature request, and PR templates, `CODEOWNERS`, `SECURITY.md`, and `CONTRIBUTING.md` guidelines.

### Changed
- Refactored `KafkaEventConsumers` listener methods to extract MDC correlation IDs and rethrow caught exceptions to execute Spring Kafka recovery pipelines instead of swallowing them.
- Upgraded the `README.md` to authoritative, senior-level engineering documentation.

### Optimized
- Hardened database index queries by replacing single indices with high-performance composite indices: `orders(status, created_at)`, `refunds(status, created_at)`, and `outbox_events(status, created_at)`.
- Switched the integration test suite to a static initialization block for Testcontainers, dropping local test run durations to 28 seconds.
