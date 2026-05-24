# ADR 0005: Observability Strategy

## Status
Approved

## Context
In a distributed event-driven environment, understanding request flow and performance characteristics is incredibly challenging. If a client receives a validation warning or if an event is routed to a DLT, developers need to easily correlate API logs, database audit logs, and Kafka streaming records.

## Decision
We implement a highly comprehensive **Observability Strategy** combining three dimensions:

1. **Correlation ID Propagation**:
   - Every inbound REST request is intercepted by `CorrelationIdFilter.java`. If `X-Correlation-Id` is missing, we auto-generate a UUID.
   - The ID is stored in Logback's **MDC (Mapped Diagnostic Context)** so every subsequent application log statement outputs `[Corr: <id>]`.
   - The ID is attached to outgoing HTTP responses via headers.
   - The ID is attached as a Kafka header (`X-Correlation-Id`) in `KafkaEventPublisher.java`.
   - Upon consumption, `KafkaEventConsumers.java` extracts this header and populates the local thread MDC, preserving the transaction trace fully.
2. **Prometheus Metrics Binding**:
   - Enable Spring Boot Actuator `/actuator/prometheus` scraping.
   - Centralize custom metrics inside `IngestionMetrics.java` using **Micrometer**.
   - Custom metrics:
     - `pulsestream.events.ingested.total`: Total events successfully processed (Counter).
     - `pulsestream.events.ingestion.duration`: Ingestion latency (Timer).
     - `pulsestream.events.ingestion.failed.total`: Failed ingestion attempts (Counter).
     - `pulsestream.persistence.duration`: Database adapter write latency (Timer).
3. **Grafana Dashboards**:
   - Run a local Prometheus scrapper pointing to Actuator, and map a Grafana instance locally to display these metrics dynamically in real time.

## Consequences
- **Production Observability**: Full traceability of any transaction from API ingestion through Kafka topics to database persistent logs.
- **Minimal Overhead**: MDC propagation is lightweight, utilizing thread-local memory structures cleanly.

## Alternatives Considered
- **Heavy APM Agents (e.g. Dynatrace/Datadog)**: Outstanding, but require paid licenses and external setup, which is not suitable for portable open-source portfolio builds.
- **Plain Text Logs (No Correlation)**: Rejected because it makes debugging concurrent event streams extremely difficult.
