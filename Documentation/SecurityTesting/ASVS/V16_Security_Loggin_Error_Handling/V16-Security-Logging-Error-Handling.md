# V16 — Security Logging and Error Handling — ASVS 5.0 Assessment

## 1. Overview

This document assesses all V16 ASVS 5.0 requirements against ArcadeHaven — a Java/Spring Boot REST API for digital game sales. ArcadeHaven has three user roles (administrator, publisher, buyer), uses JWT Bearer authentication, BCrypt password hashing, Docker containerisation, and integrates with the RAWG API for game metadata enrichment.

V16 covers five areas:
- **V16.1** — Security Logging Documentation: what must be inventoried about the logging infrastructure
- **V16.2** — General Logging: metadata completeness, timestamp integrity, log destinations, format, and sensitive data handling
- **V16.3** — Security Events: authentication, authorization, security event, and error logging
- **V16.4** — Log Protection: injection prevention, access control, and transmission integrity
- **V16.5** — Error Handling: generic error responses, external resource failures, graceful degradation, and last-resort handlers

**Primary mapping threads:**
- RNF-07 (Logging Requirements) → LM-001/002/003 → ST-008
- RNF-23 (Secrets Management) → SC-002 → ST-016
- RNF-06 (Input Validation) → IV-001/003 → AC-21 / ST-017

V16.5.4 is a Level 3 requirement and is outside the mandatory ASVS Level 2 scope for ArcadeHaven.

---

## 2. V16.1 — Security Logging Documentation

### V16.1.1 — Logging Inventory (Level 2)

**Requirement:** Verify that an inventory exists documenting the logging performed at each layer of the application's technology stack, what events are being logged, log formats, where that logging is stored, how it is used, how access to it is controlled, and for how long logs are kept.

**ArcadeHaven assessment:** The logging inventory is defined across RNF-07 and the LM-001/002/003 control catalog:

| Inventory dimension | ArcadeHaven definition |
|--------------------|------------------------|
| Events logged | Authentication attempts (success/failure), authorization failures, role changes by administrators, invoice download access, activation key access, input validation failures, security control failures |
| Log format | Structured JSON with named fields: timestamp, level, userId, role, action, resource, outcome, sourceIp |
| Log destination | Application stdout (Docker container); Docker logging driver collects to host |
| Access control | Docker host access restricted to authorised operators; no application-level log viewer exposed |
| Retention | Retention policy to be defined as part of Docker deployment configuration |

**Status:** In Progress

---

## 3. V16.2 — General Logging

### V16.2.1 — Log Entry Metadata (Level 2)

**Requirement:** Verify that each log entry includes necessary metadata (such as when, where, who, what) that would allow for a detailed investigation of the timeline when an event happens.

**ArcadeHaven assessment:** Every ArcadeHaven log entry is defined to include:
- **When:** UTC timestamp (`Instant.now()`)
- **Where:** application component / endpoint identifier
- **Who:** userId and role at time of the event
- **What:** action type, target resource (gameId, orderId, invoicePath), outcome (success/failure), source IP address

This schema is defined by LM-001 (Structured Logging) and applies uniformly across all security-relevant events including login, role changes, game approvals, invoice downloads, and file operations.

**Status:** In Progress

---

### V16.2.2 — Timestamp Synchronisation (Level 2)

**Requirement:** Verify that time sources for all logging components are synchronized, and that timestamps in security event metadata use UTC or include an explicit time zone offset. UTC is recommended to ensure consistency across distributed systems and to prevent confusion during daylight saving time transitions.

**ArcadeHaven assessment:** LM-001 mandates UTC timestamps for all log entries. The application generates UTC timestamps using the JVM's `Instant.now()`, which reads from the system clock. The Docker host's NTP configuration synchronises the system clock across all containers (application container and database container), ensuring log timestamps from different components are correlated consistently. NTP configuration is being defined as part of the Docker deployment specification (RNF-21/22).

**Status:** In Progress

---

### V16.2.3 — Authorised Log Destinations (Level 2)

**Requirement:** Verify that the application only stores or broadcasts logs to the files and services that are documented in the log inventory.

**ArcadeHaven assessment:** ArcadeHaven logs exclusively to stdout within the Docker container. No third-party logging services, external log collectors, or additional file destinations are integrated at the application level. All log destinations are identified in the logging inventory (V16.1.1). Any future integration with a SIEM or log forwarder will be added to the inventory before use.

