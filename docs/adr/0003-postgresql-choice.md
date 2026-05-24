# ADR 0003: PostgreSQL Database Selection

## Status
Approved

## Context
PulseStream needs to store relational customer profiles, order states, payment statuses, and refunds with absolute ACID guarantees to avoid transactional discrepancy (e.g. executing a refund on a non-existent payment). We also need to run fast, index-driven analytics queries on completed transaction sums.

## Decision
We select **PostgreSQL** as our primary relational database.

- **ACID Compliance**: Ensures strong integrity guarantees, referencing foreign keys across orders, payments, and refunds cleanly.
- **Audit Logging**: Store incoming JSON payloads inside a dedicated `ingested_events` table using PostgreSQL's native `JSONB` format for flexible, high-performance schema-agnostic archiving.
- **Flyway Migrations**: All changes to the database structure are explicitly defined in raw SQL Flyway migrations (`db/migration/*`) to support seamless replication across staging, testing, and production.
- **Index Optimization**: Define targeted relational indexes (`idx_orders_created_at`, `idx_activities_customer_created`, etc.) to optimize CQRS-style read queries.

## Consequences
- **Performance**: Capable of processing thousands of database operations per second when optimized with appropriate connection pooling.
- **Query Flexibility**: Supports advanced aggregation and reporting queries required by the Analytics metric endpoints.
- **Resource Constraints**: Relational write scaling is bounded by disk I/O and connection counts, but is highly sufficient for PulseStream requirements.

## Alternatives Considered
- **NoSQL (e.g. MongoDB)**: Offers excellent schema flexibility, but lacks strict ACID foreign key constraints, increasing risk of relational state inconsistencies.
- **MySQL**: Relational, but Postgres offers superior `JSONB` querying support, richer indexing tools (like GIN/GiST), and more advanced window function processing capabilities.
