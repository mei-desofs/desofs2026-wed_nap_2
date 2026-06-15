# ArcadeHaven — Data Classification and Retention Policy

Generated: 2026-06-15  
ASVS References: **V14.1.1** (data identification and classification) · **V14.2.4** (controls per classification level)

This document identifies every type of data processed or stored by ArcadeHaven, assigns each to a protection level, specifies the controls required at each level, and defines the data retention schedule.

---

## 1. Classification Levels

| Level | Label | Definition |
|-------|-------|-----------|
| **L1** | **Public** | Data intentionally exposed to unauthenticated users. No harm if disclosed. |
| **L2** | **Internal** | Operational data not meant for public consumption but whose disclosure causes minimal harm (e.g., log metadata without PII). |
| **L3** | **Confidential** | Data whose unauthorised disclosure would harm users or the business (PII, tokens, order history). |
| **L4** | **Restricted** | Credentials and cryptographic material whose disclosure directly enables system compromise. |

---

## 2. Data Inventory

### 2.1 User Data

| Data Type | Classification | Storage | Notes |
|-----------|---------------|---------|-------|
| Username | L3 Confidential | Keycloak + PostgreSQL | PII under GDPR |
| Email address | L3 Confidential | Keycloak + PostgreSQL | PII under GDPR; used for notifications and login |
| Password hash | L4 Restricted | Keycloak (BCrypt cost ≥ 12) | Never stored in application DB; never logged |
| JWT access token | L3 Confidential | Client memory only; not persisted server-side | 5-minute lifetime; revoked on logout |
| JWT refresh token | L3 Confidential | Keycloak session store | 30-day max lifetime; single-use (`revokeRefreshToken=true`) |
| TOTP seed | L4 Restricted | Keycloak (AES-encrypted credential store) | Not accessible to Spring Boot; managed by Keycloak |

### 2.2 Transactional Data

| Data Type | Classification | Storage | Notes |
|-----------|---------------|---------|-------|
| Order records (buyer, items, price, status) | L3 Confidential | PostgreSQL | Financial records; scoped to authenticated buyer |
| Game activation keys (pre-use) | L3 Confidential | PostgreSQL (`OrderItem.activationKey`) | 128-bit CSPRNG hex; single-use; redacted in logs (`[KEY_REDACTED]`) |
| Game activation keys (post-redemption) | L3 Confidential | PostgreSQL (marked `redeemed=true`) | Retained for audit; no longer usable |
| Library entries (owned games per user) | L3 Confidential | PostgreSQL | Scoped to authenticated user via IDOR check |
| Invoice / key-card text files | L3 Confidential | SFTP (SSH encrypted transport) | Contain activation key; sanitized for format-injection before storage |

### 2.3 Game Catalogue Data

| Data Type | Classification | Storage | Notes |
|-----------|---------------|---------|-------|
| Published game metadata (title, description, price, category) | **L1 Public** | PostgreSQL | Accessible via `GET /api/games` without authentication |
| Game cover images | **L1 Public** | SFTP | Served via download endpoint; no auth required for public games |
| Pending / rejected game submissions | L3 Confidential | PostgreSQL | Visible to publisher owner and admins only |
| Publisher-specific game data | L3 Confidential | PostgreSQL | Scoped via `findByIdAndPublisher()` ownership check |

### 2.4 Operational / System Data

| Data Type | Classification | Storage | Notes |
|-----------|---------------|---------|-------|
| Security audit log entries (event type, IP, path, timestamp) | **L2 Internal** | `logs/arcadehaven.log` (rolling, 30-day retention) | No PII values in log lines; usernames logged as opaque identifiers |
| Spring application logs (INFO/WARN level) | **L2 Internal** | Same log file | SQL queries and parameter values are suppressed (`show-sql=false`) |
| API schema (`/v3/api-docs`) | **L2 Internal** | Runtime (Springdoc) | Requires authenticated JWT to access |

### 2.5 Credentials and Secrets (L4 Restricted)

| Data Type | Storage | Injection Method |
|-----------|---------|----------------|
| `SPRING_DATASOURCE_PASSWORD` | GitHub Secret / `.env` | Env var at container startup |
| `KEYCLOAK_BACKEND_CLIENT_SECRET` | GitHub Secret / `.env` | Env var at container startup |
| `KEYCLOAK_ADMIN_PASSWORD` | GitHub Secret / `.env` | Env var at container startup |
| `SFTP_PASSWORD` | GitHub Secret / `.env` | Env var at container startup |
| `POSTGRES_PASSWORD` | GitHub Secret / `.env` | Env var at container startup |
| Keycloak JWT signing keys (RS256) | Keycloak keystore | Managed by Keycloak; rotatable via Admin Console |

