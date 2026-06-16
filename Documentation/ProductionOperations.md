# ArcadeHaven — Production & Operations

> **Rubric coverage:** Phase 2 Sprint 2 — *Production 5%* and *Operate 5%*
>
> Evidence of: production infrastructure management · logging & traceability · incident management · patch management · configuration management · system monitoring · penetration testing · vulnerability management.

---

## Part 1 — Production

### 1.1 Production Infrastructure Management

ArcadeHaven's production environment is fully described as **infrastructure-as-code**. No manual server configuration is required.

| Artefact | Purpose | Location |
|----------|---------|----------|
| `docker-compose.yml` | Defines all services (app, keycloak, keycloak-db, nginx, clamav) with networks, volumes, health checks | `Api/docker-compose.yml` |
| `Dockerfile` | Multi-stage build: Maven build → distroless JRE runtime image (non-root user `appuser`) | `Api/Dockerfile` |
| `nginx/nginx.conf` | Reverse proxy with TLS termination; upstream to Spring Boot on port 8080 | `Api/nginx/nginx.conf` |
| `nginx/generate-self-signed-cert.sh` | Generates TLS certificate for CI/dev; in production replace with a CA-signed certificate | `Api/nginx/generate-self-signed-cert.sh` |
| `keycloak/realm-export.json` | Declarative Keycloak realm config (clients, roles, scopes) — reproducible from scratch | `Api/keycloak/realm-export.json` |
| Flyway migrations | Versioned DDL applied at startup; schema changes tracked in version control | `Api/src/main/resources/db/migration/` |

**CI/CD pipeline** (`pipeline.yml`) enforces a strict gate sequence before any image is deployed:

```
secret-scanning
    └── build
            ├── sast-codeql
            ├── sast-code-quality (Semgrep / SpotBugs)
            └── sca-dependency-check
                    └── docker-dast (Docker Scout + Trivy + OWASP ZAP)
                                └── security-and-smoke-tests (k6 + integration tests)
```

No stage proceeds unless all upstream gates pass. All workflow definitions are in [`.github/workflows/`](../.github/workflows/).

---

### 1.2 Configuration Management

All runtime secrets and environment-specific values are injected via **environment variables** — never hardcoded in source.

| Variable group | Variables | Injection point |
|---|---|---|
| Database | `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` | `.env` (dev) / GitHub Secrets (CI) |
| Keycloak | `KEYCLOAK_JWK_SET_URI`, `KEYCLOAK_BACKEND_CLIENT_SECRET`, … | `.env` / GitHub Secrets |
| SFTP | `SFTP_HOST/PORT/USERNAME/PASSWORD/REMOTE_DIR` | `.env` / GitHub Secrets |
| Flyway DDL user | `FLYWAY_DATASOURCE_URL/USERNAME/PASSWORD` | Production env only (falls back to app user in dev) |
| TLS mode | `DB_SSL_MODE` | `.env` (`prefer` in dev → `require` in prod) |
| CORS | `CORS_ALLOWED_ORIGINS` | `.env` / deployment env |

Schema changes follow a **forward-only Flyway migration** strategy: each `V{n}__description.sql` file is immutable once merged. Rollback is achieved by adding a new migration, not by modifying existing ones.

Key policy documents:
- [Key Management Policy](KeyManagementPolicy.md) — rotation procedures for JWT signing keys, SFTP keys, and client secrets
- [Data Classification Policy](DataClassificationPolicy.md) — what data lives where and at what sensitivity level
- [Security Deviations](SecurityDeviations.md) — formally documented deviations from baseline security requirements

---

### 1.3 Logging & Traceability

All security-relevant events are written as **structured JSON** to a dedicated `SECURITY_AUDIT` logger with independent file and console appenders.

**Full inventory:** [LoggingInventory.md](LoggingInventory.md)

