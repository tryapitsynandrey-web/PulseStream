# ADR 0001: Adopting Clean Architecture

## Status
Approved

## Context
We need to design a highly maintainable, testable, and robust backend system that decouples core business rules from external technologies (e.g. databases, web frameworks, message brokers, and security modules). 

Without architectural boundaries, systems quickly degrade into a "ball of mud" where database schemas leak into API representations, and business logic is coupled directly with framework-specific annotations.

## Decision
We adopt **Clean Architecture** (closely related to **Hexagonal Architecture / Ports & Adapters**). The codebase is divided into clear logical rings:

1. **Domain (Core Inner Ring)**: Houses pure business entities (`Order`, `Payment`, etc.), domain logic, and domain repository interfaces (Ports). Absolutely zero framework dependencies or external technology references are allowed here.
2. **Application (Use Cases)**: Orchestrates the use cases of the system. Outward ports (`IngestEventUseCase`, `MetricsQueryUseCase`) represent entryways, while inward ports represent gateways (repositories, publishers) that the application core depends on.
3. **Infrastructure (Outer Ring)**: Adapters implementing the inward ports. Contains concrete technology details including Spring Boot Controllers, JPA persistence mappings, Kafka consumer/publisher configurations, and Spring Security filters.

## Consequences
- **Decoupling**: Business rules are agnostic of database choice, Kafka broker properties, or JWT security rules.
- **Testability**: Core logic can be unit-tested without mocking heavy framework elements (like Spring Boot context or database engines).
- **Technology Independent**: Upgrading Spring Boot, replacing Flyway, or migrating Kafka to RabbitMQ will not require changing domain models or business use cases.
- **Cognitive Overhead**: Developers must navigate several layers (DTOs -> Commands -> Events -> Entities) and maintain mappings between adapters and domains. This is a deliberate, justifiable trade-off for maintainability.

## Alternatives Considered
- **Traditional Layered Architecture (Controller -> Service -> DAO)**: Rejected because it naturally couples the business service directly with database transactions and JPA entities, making database migration or mocking extremely difficult.
