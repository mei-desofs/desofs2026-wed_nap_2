# ArcadeHaven — Cryptographic Inventory

**ASVS V11.1.2** — Maintained inventory of all cryptographic assets.  
**ASVS V11.2.2** — Crypto agility: procedures for reconfiguring algorithms without redesign.

Generated: 2026-06-13

---

## 1. Cryptographic Asset Inventory

| ID | Asset | Algorithm | Key/Output Size | Usage | Configured In | Status |
|----|-------|-----------|----------------|-------|--------------|--------|
| KA-01 | JWT signing key | RS256 (RSA + SHA-256) | 2048-bit RSA | Stateless session tokens | Keycloak realm settings | ✅ Active |
| KA-02 | Password hashing | BCrypt | Cost factor 12 (≥ 2^12 iterations) | User password storage | Keycloak realm → Password Policy | ✅ Active |
| KA-03 | Activation key generation | CSPRNG (`SecureRandom`) | 128 bits (16 bytes → 32-char hex) | Game activation keys | `Domain/OrderItem.java` | ✅ Active |
| KA-04 | HIBP k-anonymity prefix | SHA-1 (non-cryptographic use) | 160-bit hash; 5-char prefix sent | Breach-password check only | `Service/PasswordPolicyService.java` | ✅ Active (protocol-mandated) |
| KA-05 | Transport encryption | TLS 1.2 / TLS 1.3 | AES-256-GCM (ECDHE key exchange) | Client ↔ API, API ↔ SFTP | `application.properties`, reverse proxy | ✅ Active |
| KA-06 | SFTP host key verification | SSH RSA / ECDSA | ≥ 2048-bit RSA or 256-bit ECDSA | SFTP server identity | `sftp.known-hosts-path` (OpenSSH known_hosts) | ✅ Active (dev: PromiscuousVerifier with WARN) |

---

## 2. Algorithm Justification

### KA-01 — JWT: RS256
- RS256 (RSA-PKCS1v1.5 + SHA-256) issued by Keycloak.
- 2048-bit RSA keys provide ≥ 112-bit security (NIST SP 800-57 Table 2).
- Spring Security (Nimbus JOSE) validates signatures against the Keycloak JWKS endpoint; `alg: none` is rejected.
- **Migration path to RS384/ES256:** See §3.

### KA-02 — Passwords: BCrypt (cost 12)
- BCrypt with work factor 12 satisfies ASVS V11.4.2 (adaptive KDF).
- Cost factor 12 requires ~250 ms per verification on modern hardware — sufficient to resist offline brute-force.
- **Migration path to Argon2id:** See §3.

### KA-03 — Activation Keys: CSPRNG 128 bits
- `java.security.SecureRandom.nextBytes(16)` (NativePRNGNonBlocking on Linux).
- 128-bit entropy exceeds ASVS V11.5.1 minimum.
- Encoded as 32-char uppercase hex for human readability.

### KA-04 — HIBP: SHA-1 (non-cryptographic)
- SHA-1 is used **only** to comply with the HIBP k-anonymity API protocol (mandatory).
- The full hash is never transmitted; only the 5-character prefix is sent.
- This is not a cryptographic protection — it is a privacy-preserving lookup mechanism.
- Suppressed in static analysis: `// codeql[java/use-of-a-potentially-broken-or-risky-cryptographic-algorithm]`

### KA-05 — TLS
- Protocol: TLSv1.2 and TLSv1.3 only (`server.ssl.enabled-protocols`).
- Cipher suites: ECDHE + AES-256-GCM only (`server.ssl.ciphers`).
- Constants defined in `Security/WebServerTlsConfig.java`.

### KA-06 — SFTP
- SSH strict host-key verification via `OpenSSHKnownHosts` when `sftp.known-hosts-path` is set.
- In production, generate with: `ssh-keyscan -p <port> <host> >> known_hosts`

---

## 3. Crypto Agility — Migration Procedures (V11.2.2)

The ArcadeHaven architecture isolates cryptographic decisions to configuration files and single-responsibility classes. No algorithm is embedded in business logic. The table below documents how to swap each algorithm without redesign.

### 3.1 JWT Algorithm: RS256 → ES256 (ECDSA)

**Trigger:** RS256 broken or NIST deprecates 2048-bit RSA.

**Steps:**
1. Keycloak Admin Console → Realm Settings → Keys → Add Provider → `ecdsa-generated` (P-256 or P-384).
2. Deprecate (but do not immediately remove) the RSA key provider — outstanding tokens signed with the old key remain valid until they expire.
3. After all outstanding tokens have expired, remove the RSA key provider.
4. No Spring Boot code change required — Nimbus JOSE negotiates the algorithm from the JWKS endpoint automatically.
5. Update `Documentation/CryptographicInventory.md` (KA-01 row).

