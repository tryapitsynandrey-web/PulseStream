# ADR 0002: Kafka Event-Driven Architecture

## Status
Approved

## Context
PulseStream requires ingestion of massive volumes of transactional events (orders, payments, refunds, and activities) in real time. Processing these events synchronously inside HTTP worker threads leads to low throughput, database locking contention, and immediate failure if external downstream components are offline.

## Decision
We adopt **Apache Kafka** as our central messaging backbone and event streaming engine. 

- **Asynchronous Processing**: Ingestion endpoints accept DTO payloads, validate schemas, package them into immutable `DomainEvent` commands, publish them to Kafka, and immediately return `202 Accepted` to the client.
- **Topic Layout**:
  - `order-created`: Order ingestion topic (3 partitions).
  - `payment-confirmed`: Payment confirmation topic (3 partitions).
  - `refund-issued`: Refund processing topic (3 partitions).
  - `activity-detected`: Interaction trace tracking topic (3 partitions).
- **Dead Letter Queue (DLQ)**: Every main topic is coupled to a corresponding `.DLT` topic (e.g. `order-created.DLT`). We implement a `CommonErrorHandler` with `DeadLetterPublishingRecoverer` and `FixedBackOff` (3 total attempts, 1-second fixed delay) to automatically route poisoned payloads or unrecoverable processing failures to DLQs without losing events or crashing consumers.

## Consequences
- **Elastic Scalability**: Kafka partition mapping allows us to scale consumers horizontally across multiple JVM nodes within the consumer group.
- **Fault Tolerance**: If the database is temporarily offline or degraded, Kafka acts as a buffer. Events remain safely on Kafka brokers and are processed once the database recovers.
- **Reliability & Auditability**: Poisoned pills are routed to DLTs, logging precise alerts for operational review, while healthy traffic flows without interruption.
- **Complexity**: Debugging distributed systems increases in complexity. We mitigate this through correlation ID propagation inside Kafka headers.

## Alternatives Considered
- **Direct Synchronous Persistence**: Rejected because it creates severe thread bottlenecks and limits the system's ingestion capacity to standard JDBC connection pool sizes.
- **RabbitMQ**: Excellent for simple queue routing, but lacks Kafka's log-replayability, high-throughput partitioning, and durable event-sourcing characteristics required for portfolio aggregate analytics.