---

## 3. Controls Per Classification Level

### L1 — Public

| Control | Requirement |
|---------|------------|
| Access control | None — endpoints are intentionally unauthenticated |
| Logging | Request metadata logged (IP, path, method); no response body |
| Transport | HTTPS preferred; HTTP redirects to HTTPS |
| Storage | Standard PostgreSQL / SFTP access |

### L2 — Internal

| Control | Requirement |
|---------|------------|
| Access control | Log files: `chmod 750`, owned by `appuser:appgroup` (see Dockerfile) |
| Logging | Log entries must not contain PII values or credential values |
| Transport | Internal Docker network (log file writes are local) |
| Retention | 30 days rolling (`logback-spring.xml` — `maxHistory=30`, `totalSizeCap=500MB`) |

### L3 — Confidential

| Control | Requirement |
|---------|------------|
| Access control | Valid JWT required (`SecurityConfig` — deny-all default); ownership checks in service layer |
| Logging | Values must never appear in log output; only field names and opaque identifiers allowed. `SecurityAuditService.sanitize()` redacts JWT tokens, activation keys, form-encoded credentials, and any field whose JSON key matches `SENSITIVE_KEY_PATTERN` |
| Transport | TLS 1.2/1.3 (nginx) for external; SSH for SFTP; internal Docker bridge (single-host — see `CommunicationMatrix.md`) |
| Storage | PostgreSQL with access restricted to application service account |
| Retention | See §4 |

### L4 — Restricted

| Control | Requirement |
|---------|------------|
| Access control | Env-var injection only; never committed to source control; stored in GitHub Secrets / `.env` (`.env` is `.gitignore`d and in `.dockerignore`) |
| Logging | Absolutely prohibited in any log output. `SENSITIVE_KEY_PATTERN` in `SecurityAuditService.json()` enforces automatic redaction if a secret key name is accidentally passed |
| Transport | HTTPS / SSH only; never transmitted in plaintext |
| Storage | Never in application DB; Keycloak credential store uses AES encryption for TOTP seeds |
| Rotation | See [`Documentation/KeyManagementPolicy.md`](KeyManagementPolicy.md) for rotation schedules |

---

## 4. Data Retention Schedule

| Data Type | Level | Retention Period | Mechanism | Notes |
|-----------|-------|-----------------|-----------|-------|
| Security audit logs | L2 | 30 days | `logback-spring.xml` — `maxHistory=30`, `totalSizeCap=500MB` | Automatic daily rollover; oldest logs deleted |
| Application logs | L2 | 30 days | Same as above | |
| JWT access tokens | L3 | 5 minutes | Keycloak `accessTokenLifespan=300s` | Stateless; expire naturally |
| JWT refresh tokens | L3 | 30 days max | Keycloak `ssoSessionMaxLifespan=604800s`; single-use | Revoked immediately on `POST /api/auth/logout` |
| User account data (PII) | L3 | Until account deletion | No automated expiry | Account deletion removes user from Keycloak and DB |
| Order records | L3 | Indefinite | No automated expiry | Financial records required for audit trail |
| Activation keys | L3 | Lifetime of the order | Soft-delete via `redeemed=true` flag | Not physically deleted; redemption history retained |
| Game files / covers | L3 | Until publisher deletes the game | Manual deletion via publisher API | SFTP files deleted by `SftpStorageService.delete()` |
| Password reset tokens | L4 | 5 minutes | Keycloak `action-token-generated-by-admin-lifespan=300s` | Single-use; invalidated on use |

---

## 5. Cross-References

| Document | Relation |
|---------|---------|
| [`Documentation/CommunicationMatrix.md`](CommunicationMatrix.md) | Describes TLS posture for each data flow |
| [`Documentation/KeyManagementPolicy.md`](KeyManagementPolicy.md) | Rotation schedules for L4 cryptographic assets |
| [`Documentation/CryptographicInventory.md`](CryptographicInventory.md) | Algorithm details for encryption/hashing of L3/L4 data |
| [`Documentation/LoggingInventory.md`](LoggingInventory.md) | Log file access controls, format, and retention detail |
| [`Api/src/main/java/isep/desosfs/arcadehaven/Security/SecurityAuditService.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Security/SecurityAuditService.java) | `sanitize()` and `json()` — runtime enforcement of log redaction for L3/L4 data |
| [`Api/src/main/resources/logback-spring.xml`](../Api/src/main/resources/logback-spring.xml) | L2 log retention configuration (`maxHistory`, `totalSizeCap`) |