### 3.2 Password Hashing: BCrypt → Argon2id

**Trigger:** BCrypt cost factor no longer sufficient; migration to Argon2id required by policy.

**Steps:**
1. Keycloak Admin Console → Realm Settings → Password Policy → replace `Hashing Algorithm: bcrypt` with `Hashing Algorithm: argon2` (Keycloak 21+ native support).
2. Set `Hashing Iterations: 3`, `Parallelism: 1`, `Memory: 65536` (64 MB) per OWASP recommendations.
3. Existing BCrypt hashes are **re-hashed on next login** — Keycloak detects the stale algorithm and upgrades transparently. No forced password reset required.
4. Update `Documentation/CryptographicInventory.md` (KA-02 row).

### 3.3 BCrypt Work Factor Increase

**Trigger:** Hardware speed-up reduces effective cost factor security.

**Steps:**
1. Keycloak Admin Console → Realm Settings → Password Policy → increase `Hashing Iterations`.
2. Existing hashes are upgraded on next login automatically.
3. No Spring Boot code change required.

### 3.4 Activation Key Size Increase (KA-03)

**Trigger:** 128-bit entropy no longer considered sufficient (currently well above minimums).

**Steps:**
1. Edit `Domain/OrderItem.java` → `generateActivationKey()`.
2. Change `new byte[16]` to `new byte[32]` (256 bits).
3. Update `V5__increase_key_length.sql` Flyway migration to widen the `activation_key` column from `VARCHAR(32)` to `VARCHAR(64)`.
4. Existing keys are unaffected; new keys use the longer format.

### 3.5 TLS Protocol / Cipher Suite Change

**Trigger:** TLSv1.2 deprecated; or specific cipher suite broken.

**Steps:**
1. Edit `application.properties`:
   - `server.ssl.enabled-protocols=TLSv1.3` (drop TLSv1.2)
   - `server.ssl.ciphers=TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256` (TLS 1.3 only)
2. Update `Security/WebServerTlsConfig.java` constants to match.
3. Apply equivalent changes at the reverse proxy (`nginx: ssl_protocols TLSv1.3;`).
4. Update `Documentation/CryptographicInventory.md` (KA-05 row).

---

## 4. Prohibited Algorithms

The following are explicitly banned in ArcadeHaven (ASVS V11.4.1, V11.3.1):

| Algorithm | Reason |
|-----------|--------|
| MD5 | Broken — collision attacks trivial |
| SHA-1 | Broken for cryptographic purposes (allowed only for HIBP k-anonymity) |
| DES / 3DES | Insufficient key size; SWEET32 attack |
| RC4 | Statistically weak stream cipher |
| ECB mode | No IV — deterministic; patterns visible in ciphertext |
| PKCS#1 v1.5 RSA encryption | Bleichenbacher padding oracle |
| `alg: none` JWT | Bypasses signature verification |

Constants enforcing TLS prohibition are in `Security/WebServerTlsConfig.FORBIDDEN_CIPHER_SUITES` and `WebServerTlsConfig.FORBIDDEN_TLS_PROTOCOLS`.

---

## 5. Key Management Summary

| Key Asset | Generation | Storage | Rotation | Revocation |
|-----------|-----------|---------|----------|-----------|
| Keycloak JWT signing key | Keycloak auto-generated (RSA-2048) | Keycloak internal keystore | Annually or on compromise | Remove key from Keycloak JWKS; outstanding tokens expire naturally |
| Keycloak Admin client secret | Set at deploy time | `.env` → Docker env var | Rotate via Keycloak Admin Console → Clients → Credentials → Regenerate | Remove old secret; restart `app` container |
| DB password | Set at deploy time | `.env` → Docker env var | Rotate via DB admin + update `.env` | Immediate — old password rejected by PostgreSQL |
| SFTP password/key | Set at deploy time | `.env` → Docker env var | Rotate via SFTP admin + update `.env` | Immediate — old credentials rejected by SSH server |
| CSPRNG seed | JVM-managed (`SecureRandom`) | OS entropy pool | Automatic (seeded on each JVM start) | Not applicable |

Full key management policy: [`Documentation/KeyManagementPolicy.md`](KeyManagementPolicy.md)

---

## 6. Review Schedule

| Review trigger | Action |
|---------------|--------|
| Annual review | Verify all algorithms still meet NIST SP 800-57 recommendations |
| NIST deprecation notice | Begin migration per §3 procedures within 90 days |
| Keycloak major version upgrade | Re-verify algorithm support and defaults |
| Security incident | Immediate rotation of affected keys; incident report |
