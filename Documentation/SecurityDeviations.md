# ArcadeHaven — Accepted Security Deviations

Generated: 2026-06-15

This document formally records ASVS 5.0 controls that cannot be fully satisfied due to architectural constraints. For each deviation, it documents the business justification, residual risk, compensating controls currently in place, and the planned remediation path.

A control listed here is a **conscious, documented, and reviewed decision** — not an oversight. All deviations must be re-reviewed at the start of each sprint and removed when remediation is complete.

---

## Deviation Register

| ID | ASVS Control | Title | Status | Risk Level | Last Reviewed |
|----|-------------|-------|--------|-----------|--------------|
| DEV-01 | V10.4.4 | Only Authorization Code grant enabled | Partial — Direct Access Grant retained | Medium | 2026-06-15 |

---

## DEV-01 — V10.4.4: Only Authorization Code Grant Enabled

### Requirement

> Verify that the Authorization Server only supports the Authorization Code grant type with PKCE for applications that require user authentication.

### Current State

ArcadeHaven's Keycloak client `arcadehaven-public` has `directAccessGrantsEnabled: true`, which enables the OAuth 2.0 Resource Owner Password Credentials (ROPC) grant (also called Direct Access Grant or `grant_type=password`).

The `POST /api/auth/login` endpoint in `AuthService.login()` uses this grant internally: it forwards the user's credentials to Keycloak's token endpoint and returns the resulting JWT to the caller. This is the sole authentication pathway in the current architecture.

The Implicit Flow (`implicitFlowEnabled: false`) is **disabled** — the most dangerous grant type is removed.

### Business Justification

ArcadeHaven is currently a pure REST API with no frontend SPA. Authorization Code + PKCE requires a browser redirect to Keycloak's login page and back to a registered `redirect_uri`. Without a frontend, there is no callback endpoint to handle this redirect. Forcing Auth Code flow would prevent any form of user authentication in the current system.

This is a **temporary architectural constraint**, not a permanent design decision.

### Residual Risk

| Risk | Severity | Notes |
|------|---------|-------|
| User credentials reach the application server, not just the authorization server | Medium | Mitigated by the compensating controls below |
| Credential theft if the application server is compromised | Medium | Mitigated by HTTPS + short token lifetime |
| Phishing via compromised `POST /api/auth/login` endpoint | Low | Mitigated by brute-force protection + rate limiting |

### Compensating Controls

All the following controls are active and verified by automated tests:

| Control | Implementation | Test |
|---------|---------------|------|
| Implicit Flow disabled | `implicitFlowEnabled: false` in `keycloak/realm-export.json` | `OAuthGrantConfigTest.implicitFlow_isDisabled()` |
| PKCE S256 pre-configured | `pkce.code.challenge.method: S256` in client attributes | `OAuthGrantConfigTest.pkce_isConfiguredWithS256()` |
| Rate limiting on auth endpoint | Bucket4j 20 req/min per IP (`RateLimitFilter`) | `RateLimiterFilterTest` |
| Keycloak brute-force protection | 5 failures → increasing wait → lockout (`bruteForceProtected: true`) | `OAuthGrantConfigTest.bruteForceProtection_isEnabled()` |
| Short access token lifetime | 300 seconds (`accessTokenLifespan: 300`) | `OAuthGrantConfigTest.accessToken_lifetime_isAtMost300Seconds()` |
| Single-use refresh tokens | `revokeRefreshToken: true` | `OAuthGrantConfigTest.refreshToken_isSingleUse()` |
| Session revocation on logout | Keycloak Admin API `UserResource.logout()` called on `POST /api/auth/logout` | `AuthServiceLogoutTest` |
| Security audit logging | All auth events logged with IP, username, timestamp | `SecurityAuditService.recordLoginSuccess/Failure()` |
| All traffic over HTTPS | nginx TLS 1.2/1.3; HTTP → HTTPS redirect on port 80 | `NginxTlsConfigTest` |
| MFA required for ADMIN role | Conditional OTP flow gates on ADMIN realm role | `MfaConfigTest` |
| Deviation formally documented | Client `description` field references this deviation | `OAuthGrantConfigTest.directAccessGrant_hasDocumentedRationale_inClientDescription()` |

### Planned Remediation

When a frontend SPA is developed:
1. Disable `directAccessGrantsEnabled` on the `arcadehaven-public` client.
2. Remove `POST /api/auth/login` (or keep as an internal-only endpoint for non-browser clients).
3. Implement Authorization Code + PKCE flow in the SPA (Keycloak PKCE is already configured: `pkce.code.challenge.method: S256`).
4. Register the SPA's callback URL in `redirectUris`.

No backend code changes are required — the Spring Boot resource server is already stateless and accepts any valid Keycloak JWT regardless of which grant produced it.

### Risk Owner

Development Team

### References

- [`keycloak/realm-export.json`](../keycloak/realm-export.json) — `clients[arcadehaven-public].directAccessGrantsEnabled`
- [`Api/src/main/java/isep/desosfs/arcadehaven/Service/AuthService.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Service/AuthService.java) — `login()` method (ROPC call)
- [`Api/src/test/java/isep/desosfs/arcadehaven/Config/OAuthGrantConfigTest.java`](../Api/src/test/java/isep/desosfs/arcadehaven/Config/OAuthGrantConfigTest.java) — automated compensating-control tests
- ASVS 5.0 V10.4.4