**Status:** In Progress

---

### V16.2.4 — Common Logging Format (Level 2)

**Requirement:** Verify that logs can be read and correlated by the log processor that is in use, preferably by using a common logging format.

**ArcadeHaven assessment:** ArcadeHaven adopts structured JSON logging as the standard format across all application components. Each log record follows a consistent schema with named fields (`timestamp`, `level`, `userId`, `action`, `resource`, `outcome`, `sourceIp`), enabling direct ingestion by standard log processors such as ELK, Splunk, or CloudWatch without pre-processing or custom parsing rules. The format is defined in LM-001.

**Status:** In Progress

---

### V16.2.5 — Sensitive Data Logging Policy (Level 2)

**Requirement:** Verify that when logging sensitive data, the application enforces logging based on the data's protection level. For example, it may not be allowed to log certain data, such as credentials or payment details. Other data, such as session tokens, may only be logged by being hashed or masked, either in full or partially.

**ArcadeHaven assessment:** SC-002 explicitly prohibits logging of JWT tokens, passwords, API keys, and credit card numbers in any log entry. The structured JSON log schema (LM-001) uses named fields, and IV-003 applies JSON encoding before any user-controlled value is written to a log record. A data classification table mapping each ArcadeHaven data category to an explicit logging rule is being formalized:

| Data category | Logging rule |
|---------------|-------------|
| Passwords / BCrypt hashes | Never logged |
| JWT tokens | Never logged |
| Activation keys | Never logged in full; log only that a key was accessed |
| Email addresses | Logged only in auth events (userId field) |
| Order IDs | Logged as resource identifier |
| Payment metadata | Never logged |

**Status:** In Progress

---

## 4. V16.3 — Security Events

### V16.3.1 — Authentication Event Logging (Level 2)

**Requirement:** Verify that all authentication operations are logged, including successful and unsuccessful attempts. Additional metadata, such as the type of authentication or factors used, should also be collected.

**ArcadeHaven assessment:** ArcadeHaven uses JWT Bearer authentication (RNF-02, RF-02). LM-002 requires that every login attempt — whether successful or failed — is logged with:
- User identifier (email / userId)
- Authentication method (JWT)
- Timestamp (UTC)
- Source IP address
- Outcome (success / failure)

Failed attempts are critical for detecting brute-force attacks (abuse case AC-01) and username enumeration (abuse case AC-09). The log entry for a failed attempt does not reveal whether the username or the password was incorrect, to prevent enumeration.

**Status:** In Progress

---

### V16.3.2 — Authorization Failure Logging (Level 2)

**Requirement:** Verify that failed authorization attempts are logged. For L3, this must include logging all authorization decisions, including logging when sensitive data is accessed (without logging the sensitive data itself).

**ArcadeHaven assessment:** LM-002 requires logging of all authorization failures with user identity, role at time of request, target resource, and outcome. ArcadeHaven enforces three role levels (administrator, publisher, buyer) via RNF-04 and AC-004. Authorization failures include:
- A buyer attempting to access publisher-only game management routes (abuse case AC-11)
- Any user attempting to access administrator approval endpoints (abuse case AC-12)
- A buyer attempting to cancel another buyer's order (abuse case AC-16)

Each generates a structured log entry at SECURITY level.

**Status:** In Progress

---

### V16.3.3 — Security Event Catalog Logging (Level 2)

**Requirement:** Verify that the application logs the security events that are defined in the documentation and also logs attempts to bypass the security controls, such as input validation, business logic, and anti-automation.

**ArcadeHaven assessment:** LM-003 defines the catalog of security events to be logged in ArcadeHaven:
- Input validation failures: SQL injection attempts, XSS payloads, oversized file uploads
- Activation key misuse: attempts to use an already-claimed key
- Path traversal attempts on invoice download endpoints
- MIME type mismatch on file upload (FO-002)
- CRLF injection in user-controlled fields (abuse case AC-21)
- Rate limit violations (AC-006)

Any attempt to bypass a security control — whether it succeeds or fails — must generate a log entry at SECURITY level.

**Status:** In Progress

---

### V16.3.4 — Unexpected Errors and TLS Failures (Level 2)

**Requirement:** Verify that the application logs unexpected errors and security control failures such as backend TLS failures.

