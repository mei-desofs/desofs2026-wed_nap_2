# ArcadeHaven — Security Logging Inventory

**ASVS V16.1.1** — Formal inventory of all logged security events, formats, storage destinations, access controls, and retention policy.

Generated: 2026-06-13

---

## 1. Logging Architecture

| Layer | Implementation | Logger Name |
|-------|---------------|-------------|
| Security audit events | `Security/SecurityAuditService.java` | `SECURITY_AUDIT` |
| HTTP 401 / 403 events | `Security/SecurityEventHandler.java` | `SECURITY_AUDIT` |
| Application errors | `Exception/GlobalExceptionHandler.java` | `GlobalExceptionHandler` |
| Framework internals | Spring / Hibernate / HikariCP | various |

All log output is routed via **Logback** (`logback-spring.xml`).

---

## 2. Log Destinations

| Destination | Path | Fallback role |
|------------|------|--------------|
| **Console (stdout)** | Docker container stdout | Last-resort — always available (V16.5.2) |
| **Rolling file** | `/app/logs/arcadehaven.log` | Primary persistent store |

The application writes to both destinations simultaneously. If the file appender fails (e.g. disk full), Logback suppresses the error internally and the application continues operating via the console appender — satisfying **V16.5.2**.

---

## 3. Log Format

All security audit lines are **structured JSON** (V16.2.4), emitted by `SecurityAuditService.json()`:

```json
{"event":"SECURITY_EVENT","ts":"2026-06-13T20:00:00.000Z","type":"LOGIN_SUCCESS","ip":"192.168.1.1","user":"alice"}
```

| Field | Always present | Description |
|-------|---------------|-------------|
| `event` | ✅ | Top-level event class: `SECURITY_EVENT` or `SECURITY_ALERT` |
| `ts` | ✅ | UTC ISO-8601 timestamp (`Instant.now()`) (V16.2.2) |
| `type` | ✅ | Specific event type (see §4) |
| `ip` | Most events | Client IP resolved from `X-Forwarded-For` or `RemoteAddr` |
| `path` | Where applicable | Request URI |
| `method` | Where applicable | HTTP method |
| `user` | Auth events | Username (Keycloak `preferred_username`) |
| `action` | Admin events | Admin operation performed |
| `target` | Admin events | Object of the admin action |
| `details` | Validation events | Comma-separated list of failing field names (never values) |

**Sensitive data policy (V16.2.5):** `SecurityAuditService.sanitize()` automatically redacts JWT tokens (`[JWT_REDACTED]`) and 32-char activation keys (`[KEY_REDACTED]`) before any value is embedded in a log line. Field values are never logged — only field names appear in `details`.

---

## 4. Logged Event Types

### Authentication Events (V16.3.1)

| Event type | Logger level | Trigger | Source |
|-----------|-------------|---------|--------|
| `LOGIN_SUCCESS` | INFO | Successful password authentication | `AuthService.login()` |
| `REGISTRATION_SUCCESS` | INFO | Successful user registration | `AuthService.register()` |
| `REGISTRATION_FAILURE` | WARN | Registration rejected (policy/duplicate) | `AuthService.register()` |
| `UNAUTHORIZED` | WARN | 401 — missing or invalid JWT | `SecurityEventHandler` |

### Authorization Events (V16.3.2)

| Event type | Logger level | Trigger | Source |
|-----------|-------------|---------|--------|
| `ACCESS_DENIED` | WARN | 403 — JWT valid but role insufficient | `SecurityEventHandler` |

### Anti-Automation Events (V16.3.3)

| Event type | Logger level | Trigger | Source |
|-----------|-------------|---------|--------|
| `RATE_LIMIT_EXCEEDED` | WARN | Per-IP bucket exhausted (20 req/min) | `RateLimitFilter` |
| `VALIDATION_FAILURE` | WARN | Bean Validation / `ConstraintViolation` | `GlobalExceptionHandler` |

### Admin Audit Events (RNF-13 / V16.2.1)

| Event type | Logger level | Trigger | Source |
|-----------|-------------|---------|--------|
| `ADMIN_ACTION` | INFO | Any admin mutating operation | `AdminService` |

Admin action sub-types (`action` field): `DEACTIVATE_USER`, `ACTIVATE_USER`, `CHANGE_ROLE`, `SUSPEND_LIBRARY_ENTRY`, `REVOKE_LIBRARY_ENTRY`.

### Threshold Alerts (RNF-06)

| Event type | Logger level | Trigger | Source |
|-----------|-------------|---------|--------|
| `THRESHOLD_EXCEEDED` (SECURITY_ALERT) | ERROR | IP exceeds 10 security events / 60 s | `SecurityAuditService.checkAndAlert()` |

---

## 5. Access Controls (V16.4.2)

| Control | Detail |
|---------|--------|
| **File owner** | `appuser:appgroup` (non-root container user) |
| **Directory permissions** | `chmod 750 /app/logs` — group and world have no write access |
| **Container isolation** | Only the `app` container process can access `/app/logs` |
| **Log rotation** | Daily rollover; 30-day retention; 500 MB total cap (`logback-spring.xml`) |
| **No remote shipping** | Logs are not sent to any third-party service; they stay in the container volume |
| **Production hardening** | Mount `/app/logs` as a named Docker volume with restricted host permissions; forward to an append-only SIEM (e.g. Elasticsearch) for tamper-evident storage |

---

## 6. Retention Policy

| Log tier | Retention | Justification |
|----------|-----------|--------------|
| Rolling file on disk | 30 days | Incident investigation window |
| Total size cap | 500 MB | Prevents disk exhaustion |
| Archive / SIEM | Project policy — not yet configured | For forensics beyond 30 days |

---

## 7. What Is NOT Logged

The following data is explicitly excluded from all log output to minimise sensitive data exposure (V16.2.5):

- JWT access tokens and refresh tokens → auto-redacted as `[JWT_REDACTED]`
- Activation keys (32-char hex) → auto-redacted as `[KEY_REDACTED]`
- Request body contents (passwords, payment data) → never logged
- SQL query parameters → `spring.jpa.show-sql=false` (V13.4.2)
- Stack traces in HTTP responses → `GlobalExceptionHandler` returns generic messages only

---

## 8. Monitoring Integration

The `SECURITY_AUDIT` logger emits JSON lines that can be ingested directly by:

- **Elasticsearch / Kibana** — index pattern `arcadehaven-*`
- **Splunk** — `sourcetype=_json` with `host=arcadehaven`
- **Datadog** — `source:java service:arcadehaven`

`SECURITY_ALERT` events (level ERROR) should be routed to an on-call alerting channel.
