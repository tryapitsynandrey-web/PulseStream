# ADR 0004: Adopting Java 21 LTS

## Status
Approved

## Context
Java development has accelerated, bringing powerful language syntax and JVM performance improvements in recent LTS versions. In designing a senior-grade portfolio, we need modern, robust, and clean Java features that reduce boilerplate and enhance concurrency.

## Decision
We select **Java 21 LTS** as our baseline compiler runtime.

- **Record Types**: Extensive use of immutable record structures for API DTOs and application commands, removing hundreds of lines of Lombok or manual getter/setter boilerplate.
- **Pattern Matching for Switch**: Extensively used in `KafkaEventPublisher` and domain mapping interfaces to perform type-safe dispatching of abstract events:
  ```java
  return switch (event) {
      case OrderCreatedEvent o -> "order-created";
      ...
  };
  ```
- **Performance**: Garbage Collection and runtime JVM tuning out of the box.

## Consequences
- **Modern Codebase**: Highly readable, modern codebase that reflects the state of the art in Java programming.
- **Environment Boundaries**: Requires local setups and CI runners to explicitly use Java 21 (managed via `.github/workflows` and verified `pom.xml` configs). Newer developer preview versions are discouraged locally to avoid bytecode/Mockito version mismatches.

## Alternatives Considered
- **Java 17 LTS**: A solid LTS version, but lacks advanced record pattern matching, string templates, and sequential collection features present in 21.
- **Java 8 or 11**: Completely obsolete for modern Spring Boot 3 enterprise backends.