**ArcadeHaven assessment:** ArcadeHaven integrates with the RAWG API over HTTPS (RF-12). Any TLS failure, connection timeout, or unexpected HTTP error code from the external call is treated as a security-relevant event and logged at ERROR level with the target URL, failure type, and timestamp.

Unhandled application exceptions are caught by a global exception handler (ER-02, see Section 6) which logs the full exception detail — including stack trace and correlation ID — at ERROR level before returning a generic response to the client.

**Status:** In Progress

---

## 5. V16.4 — Log Protection

### V16.4.1 — Log Injection Prevention (Level 2)

**Requirement:** Verify that all logging components appropriately encode data to prevent log injection.

**ArcadeHaven assessment:** User-supplied data included in log records is sanitised before writing. IV-001 (whitelist validation) ensures input fields contain only expected characters, and IV-003 (output encoding) applies JSON encoding to any user-controlled value before it is embedded in a structured log entry.

CRLF injection — where an attacker submits newline characters (`\r\n`) in a login username, search query, or game description field to forge or split log entries — is specifically addressed by abuse case AC-21. The JSON log format intrinsically escapes newline characters in string values, preventing log entry splitting in JSON-aware log processors.

**Status:** In Progress

---

### V16.4.2 — Log Access Protection (Level 2)

**Requirement:** Verify that logs are protected from unauthorized access and cannot be modified.

**ArcadeHaven assessment:** ArcadeHaven logs are emitted to stdout within the Docker container (RNF-21/22). Container-level isolation restricts direct filesystem access to the log stream to authorised operators with Docker host access. The following controls are being defined as part of the Docker deployment configuration:
- Read access to log output restricted to log analysis roles
- Write access restricted to the application process (no external log modification)
- Append-only mode for the container log driver output

Log integrity at rest depends on the infrastructure layer; no application-level log signing is implemented.

**Status:** In Progress

---

### V16.4.3 — Log Transmission to Separate System (Level 2)

**Requirement:** Verify that logs are securely transmitted to a logically separate system for analysis, detection, alerting, and escalation. The aim is to ensure that if the application is breached, the logs are not compromised.

**ArcadeHaven assessment:** Not Applicable at the application layer. ArcadeHaven is a REST API containerised with Docker (RNF-21, RNF-22). The application emits structured JSON logs to stdout; collection, forwarding, and storage in a logically separate system is an infrastructure responsibility outside the application code. Log forwarding to a SIEM or log management service will be configured at deployment time using the Docker logging driver and does not require changes to application code.

**Status:** Not Applicable

---

## 6. V16.5 — Error Handling

### V16.5.1 — Generic Error Messages (Level 2)

**Requirement:** Verify that a generic message is returned to the consumer when an unexpected or security-sensitive error occurs, ensuring no exposure of sensitive internal system data such as stack traces, queries, secret keys, and tokens.

**ArcadeHaven assessment (ER-01):** ArcadeHaven exposes a REST API with four trust levels (unauthenticated, buyer, publisher, administrator). Any unhandled exception or security-sensitive error must return a generic response body, for example:

```json
{"error": "An unexpected error occurred", "ref": "<correlation-id>"}
```

The following must never appear in HTTP responses:
- Java stack traces or exception class names
- SQL error messages or ORM constraint violation details
- Internal file paths or directory structures
- Framework version information
- JWT tokens, secret keys, or API credentials

The global exception handler (ER-02) enforces this by catching all unhandled exceptions, logging full detail internally, and returning only the generic response. This is verified by ST-017, which deliberately triggers error conditions and inspects the HTTP response body.

**Status:** In Progress

---

### V16.5.2 — Secure Operation Under External Resource Failure (Level 2)

**Requirement:** Verify that the application continues to operate securely when external resource access fails, for example, by using patterns such as circuit breakers or graceful degradation.

**ArcadeHaven assessment:** ArcadeHaven integrates with the RAWG API for game metadata enrichment (RF-12). RNF-16 defines fallback behaviour to locally stored metadata when the external API is unavailable. The circuit breaker / fallback pattern being designed for ArcadeHaven:

1. HTTP client calls RAWG API with a configured timeout
2. On timeout, connection failure, or non-2xx response: log the failure at ERROR level
3. Return locally stored game metadata rather than propagating the raw external error to the client
4. Never skip authorization or input validation checks during a fallback — the fallback must produce a complete, security-validated response

