# ArcadeHaven — Communication Matrix

Generated: 2026-06-15  
ASVS Reference: **V13.1.1** — All application communication needs documented (external services, user-provided URLs)

This document lists every connection that ArcadeHaven initiates or accepts, including the protocol, authentication method, data sensitivity, TLS posture, and relevant source reference. It is the authoritative communication inventory for security review, network-policy configuration, and ASVS evidence.

---

## Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Fully implemented / in use |
| ⚠️ | Partial / conditional |
| ❌ | Not yet implemented |
| → | Outbound from ArcadeHaven app |
| ← | Inbound to ArcadeHaven app |

---

## 1. External Services (internet-facing)

| # | Service | Direction | Protocol | Port | Authentication | Data Sensitivity | TLS | Notes |
|---|---------|-----------|----------|------|---------------|-----------------|-----|-------|
| E-1 | **PostgreSQL** (remote DB) `vsgate-s1.dei.isep.ipp.pt` | → | JDBC/TCP | 10345 | Username + password (env var) | **Restricted** (all user and order data) | ⚠️ `sslmode=require` pending on remote server TLS enablement — see `application.properties` comment | All PII and transactional data flows through this connection |
| E-2 | **SFTP** (file storage) `vsgate-ssh.dei.isep.ipp.pt` | → | SSH/SFTP | 10408 | Username + password (env var); host-key verification via `OpenSSHKnownHosts` (dev: PromiscuousVerifier with WARN log) | **Confidential** (game cover images and binary files) | ✅ SSH transport (encrypted by design) | Set `SFTP_KNOWN_HOSTS_PATH` for production |
| E-3 | **ClamAV** (malware scanning) `130.61.124.73` | → | CLAMD/TCP | 3310 | None (internal trust boundary) | **Internal** (file content streamed for scanning; not stored by ClamAV) | ❌ CLAMD protocol is plaintext TCP — acceptable only if deployed within a private network segment | File bytes are transmitted but no PII; result is accept/reject |
| E-4 | **HaveIBeenPwned API** `api.pwnedpasswords.com` | → | HTTPS | 443 | None (anonymous k-anonymity) | **Internal** (only 5-char SHA-1 prefix; full hash never sent) | ✅ HTTPS enforced by JVM default SSL context | Fails open (logs WARNING) if unreachable; see `PasswordPolicyService.checkHibp()` |
| E-5 | **RAWG API** *(future)* `api.rawg.io` | → | HTTPS | 443 | API key (to be added as secret) | **Internal** (game metadata fetched from RAWG; no PII sent) | ✅ HTTPS | Not yet integrated (RF-12); when added, `RestTemplate` must inherit `setInstanceFollowRedirects(false)` |

---

## 2. Internal / Docker-network Services

| # | Service | Direction | Protocol | Port | Authentication | Data Sensitivity | TLS | Notes |
|---|---------|-----------|----------|------|---------------|-----------------|-----|-------|
| I-1 | **nginx → Spring Boot app** | ← (inbound to app) | HTTP | 8080 (internal only) | None — nginx handles auth enforcement upstream; app validates JWT | **Confidential** (all API request/response traffic) | ⚠️ HTTP within Docker bridge network (traffic does not leave the host — single-host deployment acceptable). mTLS requires K8s + service mesh for multi-host. | Port 8080 is NOT published externally; nginx is the only upstream |
| I-2 | **Spring Boot app → Keycloak** (token endpoint / admin API) | → | HTTP | 8080 (internal) | Keycloak Admin Client (`client_id` + `client_secret`); user flows use ROPC credentials | **Restricted** (user credentials flow through login; admin credentials for realm management) | ⚠️ HTTP within Docker bridge network — same single-host note as I-1 | See `AuthService`, `ProfileService`, `KeycloakAdminConfig` |
| I-3 | **Keycloak → keycloak-db** | → | JDBC/TCP | 5432 (internal) | Username + password (`KC_DB_USERNAME` / `KC_DB_PASSWORD`) | **Restricted** (Keycloak user store including credential hashes) | ⚠️ HTTP within Docker bridge network | Container-internal only; credentials env-injected |
| I-4 | **Client browsers / Postman → nginx** | ← | HTTPS | 443 | JWT Bearer token (validated by Spring Security) | **Confidential** (all API traffic including tokens) | ✅ TLS 1.2/1.3; ECDHE+AES-GCM ciphers; HTTP port 80 redirects to HTTPS | Self-signed cert for dev/CI; replace with CA cert in production |
| I-5 | **Client browsers / Postman → nginx** | ← | HTTP | 80 | N/A (redirect only) | **Internal** (no sensitive data — immediate 301 to HTTPS) | N/A | nginx returns `301 https://$host$request_uri`; no request body read |

