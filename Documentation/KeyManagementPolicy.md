# ArcadeHaven — Cryptographic Key Management Policy

**Version:** 1.0  
**Date:** 2026-05-17  
**Standard alignment:** NIST SP 800-57 Part 1 Rev. 5  
**ASVS control:** V11.1.1 [L2]

---

## 1. Scope

This policy covers all cryptographic key material used by the ArcadeHaven platform:

| Asset ID | Asset | Algorithm | Owner |
|----------|-------|-----------|-------|
| KA-01 | JWT signing key (RS256 private key) | RSA-2048 | Keycloak |
| KA-02 | BCrypt password hash | BCrypt (cost ≥ 12) | Keycloak |
| KA-03 | Database credentials | AES-256-GCM (at-rest, PostgreSQL) | Infrastructure |
| KA-04 | Keycloak Admin client secret | HMAC-SHA256 | Keycloak |
| KA-05 | SFTP SSH private key | RSA-2048 or Ed25519 | Infrastructure |
| KA-06 | TLS certificates (reverse proxy) | RSA-2048 / ECDSA P-256 | Infrastructure |

---

## 2. Key Generation Requirements

All cryptographic key material **must** be generated using an approved CSPRNG:

- **Java:** `java.security.SecureRandom` (NativePRNGNonBlocking on Linux)
- **Keycloak:** Internal key generation for RSA and HMAC keys
- **Minimum entropy:** 128 bits for symmetric keys; 2048-bit RSA or 256-bit ECDSA for asymmetric

Keys **must not** be:
- Derived from deterministic sources (sequential IDs, UUIDs, timestamps)
- Hardcoded in source code or configuration files
- Reused across environments (dev, staging, production)

---

## 3. Key Storage

| Asset | Storage Mechanism | Access Control |
|-------|-------------------|----------------|
| KA-01 (JWT signing key) | Keycloak internal keystore | Keycloak realm admin only |
| KA-02 (BCrypt) | PostgreSQL `users.password_hash` column | Application service account (DML only) |
| KA-03 (DB credentials) | Environment variable / Docker Secret | App container only |
| KA-04 (Client secret) | Environment variable / Docker Secret | App container only |
| KA-05 (SFTP key) | File system, `chmod 600` | App container only |
| KA-06 (TLS certs) | Reverse proxy configuration | Infrastructure only |

**Prohibited storage locations:** source code repositories, Docker images (`.dockerignore` must list `.env`), log files, error messages.

---

## 4. Key Distribution

- **KA-01:** Distributed via Keycloak JWKS endpoint (`/realms/arcadehaven/protocol/openid-connect/certs`). Public key only; private key never leaves Keycloak.
- **KA-04:** Distributed to the Spring Boot application as an environment variable at deploy time. Never logged.
- **No key may be shared between more than two entities** (NIST SP 800-57 §5.1 — shared secret key sharing limit).

---

## 5. Key Rotation Schedule

| Asset | Rotation Trigger | Maximum Lifetime |
|-------|-----------------|-----------------|
| KA-01 (JWT signing key) | Compromise, or 1 year | 1 year |
| KA-03 (DB credentials) | Compromise, team member departure, or 90 days | 90 days |
| KA-04 (Client secret) | Compromise, or 90 days | 90 days |
| KA-05 (SFTP key) | Compromise, or 1 year | 1 year |
| KA-06 (TLS certs) | Expiry (Let's Encrypt auto-renew at 60 days) | 90 days |

Rotation procedure:
1. Generate new key using approved CSPRNG
2. Update secret store (Docker Secret / environment variable)
3. Redeploy affected services
4. Verify new key is active
5. Revoke old key

---

## 6. Key Revocation

If a key compromise is suspected:

1. **Immediate:** Revoke the key in its respective system (Keycloak admin → Keys → Delete; rotate Docker Secret)
2. **Session invalidation:** For KA-01 compromise, call `POST /api/auth/logout` for all active users via Keycloak Admin Console
3. **Incident log:** Record the event in the security audit log (`logs/arcadehaven.log`)
4. **Post-mortem:** Document the root cause within 48 hours

---

## 7. Key Expiry

- JWT access tokens: expire per `Access Token Lifespan` in Keycloak (recommended ≤ 15 minutes)
- JWT refresh tokens: expire per `Client Session Idle` / `Client Session Max` (recommended ≤ 7 days)
- Activation keys: do not expire (permanent game access grant); protected by CSPRNG entropy (V11.5.1)

---

## 8. Prohibited Algorithms and Key Sizes

The following **must never be used:**

| Category | Prohibited |
|----------|-----------|
| Hash functions | MD5, SHA-1 (except HIBP k-anonymity protocol, non-cryptographic) |
| Symmetric encryption | DES, 3DES, RC4, AES-ECB mode |
| Asymmetric | RSA < 2048-bit, DSA |
| TLS versions | SSLv2, SSLv3, TLSv1.0, TLSv1.1 |
| Key entropy | < 128 bits for symmetric; UUID-derived keys |

---

## 9. Compliance References

| Reference | Relevance |
|-----------|----------|
| NIST SP 800-57 Part 1 Rev. 5 | Key management lifecycle framework |
| ASVS 5.0 V11.1.1 | Key management policy requirement |
| ASVS 5.0 V11.5.1 | CSPRNG ≥ 128-bit entropy for non-guessable strings |
| ASVS 5.0 V11.4.2 | Password KDF (BCrypt) |
| ASVS 5.0 V12.1.1 | TLS 1.2/1.3 only |
| RNF-23 | JWT signing key as environment variable |
| SC-001 | Key storage security control |

---

## 10. Implementation Evidence

| Asset | Implementation File |
|-------|-------------------|
| Activation key CSPRNG | `Api/src/main/java/.../Domain/OrderItem.java` — `generateActivationKey()` |
| JWT validation | `Api/src/main/java/.../Security/SecurityConfig.java` — Keycloak JWKS |
| BCrypt delegation | `Api/src/main/java/.../Service/AuthService.java` |
| No hardcoded admin credentials | `Api/src/main/java/.../Config/AdminInitializer.java` |
| TLS protocol restriction | `Api/src/main/resources/application.properties` — `server.ssl.enabled-protocols` |
| TLS protocol constants | `Api/src/main/java/.../Security/WebServerTlsConfig.java` |
