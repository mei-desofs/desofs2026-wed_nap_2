[← Back to index page](../../../Overview/overview.md)

# Secure Communication — ASVS Security Requirements (V12.1 – V12.3)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design

---

## V12.1 — General TLS Security Guidance

| Req ID | Level | Status | Observations                                                                                                                                                                                                                                        |
|---|---|---|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| V12.1.1 | 1 |  Planned | ArcadeHaven will enforce TLS 1.2 and TLS 1.3 only. TLS 1.0 and 1.1 will be explicitly disabled in the Spring Boot embedded Tomcat configuration and in the reverse proxy (Nginx/Docker). TLS 1.3 will be set as the preferred version.              |
| V12.1.2 | 2 |  Planned | Only strong cipher suites will be enabled. Weak ciphers (e.g. RC4, 3DES, NULL) will be explicitly disabled. For Level 2, cipher suites supporting forward secrecy (ECDHE-based) will be preferred and configured in the reverse proxy TLS settings. |
| V12.1.3 | 2 |  N/A | ArcadeHaven does not implement mutual TLS (mTLS) client certificate authentication in Phase 1. Authentication is handled via JWT Bearer tokens.                                                                                                     |
| V12.1.4 | 3 |  N/A | OCSP Stapling needed to be configured in the reverse proxy (Nginx) for production deployment in Phase 2 Sprint 2. Not applicable for the development environment.                                                                                   |
| V12.1.5 | 3 |  N/A | Encrypted Client Hello (ECH) needed to be evaluated for Phase 2 Sprint 2 during production deployment. Dependent on the hosting provider and reverse proxy support.                                                                                 |

---

## V12.2 — HTTPS Communication with External Facing Services

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V12.2.1 | 1 |  Planned | All client-to-API communication will be over HTTPS only. HTTP connections will not be redirected but rejected outright to prevent silent plaintext data leakage. Enforced via Spring Security and reverse proxy configuration. |
| V12.2.2 | 1 |  Planned | A publicly trusted TLS certificate (e.g. Let's Encrypt) will be used for the ArcadeHaven API in production. Self-signed certificates will only be used in local development environments and will never be used in staging or production. |

---

## V12.3 — General Service to Service Communication Security

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V12.3.1 | 2 |  Planned | All connections between ArcadeHaven components will use TLS — including the API-to-PostgreSQL connection (via `ssl=true` in the JDBC URL) and any external API calls (e.g. RAWG API). No fallback to unencrypted protocols is permitted. |
| V12.3.2 | 2 |  Planned | The ArcadeHaven backend will validate TLS certificates when making outbound calls to external services (e.g. RAWG API, HaveIBeenPwned API). Certificate validation will not be disabled in any environment, including development. |
| V12.3.3 | 2 |  Planned | Internal communication between the Spring Boot application and the PostgreSQL database will use TLS. Docker network communication will be reviewed to ensure no unencrypted internal HTTP traffic occurs. |
| V12.3.4 | 2 |  Planned | Internal service certificates will be trusted explicitly. If self-signed certificates are used for the database connection in development, the application will be configured to trust only that specific certificate, not all self-signed certificates globally. |
| V12.3.5 | 3 |  N/A | ArcadeHaven is a monolithic back-end application in Phase 1, not a microservice architecture. Intra-service mTLS and service mesh are not applicable in the current scope. |

---

## Summary

| Section | Total |  Planned |  N/A |
|---|---|---|---|
| V12.1 General TLS Security Guidance | 5 | 4 | 1 |
| V12.2 HTTPS Communication with External Facing Services | 2 | 2 | 0 |
| V12.3 General Service to Service Communication Security | 5 | 4 | 1 |
| **Total** | **12** | **10** | **2** |

> Almost all V12 requirements are Planned as TLS configuration is essential for a production REST API.
> Key implementation points: TLS 1.2/1.3 only, strong cipher suites with forward secrecy, HTTPS enforcement,
> certificate validation on outbound calls, and encrypted database connections.
