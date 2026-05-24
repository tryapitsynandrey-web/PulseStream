# PulseStream System Architecture Diagrams

This document contains high-fidelity **Mermaid** sequence and component diagrams visualizing the distributed systems architecture of PulseStream.

---

## 1. Container & C4 Component Diagram

```mermaid
graph TD
    Client[REST API Client / JMeter / k6] -->|HTTPS POST| IngestionGW[Event Ingestion Gateway<br/>EventIngestionController]
    Client -->|HTTPS GET| AnalyticsEngine[Analytics & Metrics Engine<br/>AnalyticsController]
    
    subgraph Spring Boot 3 Core Service [PulseStream Application]
        IngestionGW -->|Orchestrates Command| IngestionUseCase[IngestEventUseCase<br/>EventIngestionService]
        IngestionUseCase -->|Transactional Save| IngestedEventRepo[IngestedEventRepository]
        IngestionUseCase -->|Transactional Save| OutboxPublisher[EventPublisher<br/>OutboxEventPublisher]
        
        OutboxScheduler[OutboxProcessor<br/>@Scheduled Background Poll] -->|Fetch PENDING| OutboxRepo[SpringDataOutboxRepository]
        OutboxScheduler -->|Publish Event| KafkaPublisher[KafkaEventPublisher]
        
        AnalyticsEngine -->|Query Metrics| MetricsUseCase[MetricsQueryUseCase<br/>AnalyticsService]
        MetricsUseCase -->|Optimized Fetch| AnalyticsRepo[AnalyticsQueryRepository<br/>AnalyticsQueryPersistenceAdapter]
    end

    subgraph Relational Database [PostgreSQL 16]
        IngestedEventRepo -->|Deduplicate & Log| IngestedEventsTable[(ingested_events)]
        OutboxPublisher -->|Save Outbox Event| OutboxEventsTable[(outbox_events)]
        OutboxRepo -->|Read/Update Status| OutboxEventsTable
        AnalyticsRepo -->|Query Aggregates| OrdersTable[(orders)]
        AnalyticsRepo -->|Query Aggregates| PaymentsTable[(payments)]
        AnalyticsRepo -->|Query Aggregates| RefundsTable[(refunds)]
    end

    subgraph Event Broker [Kafka Broker]
        KafkaPublisher -->|At-Least-Once Send| KafkaTopics[[Kafka Event Topics<br/>order-created / payment-confirmed]]
    end
```

---

## 2. Transactional Outbox Sequence

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client / API Caller
    participant Controller as EventIngestionController
    participant Service as EventIngestionService
    participant DB as PostgreSQL Database
    participant OutboxPublisher as OutboxEventPublisher
    participant OutboxScheduler as OutboxProcessor
    participant Broker as Kafka Broker

    Client->>Controller: POST /api/v1/events/orders (with X-Event-Id)
    Controller->>Service: ingestOrder(OrderCommand)
    
    Note over Service, DB: Database Transaction BEGINS
    
    Service->>DB: Check if Event ID exists (existsById)
    alt Event ID is duplicate
        DB-->>Service: Event ID exists
        Service-->>Controller: Throw DuplicateEventException
        Controller-->>Client: 409 Conflict Response
    else Event ID is unique
        DB-->>Service: Event ID is unique
        Service->>OutboxPublisher: publish(OrderCreatedEvent)
        OutboxPublisher->>DB: Save OutboxEventEntity (PENDING)
        Service->>DB: Log IngestedEventEntity (Audit log)
        Note over Service, DB: Database Transaction COMMITS
        Service-->>Controller: Return IngestionResult
        Controller-->>Client: 202 Accepted Response
    end

    Note over OutboxScheduler, Broker: Asynchronous Processing Loop
    loop Every 100ms
        OutboxScheduler->>DB: Fetch top 50 PENDING records
        DB-->>OutboxScheduler: List of OutboxEventEntity
        loop For each event record
            OutboxScheduler->>Broker: Send record (KafkaEventPublisher)
            Broker-->>OutboxScheduler: Send Acknowledged (ACK)
            OutboxScheduler->>DB: Update status to PROCESSED & processedAt
        end
    end
```

---

## 3. Resilience, Retry, and DLQ Flow

```mermaid
graph TD
    Broker[[Kafka Broker]] -->|Consume Event| Consumer[KafkaEventConsumers]
    
    subgraph Spring Kafka Container [Consumer Retry & Recovery]
        Consumer -->|Process Payload| SaveAudit[Save Ingestion Audit log]
        SaveAudit -->|Transient Failure| RetryLogic{Spring Kafka Container<br/>ErrorHandler Retry?}
        RetryLogic -->|Yes: Max 3 Attempts| Consumer
        RetryLogic -->|No: Exhausted| DLTRecoverer[DeadLetterPublishingRecoverer]
    end

    DLTRecoverer -->|Forward Dead Record| KafkaDLT[[Dead Letter Topics<br/>order-created.DLT / payment-confirmed.DLT]]
    
    subgraph Observability Alarm
        KafkaDLT -->|Trigger Monitor| DLQConsumer[KafkaEventConsumers.consumeOrderCreatedDlt]
        DLQConsumer -->|Alert Trace| LogAlert[CRITICAL DLQ ALERT logged]
    end
```
