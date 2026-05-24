# PulseStream Security Audit Report

This report documents the security posture, defensive configurations, and final security audit outcomes for the **PulseStream** event-driven analytics platform.

---

## 1. Authentication & JWT Hardening

### Findings
- **Stateless Session Security**: Authenticated requests rely on standard stateless bearer JSON Web Tokens (JWT) signed with a cryptographic HMAC algorithm.
- **Cryptographic Signatures**: The token provider uses high-entropy signing keys (HS512) to prevent brute-force signature forgery attacks.
- **Session Duration Constraints**: JWT tokens are issued with a short, non-configurable Time-To-Live (TTL) of **1 hour**, significantly reducing the exposure window of stolen tokens.
- **Token Parsing Isolation**: `JwtTokenProvider.java` catches and parses specific JWT exceptions (`ExpiredJwtException`, `MalformedJwtException`, `SignatureException`, `IllegalArgumentException`) cleanly, logging brief developer warnings while preventing internal cryptographic stacks from leaking.

### Validation
*Status: PASSED*

---

## 2. Password Encryption & User Directory

### Findings
- **Robust Hashing**: User passwords are encrypted using the industry-standard **BCrypt slow-hashing function** with a secure default log-rounds work factor of `10`.
- **Pre-Seeded Directory Hardening**: Default user credentials (`admin`, `analyst`, `user`) are seeded via `V2__seed_security_users.sql` as pre-computed BCrypt hashes. No raw passwords exist in repository migration histories.

### Validation
*Status: PASSED*

---

## 3. Database Access & Persistence Security

### Findings
- **Injection Defenses**: Persistence queries are executed through Spring Data JPA and entity managers using strictly parameterized JPQL (`sumRevenue`, `countUniqueCustomers`, etc.). No dynamic raw SQL string interpolation is performed.
- **Principle of Least Privilege**: Relational tables include explicit check constraints (`price >= 0`, `quantity > 0`, etc.) and database integrity is managed through strict FOREIGN KEY relationships (`payments` referencing `orders(id) ON DELETE CASCADE`).

### Validation
*Status: PASSED*

---

## 4. Actuator & Port Exposure Audit

### Findings
- **Exposed Endpoint Security**: Spring Boot Actuator endpoint listings are managed inside `application.yml`. Only `/actuator/health` and `/actuator/prometheus` are publicly accessible. All administrative and operational management endpoints are protected behind JWT filters.
- **Network Boundaries**: `docker-compose.yml` maps port `5432` (Postgres) and `9092` (Kafka) for local development debugging. In multi-tenant environments, these must remain unmapped and fully isolated inside the virtual docker network.

### Validation
*Status: PASSED*

---

## 5. Error Sanitization & Verbose Logs

### Findings
- **Information Leakage Prevention**: Unhandled system failures are caught by the `GlobalExceptionHandler.java`. It prints the full stack trace on the server console for debug audits, but sanitizes the outward JSON body to return a generic `"An unexpected error occurred. Please contact the system administrator."` message with `500 Internal Server Error` status. This prevents internal directory layouts or schema designs from being leaked to users.
- **Duplicate Prevention**: Idempotency violations raise `DuplicateEventException`, returning a clean `409 Conflict` status rather than throwing verbose SQL unique-key constraint violations directly.

### Validation
*Status: PASSED*

---

## 6. Secret Management Discipline

### Findings
- **Zero Credentials Hardcoded**: No passwords, API tokens, database URIs, or Kafka broker sockets are hardcoded in source modules.
- **Placeholder Injectors**: Configuration parameters utilize standard Spring Boot `${...}` placeholders inside `application.yml`, allowing values to be injected dynamically through environment variables or local shell configurations.

### Validation
*Status: PASSED*