**Log format** (every line is a complete JSON object):
```json
{"event":"SECURITY_EVENT","ts":"2026-06-16T10:00:00.000Z","type":"LOGIN_SUCCESS","ip":"10.0.1.5","user":"alice"}
```

| Event class | Types logged | Level |
|---|---|---|
| Authentication | `LOGIN_SUCCESS`, `REGISTRATION_SUCCESS`, `REGISTRATION_FAILURE`, `UNAUTHORIZED` | INFO / WARN |
| Authorisation | `ACCESS_DENIED` | WARN |
| Anti-automation | `RATE_LIMIT_EXCEEDED`, `VALIDATION_FAILURE` | WARN |
| Admin audit | `ADMIN_ACTION` (with sub-action and target) | INFO |
| Threshold alert | `THRESHOLD_EXCEEDED` — IP triggers ≥10 security events / 60 s | ERROR |

**Retention:** 30-day rolling file at `/app/logs/arcadehaven.log`; 500 MB total cap (`logback-spring.xml`).

**Sensitive data redaction** (`SecurityAuditService.sanitize()`): JWT tokens → `[JWT_REDACTED]`, activation keys → `[KEY_REDACTED]`, form-encoded credentials → `[REDACTED]`.

**Traceability chain:** every security event records the client IP (resolved from trusted `X-Forwarded-For` via Tomcat `RemoteIpFilter`), timestamp (UTC), and the specific operation — sufficient to reconstruct the sequence of events for any incident.

---

### 1.4 Patch Management

Defined in [VulnerabilityRemediationPolicy.md](VulnerabilityRemediationPolicy.md).

**Automated discovery tools running on every CI push:**

| Tool | What it scans | Workflow | Output |
|---|---|---|---|
| OWASP Dependency Check | Java dependencies vs NVD CVE database | `sca-dependency-check.yml` | `report-sca` artifact |
| CycloneDX SBOM | Full software bill of materials | `build.yml` | `bom.xml` / `bom.json` |
| GitHub Dependabot | Dependency version alerts + auto-PRs | Repository setting | GitHub Security tab |
| GitHub Secret Scanning | Committed secrets / credentials | `secret-scanning.yml` | GitHub Security tab |
| Docker Scout | Container image CVE scan | `build-docker-and-dast.yml` | CI log |
| Trivy | Container image + filesystem scan | `build-docker-and-dast.yml` | `report-docker-dast` artifact |

**Remediation SLA (CVSS v3):**

| Severity | Score | Deadline | Interim mitigation |
|---|---|---|---|
| Critical | 9.0–10.0 | **7 calendar days** | Required — disable feature or WAF rule |
| High | 7.0–8.9 | **30 calendar days** | Required if unauthenticated exploit exists |
| Medium | 4.0–6.9 | **90 calendar days** | Only if public exploit exists |
| Low | 0.1–3.9 | Next scheduled release | Not required |

False positives are suppressed in `Api/dependency-check-suppressions.xml` with reviewer sign-off and are re-reviewed quarterly.

---

### 1.5 Incident Management

#### 1.5.1 Detection

Incidents are surfaced automatically by the application's threshold alerting:

- **Trigger:** `SecurityAuditService.checkAndAlert()` fires a `THRESHOLD_EXCEEDED` (level ERROR) `SECURITY_ALERT` event when a single IP generates ≥10 security events within 60 seconds.
- **Source code:** `Api/src/main/java/isep/desosfs/arcadehaven/Security/SecurityAuditService.java`
- **CI detection:** ZAP, Trivy, and Dependency Check reports are uploaded as GitHub Actions artifacts after every pipeline run — a failed gate blocks merge.

#### 1.5.2 Incident Response Runbook

**Step 1 — Detect**

| Signal | Source | Action |
|---|---|---|
| `THRESHOLD_EXCEEDED` in logs | `/app/logs/arcadehaven.log` | Proceed to Step 2 |
| CI gate failure (CVSS ≥ 7.0) | GitHub Actions | Open private security issue, assign to on-call |
| Dependabot / Secret Scanning alert | GitHub Security tab | Assess and triage within 24 h |