RAWG API failures must produce a structured fallback response (abuse case AC-19), not an unhandled exception.

**Status:** In Progress

---

### V16.5.3 — Graceful and Secure Failure (Level 2)

**Requirement:** Verify that the application fails gracefully and securely, including when an exception occurs, preventing fail-open conditions such as processing a transaction despite errors resulting from validation logic.

**ArcadeHaven assessment (ER-02, ER-03):** ArcadeHaven's authorization model (RNF-04, AC-004) is designed to deny by default:
- Any exception thrown during an authorization check must result in access being **denied**, not granted
- A fail-open condition — where an authorization check throws an exception and the request is allowed through — is explicitly prohibited by the security architecture
- The global exception handler (ER-02) catches all unhandled exceptions and returns a generic error response, ensuring incomplete request processing never results in partial authorization

Validation errors returned to the client (ER-03) describe the field and constraint violated without revealing ORM internals. For example, "password must be at least 12 characters" is acceptable; a raw Hibernate `ConstraintViolationException` message is not.

**Status:** In Progress

---

### V16.5.4 — Last Resort Error Handler (Level 3)

**Requirement:** Verify that a "last resort" error handler is defined which will catch all unhandled exceptions. This is both to avoid losing error details that must go to log files and to ensure that an error does not take down the entire application process, leading to a loss of availability.

**ArcadeHaven assessment:** Although a Level 3 requirement, ArcadeHaven's ER-02 control already defines the global exception handler using Spring's `@ControllerAdvice` with an `@ExceptionHandler(Exception.class)` catch-all. This handler intercepts all unhandled exceptions — including runtime exceptions, checked exceptions that escape a service layer, and framework-level errors — logs the full detail (stack trace, correlation ID) at ERROR level internally, and returns the generic response defined in ER-01. In Spring Boot, the `@ControllerAdvice` catch-all functions as the last resort handler for the request processing lifecycle.

A formally verified implementation confirming that no exception class hierarchy can escape the handler (e.g., `Error` subclasses such as `OutOfMemoryError`, or Servlet container exceptions outside the Spring dispatcher) has not yet been completed. This confirmation will be part of the ER-02 implementation review.

**Status:** In Progress

---

## 7. Traceability Threads

### Thread A — Authentication and Authorization Logging

RNF-07 → LM-001 (structured logging schema) → LM-002 (auth/authz event logging) → ST-008 (log verification test)

Covers: V16.1.1, V16.2.1, V16.2.3, V16.2.4, V16.3.1, V16.3.2

### Thread B — Sensitive Data Protection in Logs

RNF-23 → SC-002 (no secrets in logs) → data classification table → ST-016 (configuration and log review)

Covers: V16.2.5, V16.3.4

### Thread C — Log Injection Prevention

RNF-06 → IV-001 (whitelist validation) + IV-003 (JSON encoding in logs) → AC-21 (CRLF injection abuse case) → ST-017

Covers: V16.4.1, V16.3.3

### Thread D — Error Response Security

ER-01 (no internal detail in HTTP responses) → ER-02 (global exception handler) → ER-03 (generic validation errors) → ST-017 (error response validation test)

Covers: V16.5.1, V16.5.3

### Thread E — External Integration Resilience

RNF-16 (RAWG fallback) → circuit breaker design → AC-19 (RAWG unavailability abuse case) → ST-017

Covers: V16.5.2

---

## 8. Identified Gaps

| Gap | Requirement | Impact | Action required |
|-----|-------------|--------|----------------|
| NTP synchronisation not yet configured in Docker | V16.2.2 | Log timestamps from different containers may diverge; cross-component correlation unreliable | Define NTP configuration in Docker deployment spec (RNF-21/22) |
| Data classification logging policy incomplete | V16.2.5 | Risk of inadvertently logging sensitive data beyond what SC-002 prohibits | Formalise data classification table mapping each ArcadeHaven data category to logging rule |
| Log file access controls not yet defined | V16.4.2 | Logs potentially readable or modifiable by any Docker host user | Define append-only mode and read-restricted access in Docker deployment configuration |
| Circuit breaker / fallback pattern not yet formally designed | V16.5.2 | RAWG API failure may result in unhandled exception or raw error response to client | Design and document the fallback architecture for external API integration (RNF-16) |