---

## 3. Authentication Methods Summary

| Connection | Method | Secret storage |
|-----------|--------|---------------|
| PostgreSQL (E-1) | Password | `SPRING_DATASOURCE_PASSWORD` env var |
| SFTP (E-2) | Password + host-key verification | `SFTP_PASSWORD` env var; known-hosts file |
| ClamAV (E-3) | None | N/A |
| HIBP (E-4) | None (anonymous) | N/A |
| RAWG (E-5, future) | API key | To be added as `RAWG_API_KEY` secret |
| Keycloak Admin (I-2) | `client_id` + `client_secret` | `KEYCLOAK_BACKEND_CLIENT_SECRET` env var |
| Keycloak DB (I-3) | Password | `KC_DB_PASSWORD` env var |
| Inbound API (I-4) | JWT Bearer | Validated against Keycloak JWKS |

---

## 4. TLS / Encryption Posture Summary

| Connection | Current TLS Status | Required Action |
|-----------|-------------------|----------------|
| Client → nginx (I-4, I-5) | ✅ TLS 1.2/1.3 + ECDHE/GCM | Replace self-signed cert with CA cert in production |
| SFTP (E-2) | ✅ SSH (encrypted by design) | Configure `SFTP_KNOWN_HOSTS_PATH` in production |
| HIBP (E-4) | ✅ HTTPS | None |
| RAWG (E-5, future) | ✅ HTTPS (when implemented) | None |
| PostgreSQL (E-1) | ⚠️ sslmode=require pending | Append `?sslmode=require` to `SPRING_DATASOURCE_URL` once remote DB has TLS |
| App → Keycloak (I-2) | ⚠️ HTTP internal Docker bridge | Acceptable for single-host; add mTLS for multi-host/K8s deployments |
| nginx → App (I-1) | ⚠️ HTTP internal Docker bridge | Same as above |
| ClamAV (E-3) | ❌ Plaintext TCP | Deploy ClamAV on the same private network segment; evaluate TCP-over-TLS tunnel for production |

---

## 5. Source References

| Item | Source File |
|------|------------|
| PostgreSQL connection | [`Api/src/main/resources/application.properties`](../Api/src/main/resources/application.properties) |
| SFTP host-key verification | [`Api/src/main/java/isep/desosfs/arcadehaven/Service/SftpStorageService.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Service/SftpStorageService.java) |
| ClamAV connection | [`Api/src/main/resources/application.properties`](../Api/src/main/resources/application.properties) — `clamav.*` properties |
| HIBP k-anonymity | [`Api/src/main/java/isep/desosfs/arcadehaven/Service/PasswordPolicyService.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Service/PasswordPolicyService.java) |
| Keycloak Admin client | [`Api/src/main/java/isep/desosfs/arcadehaven/Config/KeycloakAdminConfig.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Config/KeycloakAdminConfig.java) |
| nginx TLS config | [`Api/nginx/nginx.conf`](../Api/nginx/nginx.conf) |
| Docker network topology | [`Api/docker-compose.yml`](../Api/docker-compose.yml) |
| RestTemplate redirect policy | [`Api/src/main/java/isep/desosfs/arcadehaven/Config/KeycloakAdminConfig.java`](../Api/src/main/java/isep/desosfs/arcadehaven/Config/KeycloakAdminConfig.java) — `setInstanceFollowRedirects(false)` |