**Step 2 — Assess**

1. Identify the affected IP, user, and event type from the JSON log line.
2. Determine scope: is the event isolated (brute-force attempt) or broad (data exfiltration pattern)?
3. Classify severity using CVSS v3 if a vulnerability is involved; otherwise use the event frequency and type.

**Step 3 — Contain**

| Scenario | Immediate action |
|---|---|
| Brute-force / credential stuffing | Block IP at nginx (`deny <IP>;` in `nginx.conf` + `docker compose exec nginx nginx -s reload`) |
| Compromised service-account secret | Rotate `KEYCLOAK_BACKEND_CLIENT_SECRET` in Keycloak Admin Console and update GitHub Secret; redeploy |
| Compromised DB credentials | Rotate `SPRING_DATASOURCE_PASSWORD` in PostgreSQL and update `.env` / GitHub Secret; redeploy |
| Compromised JWT signing key | Trigger Keycloak key rotation (Admin Console → Realm Settings → Keys → Generate); all existing tokens are immediately invalidated |
| Malicious file uploaded | Remove the file from SFTP storage; suspend the publisher account via `POST /api/admin/users/{id}/suspend` (ADMIN role required) |

**Step 4 — Remediate**

1. Patch the root cause (dependency upgrade, configuration fix, code change).
2. Re-run CI pipeline — all gates must pass before redeployment.
3. Verify fix via DAST/SAST reports in CI artifacts.

**Step 5 — Document**

1. Record the incident in a private GitHub issue: timeline, affected systems, root cause, fix applied.
2. Update `Documentation/SecurityDeviations.md` if the incident exposed a gap in the security baseline.
3. Update `Documentation/VulnerabilityRemediationPolicy.md` if the SLA or process needs refinement.

---

## Part 2 — Operate

### 2.1 System & User Monitoring

#### Health endpoint

The `/actuator/health` endpoint is the only exposed Spring Boot Actuator endpoint (all others are disabled). It returns `{"status":"UP"}` and is used by Docker Compose health checks and the CI pipeline's readiness probe.

```yaml
# application.properties
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=never
```

The CI pipeline (`build-docker-and-dast.yml`) polls `https://localhost/actuator/health` (via nginx TLS) before proceeding to DAST scanning — ensuring DAST always runs against a live, healthy instance.

#### Automated threshold alerting

`SecurityAuditService.checkAndAlert()` tracks per-IP event counts in a sliding 60-second window. When ≥10 security events are detected from a single IP, a `SECURITY_ALERT` with level ERROR is emitted — designed to be ingested by any SIEM that routes ERROR-level events to an on-call channel.

#### k6 Load & Availability Tests

Performance baselines are defined and executed in CI. Full results: [k6Report.md](Reports/k6Report.md).

| Test | Scenario | Result |
|---|---|---|
| Response Time | 50 concurrent VUs, 1 min ramp-up, p95 < 500 ms | Passing |
| Load Test | Ramp to 100 VUs over 3 min, p95 < 1 s | Passing |
| Availability | 30-min sustained load, error rate < 1% | Passing |

k6 test files: `Api/k6/`; Docker Compose k6 config: `Api/docker-compose.k6.yml`.

---

### 2.2 Penetration Testing (DAST)

Two OWASP ZAP scans run automatically on every pipeline execution against a fully deployed Docker Compose stack with a real JWT:

| Scan | Target | Report |
|---|---|---|
| ZAP Baseline Scan | `https://localhost` — passive crawl of all reachable pages | `zap-baseline-report.html/json/md` |
| ZAP API Scan | `https://localhost/v3/api-docs` — active scan of every OpenAPI endpoint with injected Bearer token | `zap-api-report.html/json/md` |

Both scans run with a valid admin JWT injected via ZAP's replacer rule — authenticated endpoints are exercised, not just public ones.

