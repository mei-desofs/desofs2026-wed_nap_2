# Deliverable — Phase 2: Sprint 1

**Project:** ArcadeHaven — Secure Online Game Store  
**Team:** desofs2026_wed_nap_2  
**Sprint period:** Week 8 – Week 10 (20/04/2026 – 18/05/2026)  
**Submission:** 18/05/2026

---

## Summary

Sprint 1 of Phase 2 focused on the **SSDLC Implementation and Testing** steps. The team implemented the remaining security-critical functionalities identified in the Phase 1 threat model and ASVS tracker, integrated a full DevSecOps pipeline, and produced evidence of automated security testing across static, dynamic, and composition dimensions.

Key achievements:
- Completed all high-priority ASVS controls pending from Phase 1 (password policy, security headers, TLS configuration, SFTP host-key verification, audit logging, rate limiting, file MIME validation)
- Established a fully automated CI/CD pipeline with SAST (CodeQL), SCA (OWASP Dependency Check), DAST (OWASP ZAP), secret scanning (Gitleaks), container scanning (Docker Scout + Trivy), code quality (SpotBugs + Checkstyle), and SBOM generation (CycloneDX)
- Conducted code reviews on every PR merged to `main`
- Maintained traceability between ASVS controls, security tests, and implementation evidence

---

## Development

### Implemented Features

All features below were merged to `main` through reviewed Pull Requests during Sprint 1.

#### Authentication & Session Management
- `POST /api/auth/login` — Proxies credentials to Keycloak token endpoint; returns JWT access/refresh tokens
- `POST /api/auth/register` — Creates Keycloak user + local DB record; enforces password policy
- `POST /api/auth/logout` — Revokes all active Keycloak sessions (ASVS V6.8.3)

#### Password Policy (ASVS V6.2 / RNF-16)
- Context-specific blocklist (terms like "arcadehaven", "password", etc.)
- Common password list check (top 10k list via HIBP API integration)
- Complexity rules: min 12 chars, uppercase, lowercase, digit, special character
- Implementation: [`Service/PasswordPolicyService.java`](../../../Api/src/main/java/isep/desosfs/arcadehaven/Service/PasswordPolicyService.java)

#### Security Headers (ASVS V3 / V12)
- HSTS with `max-age=31536000; includeSubDomains` on all responses
- `X-Content-Type-Options: nosniff`
- `Cache-Control: no-store` on sensitive responses
- CORS restricted to explicit origin allowlist via `security.cors.allowed-origins`
- Implementation: [`Security/SecurityConfig.java`](../../../Api/src/main/java/isep/desosfs/arcadehaven/Security/SecurityConfig.java)

#### TLS Configuration (ASVS V12.1)
- TLS 1.2 / 1.3 only; forward-secrecy (ECDHE) cipher suites enforced
- Configuration: [`application.properties`](../../../Api/src/main/resources/application.properties) (`server.ssl.enabled-protocols`, `server.ssl.ciphers`)
- TLS terminated at reverse proxy; Spring Boot SSL disabled in Docker via `SERVER_SSL_ENABLED=false`

#### Rate Limiting (ASVS V6.3 / RNF-19)
- 20 requests/minute per IP on `/api/auth/login`, `/api/auth/register`, `/api/publisher/games`
- Token-bucket algorithm via Bucket4j; HTTP 429 with JSON error on breach
- `X-Forwarded-For` aware for reverse-proxy deployments
- Implementation: [`Security/RateLimitFilter.java`](../../../Api/src/main/java/isep/desosfs/arcadehaven/Security/RateLimitFilter.java)

#### Audit Logging (ASVS V16 / RNF-05 / RNF-13)
- Dedicated `SECURITY_AUDIT` logger writing to `logs/arcadehaven.log`
- Events logged: login success/failure, registration, logout, access denied, invalid token
- Implementation: [`Security/SecurityEventHandler.java`](../../../Api/src/main/java/isep/desosfs/arcadehaven/Security/SecurityEventHandler.java)

#### File Handling Security (ASVS V5 / RNF-21 / RNF-22)
- MIME-type validation via Apache Tika (content-based, not extension-based)
- 25 MB upload limit enforced at Tomcat level; `MaxUploadSizeExceededException` → HTTP 400
- SFTP trusted host-key verification via SSH `known_hosts` file (`sftp.known-hosts-path`)
- Path traversal prevention in file path construction
- Implementation: [`Validation/FileValidator.java`](../../../Api/src/main/java/isep/desosfs/arcadehaven/Validation/FileValidator.java), [`Service/SftpStorageService.java`](../../../Api/src/main/java/isep/desosfs/arcadehaven/Service/SftpStorageService.java)

