# Contributing to PulseStream

Thank you for exploring PulseStream! This project is designed as an architectural showcase demonstrating production-grade, event-driven backend engineering. 

We welcome issues and suggestions from recruiters, peer reviewers, and engineering architects.

## Architectural Principles

Before submitting any code changes, ensure they adhere strictly to our **Clean Architecture** package boundaries:
1. **Domain Layer**: Must remain completely free of Spring Boot, JPA/Hibernate, or any framework annotations. Only core models and sealed event types are allowed.
2. **Ports & Adapters**: Inbound ports orchestrate use cases, and outbound ports are implemented inside infrastructure adapters. The dependency flow must point inward.
3. **Outbox Pattern**: Never publish domain events directly to Kafka brokers synchronously inside business transactions. Always utilize the Transactional Outbox pattern via the `outbox_events` logging table.

## Local Development Workflow

1. **Fork and Clone**: Fork the repository and check out your feature branch:
   ```bash
   git checkout -b feature/amazing-improvement
   ```
2. **Launch Services**: Spin up Zookeeper, Kafka, PostgreSQL, Prometheus, and Jaeger:
   ```bash
   docker compose up -d
   ```
3. **Verify Compliance**: Run the complete automated build and Testcontainers integration test suite:
   ```bash
   ./scripts/verify.sh
   ```
4. **Submit Pull Request**: Open a PR using our standard template. Ensure all tests are green and compilation produces zero warnings.
