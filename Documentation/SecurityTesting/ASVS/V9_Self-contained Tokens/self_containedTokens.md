# Self-contained Tokens — ASVS Security Requirements (V9.1 – V9.2)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design

---

## V9.1 — Token Source and Integrity

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V9.1.1 | 1 |  Planned | All JWT tokens issued by ArcadeHaven will be validated using their digital signature (HMAC-SHA256 or RS256) before any token content is trusted. Spring Security's JWT filter will reject any token with an invalid or missing signature with a 401 response. |
| V9.1.2 | 1 |  Planned | An algorithm allowlist will be configured in the JWT validation layer. Only HS256 or RS256 will be permitted. The `None` algorithm will be explicitly rejected. No mixed symmetric/asymmetric support is planned, avoiding key confusion risks. |
| V9.1.3 | 1 |  Planned | Key material for JWT validation will be pre-configured server-side via environment variables. JWT headers such as `jku`, `x5u`, and `jwk` will be ignored or validated against a strict allowlist to prevent attackers from supplying untrusted key sources. |

---

## V9.2 — Token Content

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V9.2.1 | 1 |  Planned | JWT tokens will include `exp` (expiration) and `nbf` (not before) claims. The validation layer will reject tokens outside their validity window. Access tokens will have a maximum lifetime of 15 minutes. |
| V9.2.2 | 2 |  Planned | ArcadeHaven will issue distinct token types (access token, refresh token). Each service endpoint will validate the token type before processing. Refresh tokens will not be accepted for authorization decisions. |
| V9.2.3 | 2 |  Planned | JWT tokens will include an `aud` (audience) claim set to `arcadehaven-api`. The validation layer will reject any token whose `aud` claim does not match the expected value. |
| V9.2.4 | 2 |  Planned | ArcadeHaven uses a single audience (`arcadehaven-api`), so audience confusion risk is low. If multi-audience support is introduced in future phases, each token will include a specific audience restriction and the issuer will validate audiences before issuance. |

---

## Summary

| Section | Total |  Planned |  N/A |
|---|---|---|---|
| V9.1 Token Source and Integrity | 3 | 3 | 0 |
| V9.2 Token Content | 4 | 4 | 0 |
| **Total** | **7** | **7** | **0** |

> All V9 requirements are directly applicable to ArcadeHaven as JWT is the core authentication mechanism.
> Key implementation points: algorithm allowlist (no `None`), `exp`/`nbf` validation, `aud` claim enforcement, and server-side key pre-configuration.