#### Cryptography (ASVS V11)
- Activation keys generated with `SecureRandom` (not `java.util.Random`)
- SHA-1 removed from `PasswordPolicyService`; replaced with HIBP k-anonymity API (SHA-1 is used by HIBP's own protocol, not stored)
- Fix applied in PR #22 following CodeQL + reviewer flag

#### Authorization & RBAC (ASVS V8 / RNF-08 / RNF-18)
- Three roles enforced: `ADMIN`, `PUBLISHER`, `BUYER`
- Path-level rules in `SecurityConfig` + method-level `@PreAuthorize` on controllers
- Ownership checks: publisher can only manage own games; buyer can only access own orders/library
- IDOR prevention: all resource lookups scoped by authenticated user's identity

#### Error Handling (ASVS V16 / RNF-14)
- Global exception handler: no stack traces, no internal details in responses
- Generic error responses for 401, 403, 404, 409, 500
- Implementation: [`Exception/GlobalExceptionHandler.java`](../../../Api/src/main/java/isep/desosfs/arcadehaven/Exception/GlobalExceptionHandler.java)

#### Additional Features
- Game catalogue with search, filter by category, and RAWG API metadata import
- Publisher game management (create, update, upload cover/screenshots, view metrics)
- Admin approval workflow (approve/reject/remove games)
- Order lifecycle (create, pay, view invoice)
- Library management per buyer
- User profile management (view/update email)
- Admin user management (activate/deactivate, change role)
- Invoice generation with PDF storage via SFTP

---

### Security Practices Adopted

| Practice | Tooling | When Applied |
|---|---|---|
| Peer code review | GitHub PR reviews | Every merge to `main` |
| Static analysis (SAST) | CodeQL (`security-extended` + `security-and-quality` queries) | Every push/PR to `main` |
| Dependency vulnerability scan (SCA) | OWASP Dependency Check + NVD API | Every push/PR to `main` |
| Code quality / code smells | SpotBugs + Checkstyle | Every push/PR to `main` |
| Secret / credential scanning | Gitleaks | Every push/PR to `main` |
| Container vulnerability scan | Docker Scout + Trivy | Every push/PR to `main` |
| Dynamic scan (DAST) | OWASP ZAP Baseline | Every push/PR to `main` (full Docker stack) |
| SBOM generation | CycloneDX Maven Plugin | Every build |
| Automated tests | JUnit 5 (unit + integration) | Every build |

---

### Code Reviews

All PRs were reviewed by a second team member before merge. Reviews validated implementation correctness, pipeline green status, and absence of introduced security regressions.

| PR | Title | Author | Reviewer | Outcome |
|---|---|---|---|---|
| [#22](https://github.com/mei-desofs/desofs2026-wed_nap_2/pull/22) | Implementation of security functionalities of the ASVS file | DiogoPereira-1221137 | diogojms2 | Approved after SHA-1 fix flagged by CodeQL |
| [#24](https://github.com/mei-desofs/desofs2026-wed_nap_2/pull/24) | Improvement of the security for the file manager and TLS Protocol | DiogoPereira-1221137 | diogojms2 | Approved — "Security implementations and test validated. All pipelines passed." |
| [#25](https://github.com/mei-desofs/desofs2026-wed_nap_2/pull/25) | Pipeline (Docker Build, Scan & DAST) | diogojms2 | DiogoPereira-1221137 | Approved — 13/14 checks passed; Docker build, scan and DAST confirmed working |
| [#19](https://github.com/mei-desofs/desofs2026-wed_nap_2/pull/19) | Implementation of errors treatment and security improvement | DiogoPereira-1221137 | diogojms2 | Approved — "All pipeline passed. Tests cover enough domain." |
| [#14](https://github.com/mei-desofs/desofs2026-wed_nap_2/pull/14) | Documentation | diogojms2 | DiogoPereira-1221137 | Merged |
| [#8](https://github.com/mei-desofs/desofs2026-wed_nap_2/pull/8) | Pipeline (initial CI/CD) | diogojms2 | DiogoPereira-1221137 | Merged |
| [#7](https://github.com/mei-desofs/desofs2026-wed_nap_2/pull/7) | Feature: Implement remote file storage | diogojms2 | DiogoPereira-1221137 | Merged |
| [#2](https://github.com/mei-desofs/desofs2026-wed_nap_2/pull/2) | Fix: Correction of the implementation with Keycloak | DiogoPereira-1221137 | diogojms2 | Merged |

**Notable review event (PR #22):** GitHub Advanced Security bot identified risky use of SHA-1 in `PasswordPolicyService.java`. Reviewer diogojms2 flagged this in the review; author corrected the usage to align with the HIBP k-anonymity API protocol (where SHA-1 is mandated by the external API, not used for storage). Reviewer approved after confirming the fix and the intent.

---

## Build and Test

### Component Inventory (SBOM)

A Software Bill of Materials is generated on every build via the [CycloneDX Maven Plugin](https://github.com/CycloneDX/cyclonedx-maven-plugin) and uploaded as a build artifact (`sbom`).

Key runtime dependencies:

| Component | Version | Purpose |
|---|---|---|
| Spring Boot | 3.4.x | Application framework |
| Spring Security OAuth2 Resource Server | 3.4.x | JWT validation |
| Keycloak Admin Client | 26.x | User/role management |
| PostgreSQL Driver | 42.x | Database connectivity |
| Flyway | 10.x | Schema migration |
| Bucket4j | 8.x | Rate limiting |
| Apache Tika | 3.x | MIME-type detection |
| CycloneDX Maven Plugin | 2.x | SBOM generation |
| OWASP Dependency Check Maven Plugin | 12.x | SCA |

Full SBOM (CycloneDX JSON/XML) available in the [GitHub Actions build artifacts](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/build.yml) (`sbom` artifact).

---

### Test Execution

Tests are executed automatically on every push/PR via the [Build API and Test](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/build.yml) workflow (`mvn clean package`).

| Test Class | Type | What is Validated |
|---|---|---|
| `GameDomainTest` | Unit | Game aggregate invariants, status transitions |
| `LibraryDomainTest` | Unit | Library creation, entry management |
| `OrderDomainTest` | Unit | Order creation, item addition, status flow |
| `OrderItemTest` | Unit | OrderItem value object constraints |
| `GameServiceTest` | Unit | Game service logic, ownership enforcement |
| `LibraryServiceTest` | Unit | Library access scoping per buyer |
| `AuthServiceLogoutTest` | Unit | Logout Keycloak session revocation |
| `InvoiceServiceTest` | Unit | Invoice generation and storage |
| `PasswordPolicyServiceTest` | Unit | Blocklist, complexity, HIBP integration |
| `LocalStorageServiceTest` | Unit | File storage path safety |
| `SecurityHeadersTest` | Integration | HSTS, X-Content-Type-Options, Cache-Control present |
| `WebServerTlsConfigTest` | Integration | TLS protocol and cipher configuration |
| `FlywayMigrationTest` | Integration | All migrations apply cleanly against real DB |
| `ConcurrencyConfigTest` | Integration | Thread pool and connection pool configuration |

Test results and build logs are available in the [GitHub Actions run history](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/build.yml).

---

### SAST Results (CodeQL)

**Workflow:** [sast-codeql.yml](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/sast-codeql.yml)  
**Queries:** `security-extended` + `security-and-quality`  
**Language:** Java 21

CodeQL runs on every push/PR to `main`. Results are published to **GitHub Advanced Security** (Security tab → Code scanning alerts).

Findings resolved during Sprint 1:
- **Unsafe random number generation** in activation key generation → fixed to use `SecureRandom`
- **Risky SHA-1 usage** in `PasswordPolicyService` → reviewed and confirmed compliant with HIBP API protocol (flagged in PR #22 review; accepted after explanation)

No open critical/high CodeQL alerts remain at time of submission.

> Full results: [GitHub Code Scanning Alerts](https://github.com/mei-desofs/desofs2026-wed_nap_2/security/code-scanning)

---

### SCA Results (OWASP Dependency Check)

**Workflow:** [sca-dependency-check.yml](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/sca-dependency-check.yml)  
**Tool:** OWASP Dependency Check (NVD API)  
**Output:** HTML report uploaded as `dependency-check-report` artifact

The scan checks all Maven dependencies against the NVD CVE database. The full HTML report is downloadable from the [workflow run artifacts](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/sca-dependency-check.yml).

An additional **Dependabot PR (#1)** was merged during this sprint, automatically bumping the Apache Tika dependency to patch a known vulnerability.

---

### DAST Results (OWASP ZAP)

**Workflow:** [build-docker-and-dast.yml](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/build-docker-and-dast.yml)  
**Tool:** OWASP ZAP Baseline Scan (`zaproxy/action-baseline@v0.14.0`)  
**Target:** `http://localhost:8080` (full Docker Compose stack)

The DAST pipeline:
1. Builds the Docker image
2. Scans the image with **Docker Scout** and **Trivy** (artifact: `trivy-report`)
3. Starts the full stack (app + Keycloak + PostgreSQL)
4. Waits for Keycloak and app health checks to pass
5. Runs OWASP ZAP Baseline scan
6. Uploads ZAP HTML report as `zap-report` artifact

ZAP report available in the [workflow run artifacts](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/build-docker-and-dast.yml).

---

### Code Quality

**Workflow:** [code-quality.yml](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/code-quality.yml)

| Tool | Purpose | Artifact |
|---|---|---|
| **Checkstyle** | Formatting, naming conventions, code style | Pipeline log |
| **SpotBugs** | Potential bugs, unsafe patterns, code smells | `spotbugs-report` (XML) |

SpotBugs report uploaded as `spotbugs-report` artifact on every run.

---

### Secret Scanning

**Workflow:** [secret-scanning.yml](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/secret-scanning.yml)  
**Tool:** Gitleaks with custom `.gitleaks.toml` configuration

Gitleaks scans the entire repository on every push/PR to detect accidentally committed secrets, API keys, passwords, or tokens. No secrets were detected during Sprint 1.

---

## Pipeline

All workflows trigger automatically on every push or pull request to `main`.

| Workflow | Trigger | Purpose |
|---|---|---|
| [Build API and Test](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/build.yml) | push/PR to `main` | Compile, test, generate SBOM |
| [SAST - CodeQL](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/sast-codeql.yml) | push/PR to `main` | Static security analysis |
| [SCA - Dependency Check](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/sca-dependency-check.yml) | push/PR to `main` | Dependency vulnerability scan |
| [Code Quality](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/code-quality.yml) | push/PR to `main` | SpotBugs + Checkstyle |
| [Secret Scanning](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/secret-scanning.yml) | push/PR to `main` | Gitleaks credential scan |
| [Docker Build, Scan & DAST](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/build-docker-and-dast.yml) | push/PR to `main` | Container build, Docker Scout, Trivy, OWASP ZAP |
| [Release Please](https://github.com/mei-desofs/desofs2026-wed_nap_2/actions/workflows/release-please.yml) | push to `main` | Automated versioning and changelog |

Pipeline enforcement: all workflows must pass before a PR can be merged. Reviewers validated pipeline status as part of the code review process on every PR.

---

## ASVS

The ASVS 5.0 assessment is maintained in [`Deliverables/ASVS_5.0_Tracker.xlsx`](../../ASVS_5.0_Tracker.xlsx).

Evidence for each compliant control is documented in [`Documentation/ASVS_Compliant_Evidence.md`](../../../Documentation/ASVS_Compliant_Evidence.md), mapping every ASVS control to the specific source file(s) that implement or validate it.

Traceability between security requirements, abuse cases, STRIDE threats, and test cases is documented in [`Documentation/SecurityTesting/TraceabilityMatrix.md`](../../../Documentation/SecurityTesting/TraceabilityMatrix.md).

### Controls addressed in Sprint 1

| ASVS Category | Controls Addressed |
|---|---|
| V1 — Encoding & Sanitization | V1.1.1 — Input decoded to canonical form; `@NoHtml` validator on all request DTOs |
| V3 — Web Frontend Security | V3.5.1 CSRF via stateless JWT; V3.5.2 Content-Type enforcement; V3.5.3 safe HTTP verbs |
| V4 — API & Web Service | V4.1.1 Content-Type headers; V4.2.1 request boundary enforcement |
| V5 — File Handling | V5.2.1 upload size limit; file MIME validation; SFTP path safety |
| V6 — Authentication | V6.2.8 no password truncation; V6.3.1 rate limiting + Keycloak brute-force; V6.3.2 no default accounts; V6.4.2 no KBA; V6.8.2 JWT validation; V6.8.3 session revocation on logout |
| V8 — Authorization | V8.2.1 function-level RBAC; V8.2.2 IDOR prevention (ownership checks); V8.2.3 field-level access via response DTOs |
| V9 — Self-contained Tokens | V9.1.1–V9.1.3 JWT signature validation; algorithm allowlist; pinned JWKS; V9.2.1 `exp`/`nbf` validation |
| V11 — Cryptography | SecureRandom for key generation; TLS 1.2/1.3 only; forward-secrecy cipher suites |
| V12 — Secure Communication | HSTS; TLS protocol/cipher restrictions; SFTP host-key verification |
| V13 — Configuration | No sensitive data in error messages; cache control headers; security-relevant config via env vars |
| V14 — Data Protection | Response DTOs prevent internal field leakage; audit log separation |
| V15 — Secure Coding | SpotBugs + Checkstyle enforced in CI; CodeQL security queries |
| V16 — Logging & Error Handling | Dedicated audit log file; structured security events; no sensitive data in logs or error responses |