Reports are uploaded as the `report-docker-dast` GitHub Actions artifact after every pipeline run.

**Container image scanning** also runs before ZAP:

| Tool | Scope |
|---|---|
| Docker Scout | CVE scan of `arcadehaven-api:latest` against Docker Hub vulnerability database |
| Trivy | Filesystem + OS package scan of the image; report saved as `reports/trivy/trivy-report.json` |

Workflow: [`.github/workflows/build-docker-and-dast.yml`](../.github/workflows/build-docker-and-dast.yml)

---

### 2.3 Vulnerability Management

Full pipeline gate sequence on every push to `main` or PR:

| Stage | Tool | Workflow | Blocks merge? |
|---|---|---|---|
| Secret scanning | TruffleHog / GitHub Secret Scanning | `secret-scanning.yml` | Yes |
| Build & SBOM | Maven + CycloneDX | `build.yml` | Yes |
| SAST (semantic) | GitHub CodeQL (Java) | `sast-codeql.yml` | Yes |
| SAST (quality) | Semgrep + SpotBugs | `sast-code-quality.yml` | Yes |
| SCA | OWASP Dependency Check | `sca-dependency-check.yml` | Yes |
| Container CVE | Docker Scout + Trivy | `build-docker-and-dast.yml` | Yes |
| DAST | OWASP ZAP (baseline + API) | `build-docker-and-dast.yml` | Yes |
| Integration/Security tests | k6 + REST-assured | `security-and-smoke-tests.yml` | Yes |

All reports are retained as GitHub Actions artifacts (`report-sca`, `report-docker-dast`, `report-build-test`) for audit trail purposes.

Remediation policy and CVSS-based SLAs: [VulnerabilityRemediationPolicy.md](VulnerabilityRemediationPolicy.md)

---

### 2.4 Backup & Restore

**Current state — known limitation:**

Database backup and restore is **not automated** in the current implementation. This is a recognised gap.

| Component | Current state | Production recommendation |
|---|---|---|
| PostgreSQL (app DB) | No scheduled backup | `pg_dump` cron job to offsite object storage (e.g. S3-compatible); daily full + hourly incremental; 30-day retention |
| PostgreSQL (Keycloak DB) | No scheduled backup | Same as above; Keycloak realm export (`keycloak/realm-export.json`) serves as a configuration backup |
| SFTP file storage | No snapshot | Object storage versioning or rsync to secondary host |
| Keycloak realm config | `keycloak/realm-export.json` in version control | Already version-controlled — sufficient for configuration restore |

The `keycloak/realm-export.json` file allows a full Keycloak realm to be recreated from scratch by passing it to the `--import-realm` Keycloak startup flag, which is already wired into `docker-compose.yml`.

---

## Summary

| Criterion | Evidence | Status |
|---|---|---|
| Production infrastructure management | Docker Compose IaC, 11 CI/CD workflows, multi-stage Dockerfile | ✅ |
| Logging & traceability | JSON audit log, 30-day retention, per-event traceability, `LoggingInventory.md` | ✅ |
| Incident management | Threshold alerting + 5-step response runbook (§1.5) | ✅ |
| Patch management | OWASP Dependency Check + Dependabot + CVSS SLA in `VulnerabilityRemediationPolicy.md` | ✅ |
| Configuration management | Env-var injection, Flyway versioned migrations, `KeyManagementPolicy.md` | ✅ |
| System & user monitoring | `/actuator/health`, `THRESHOLD_EXCEEDED` alerts, k6 load/availability tests | ✅ |
| Penetration testing | OWASP ZAP baseline + API scan on every CI run (authenticated) | ✅ |
| Vulnerability management | Full 8-stage CI gate: secret scan → SAST → SCA → container scan → DAST | ✅ |
| Backup & restore | Not automated — documented as known limitation with remediation path | ⚠️ Documented |
