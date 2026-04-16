# V11 Cryptography

## 1. Overview & Scope

**Context:** OWASP ASVS v5.0 Level 2 evaluation for ArcadeHaven digital game sales platform.

**Coverage:** 22 ASVS V11 requirements assessed across 7 sub-sections: Cryptographic Inventory, Secure Implementation, Encryption Algorithms, Hashing, Random Values, Public Key Cryptography, and In-Use Data Cryptography.

**Primary Cryptographic Operations in ArcadeHaven:**

| Operation | Mechanism | Requirement | Control |
|-----------|-----------|-------------|---------|
| Password storage | BCrypt (adaptive KDF) | RNF-01 | AC-001 |
| Authentication tokens | JWT with HMAC-SHA256 | RNF-02 | AC-002 |
| Transport encryption | HTTPS / TLS 1.2+ | RNF-05 | AC-005 |
| Activation key generation | UUID (gap — see Section 5) | RNF-08 | SC-003 |

**Primary Mapping Thread:** RNF-01 (Password Storage) → AC-001 (BCrypt Policy) → ST-001 (Hashing Verification)

**Secondary Mapping Threads:**
- RNF-02 (JWT Authentication) → AC-002 (JWT Validation Hardening) → ST-002 (Token Expiration Testing)
- RNF-08 (Activation Key Generation) → SC-003 (Secure Key Entropy) → ST-009 (Entropy Checks)

---

## 2. Requirement-by-Requirement Mapping

### V11.1 Cryptographic Inventory and Documentation (4 requirements)

| V11 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V11.1.1 | Verify that there is a documented policy for management of cryptographic keys and a cryptographic key lifecycle that follows a key management standard such as NIST SP 800-57. This should include ensuring that keys are not overshared. | — | — | — | Not Started |
| V11.1.2 | Verify that a cryptographic inventory is performed, maintained, regularly updated, and includes all cryptographic keys, algorithms, and certificates used by the application. It must also document where keys can and cannot be used in the system. | RNF-01, RNF-02, RNF-08 | AC-001, AC-002, SC-003 | — | Not Started |
| V11.1.3 | Verify that cryptographic discovery mechanisms are employed to identify all instances of cryptography in the system, including encryption, hashing, and signing operations. | — | — | — | Not Started [Level 3] |
| V11.1.4 | Verify that a cryptographic inventory is maintained. This must include a documented plan that outlines the migration path to new cryptographic standards, such as post-quantum cryptography. | — | — | — | Not Started [Level 3] |

**Mapping Rationale:** ArcadeHaven's cryptographic operations are documented implicitly across RNF-01 (BCrypt), RNF-02 (JWT), and RNF-08 (activation keys), but no formal key management policy or cryptographic inventory document exists. V11.1.1 requires a lifecycle policy (rotation schedule, expiry, revocation) for JWT signing keys. V11.1.2 requires a consolidated inventory — currently fragmented across requirements and mitigations documents. V11.1.3 and V11.1.4 are Level 3 and outside the mandatory Level 2 scope.

---

### V11.2 Secure Cryptography Implementation (5 requirements)

| V11 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V11.2.1 | Verify that industry-validated implementations (including libraries and hardware-accelerated implementations) are used for cryptographic operations. | RNF-01, RNF-02 | AC-001, AC-002 | ST-001, ST-002 | In Progress |
| V11.2.2 | Verify that the application is designed with crypto agility such that algorithms, key lengths, rounds, ciphers and modes can be reconfigured, upgraded, or swapped at any time. | RNF-01, RNF-02 | AC-001, AC-002 | — | Not Started |
| V11.2.3 | Verify that all cryptographic primitives utilize a minimum of 128-bits of security based on the algorithm, key size, and configuration. | RNF-01, RNF-02 | AC-001, AC-002 | ST-001, ST-002 | In Progress |
| V11.2.4 | Verify that all cryptographic operations are constant-time, with no short-circuit operations in comparisons, calculations, or returns, to avoid leaking information. | RNF-01, RNF-02 | AC-001, AC-002 | — | In Progress [Level 3] |
| V11.2.5 | Verify that all cryptographic modules fail securely, and errors are handled in a way that does not enable vulnerabilities, such as Padding Oracle attacks. | — | — | — | Not Applicable [Level 3] |

**Mapping Rationale:** V11.2.1 is satisfied by using Spring Security's BCrypt implementation (industry-validated) and a standard JWT library. V11.2.3 is partially satisfied: BCrypt with a work factor of 12+ provides adequate computational security; JWT HS256 uses a 256-bit HMAC key (128-bit security equivalence). V11.2.2 (crypto agility) is not yet designed — algorithm choices are hardcoded in RNF-01 (BCrypt) and RNF-02 (JWT) with no documented upgrade path. V11.2.4 (Level 3) is In Progress: Spring Security's `BCryptPasswordEncoder.matches()` and standard JWT libraries use constant-time comparison by design. V11.2.5 (Level 3) is Not Applicable: Padding Oracle requires application-level CBC-mode decryption, which ArcadeHaven does not perform.

---

### V11.3 Encryption Algorithms (5 requirements)

| V11 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V11.3.1 | Verify that insecure block modes (e.g., ECB) and weak padding schemes (e.g., PKCS#1 v1.5) are not used. | RNF-05 | AC-005 | ST-005 | In Progress |
| V11.3.2 | Verify that only approved ciphers and modes such as AES with GCM are used. | RNF-05 | AC-005 | ST-005 | In Progress |
| V11.3.3 | Verify that encrypted data is protected against unauthorized modification preferably by using an approved authenticated encryption method or by combining an approved encryption method with an approved MAC algorithm. | RNF-02 | AC-002 | ST-002 | In Progress |
| V11.3.4 | Verify that nonces, initialization vectors, and other single-use numbers are not used for more than one encryption key and data-element pair. | — | — | — | Not Started [Level 3] |
| V11.3.5 | Verify that any combination of an encryption algorithm and a MAC algorithm is operating in encrypt-then-MAC mode. | — | — | — | Not Applicable [Level 3] |

**Mapping Rationale:** V11.3.1 and V11.3.2 map to transport-layer encryption via TLS (AC-005, RNF-05). ArcadeHaven does not perform application-level symmetric encryption beyond what TLS provides; all data at rest is either hashed (passwords via BCrypt) or signed (JWT). TLS 1.2+ mandates AES-GCM and prohibits ECB/weak padding. V11.3.3 maps to JWT token integrity: HS256 provides an HMAC over the header and payload, ensuring that any modification to the token claims is detectable. V11.3.4 (Level 3) is outside mandatory scope — IV uniqueness is enforced by TLS and BCrypt. V11.3.5 (Level 3) is Not Applicable: ArcadeHaven uses no custom cipher+MAC combination; JWT uses sign-then-encode and TLS uses AEAD (AES-GCM) internally.

---

### V11.4 Hashing and Hash-based Functions (4 requirements)

| V11 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V11.4.1 | Verify that only approved hash functions are used for general cryptographic use cases. Disallowed hash functions such as MD5 must not be used for any cryptographic purpose. | RNF-01, RNF-02 | AC-001, AC-002 | ST-001, ST-002 | In Progress |
| V11.4.2 | Verify that passwords are stored using an approved, computationally intensive, key derivation function (password hashing function), with parameter settings configured based on current guidance. | RNF-01 | AC-001 | ST-001 | In Progress |
| V11.4.3 | Verify that hash functions used in digital signatures are collision resistant and have appropriate bit-lengths. If collision resistance is required, the output length must be at least 256 bits. | RNF-02 | AC-002 | ST-002 | In Progress |
| V11.4.4 | Verify that the application uses approved key derivation functions with key stretching parameters when deriving secret keys from passwords. | RNF-01 | AC-001 | ST-001 | In Progress |

**Mapping Rationale:** BCrypt is an approved, computationally intensive KDF specifically designed for password hashing (V11.4.2, V11.4.4). Its adaptive cost factor (work factor) provides key stretching that can be increased over time to keep pace with hardware improvements. JWT HS256 uses HMAC-SHA256: SHA-256 produces a 256-bit output, meeting the collision resistance threshold in V11.4.3. No MD5 or SHA-1 is used in any cryptographic context (V11.4.1).

---

### V11.5 Random Values (2 requirements)

| V11 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V11.5.1 | Verify that all random numbers and strings which are intended to be non-guessable must be generated using a cryptographically secure pseudo-random number generator (CSPRNG) and have at least 128 bits of entropy. Note that UUIDs do not respect this condition. | RNF-08 | SC-003 | ST-009 | Not Started |
| V11.5.2 | Verify that the random number generation mechanism in use is designed to work securely, even under heavy demand. | RNF-08 | SC-003 | — | In Progress [Level 3] |

**Mapping Rationale — Critical Gap:**

RNF-08 specifies "Activation keys must be generated securely using UUID or equivalent." V11.5.1 explicitly states that **UUIDs do not respect the CSPRNG 128-bit entropy condition**. UUID v4 is based on random bits from the OS PRNG and its format wastes 6 bits on version and variant markers, reducing effective entropy and making the format predictable in structure. An activation key based on UUID is guessable in principle by an attacker who understands the UUID structure.

The correct approach is to generate activation keys using `SecureRandom` (Java) or an equivalent CSPRNG, producing at least 128 bits of raw random data, then encoding as hex or base64url. This gap must be addressed before Sprint 1 implementation.

**SC-003** (Secure activation key generation entropy) exists in the control catalog but maps to UUID — it must be updated to require CSPRNG explicitly.

---

### V11.6 Public Key Cryptography (2 requirements)

| V11 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V11.6.1 | Verify that only approved cryptographic algorithms and modes of operation are used for key generation and seeding, and digital signature generation and verification. Key generation algorithms must not generate insecure keys vulnerable to known attacks, for example, RSA keys which are vulnerable to Fermat factorization. | RNF-02 | AC-002 | ST-002 | In Progress |
| V11.6.2 | Verify that approved cryptographic algorithms are used for key exchange (such as Diffie-Hellman) with a focus on ensuring that key exchange mechanisms use secure parameters. This will prevent attacks on the key establishment process which could lead to adversary-in-the-middle attacks or cryptographic breaks. | RNF-05 | AC-005 | ST-005 | Not Applicable |

**Mapping Rationale:** V11.6.1 applies to JWT signing key generation. If HS256 is used, the HMAC secret is generated from a CSPRNG and is not subject to the RSA Fermat factorization vulnerability. If RS256 is used, the RSA key pair must be generated via a standard `KeyPairGenerator` from a trusted library with a minimum key size of 3072 bits — standard Java and Spring Security implementations handle this correctly and do not generate weak-prime RSA keys. V11.6.2 addresses key exchange protocols such as Diffie-Hellman. ArcadeHaven performs no application-level key exchange; all key establishment is handled by TLS at the transport layer (AC-005, RNF-05). The TLS stack negotiates cipher suites with ECDHE or DHE, which use approved parameters. This is an infrastructure responsibility, not an application-level concern.

---

### V11.7 In-Use Data Cryptography (2 requirements)

| V11 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V11.7.1 | Verify that full memory encryption is in use that protects sensitive data while it is in use, preventing access by unauthorized users or processes. | — | — | — | Not Applicable [Level 3] |
| V11.7.2 | Verify that data minimization ensures the minimal amount of data is exposed during processing, and ensure that data is encrypted immediately after use or as soon as feasible. | — | — | — | Not Started [Level 3] |

**Mapping Rationale:** Both V11.7 requirements are Level 3 and outside the mandatory Level 2 scope for ArcadeHaven. V11.7.1 (full memory encryption) is Not Applicable: full memory encryption (AMD SME/SEV, Intel TME) is a hardware/OS capability that cannot be controlled from a Java/Spring Boot application or Docker container. V11.7.2 (data minimization in processing) is partially addressed by design: plaintext passwords are passed directly to BCrypt and are not stored anywhere else in the processing chain; JWT tokens are validated and discarded from memory after the request lifecycle. However, no explicit memory zeroing or encrypted memory region is implemented at the application level.

---

## 3. Key Traceability Threads

### Primary Thread: RNF-01 → BCrypt → ST-001

**RNF-01 Definition:** "Passwords must be stored using BCrypt hashing."

**Mapping:**
- **AC-001 (BCrypt Password Hashing Policy):** All user passwords are stored as BCrypt hashes with a configurable work factor. Plaintext passwords are never persisted.
- **V11.4.2:** BCrypt is an approved KDF with computational intensity and adaptive cost factor.
- **V11.4.4:** BCrypt inherently applies key stretching via its Blowfish-based cost rounds.
- **V11.4.1:** BCrypt does not use MD5 or SHA-1; it uses Blowfish-based hashing.

**Test Coverage (ST-001):**
- Code scan confirms no plaintext password storage
- BCrypt hash format verified in database output
- Work factor verified to be within current security guidance (minimum 10, recommended 12)

---

### Secondary Thread: RNF-02 → JWT → ST-002

**RNF-02 Definition:** "Authentication must be performed using JWT with configurable expiration."

**Mapping:**
- **AC-002 (JWT Expiration and Validation Hardening):** Tokens have a configurable TTL; expired tokens are rejected.
- **V11.3.3:** HMAC-SHA256 signature on the JWT header+payload provides integrity and authenticity.
- **V11.4.1:** HMAC-SHA256 is an approved hash function.
- **V11.4.3:** SHA-256 output (256 bits) meets the collision resistance threshold.
- **V11.6.1/6.2:** JWT signing secret must be generated via CSPRNG and stored only in environment variables.

**Test Coverage (ST-002):**
- Expired token rejected with 401
- Tampered payload (signature mismatch) rejected
- Algorithm confusion attack tested (HS256 vs RS256 swap)

---

### Gap Thread: RNF-08 → UUID → V11.5.1 Violation

**RNF-08 Definition:** "Activation keys must be generated securely using UUID or equivalent."

**Gap:** UUID v4, while random, does not meet CSPRNG 128-bit entropy as defined by V11.5.1. UUID structure wastes 6 bits on version/variant markers and its hex-with-dashes format is structurally predictable. An attacker with knowledge that keys are UUID-formatted has a reduced search space.

**Required Fix:** Replace UUID generation with `SecureRandom.generateSeed(16)` or equivalent CSPRNG producing 128 bits of raw entropy, encoded as a 32-character hex string or 22-character base64url string.

**Impact:** SC-003 must be updated. ST-009 must verify CSPRNG usage, not UUID usage.

---

## 4. Documented Gaps

| V11 Item | Gap Description | Status | Rationale |
|----------|---|---|---|
| V11.1.1 | No key management policy for JWT signing keys (rotation schedule, lifecycle, revocation) | Not Started | NIST SP 800-57 compliant policy not yet drafted |
| V11.1.2 | No formal cryptographic inventory document; operations are implicit in RNFs | Not Started | Inventory must be a standalone artifact covering BCrypt, JWT HMAC key, TLS certificates, activation key CSPRNG |
| V11.2.2 | No crypto agility design; BCrypt and HS256 are hardcoded with no upgrade mechanism | Not Started | If BCrypt is deprecated or HS256 is compromised, there is no documented migration path |
| V11.5.1 | Activation keys use UUID; UUID does not satisfy CSPRNG 128-bit entropy requirement (V11.5.1 explicitly) | Not Started | RNF-08 and SC-003 must be updated to require SecureRandom-based generation |

---

## 5. Evidence & References

| Document | Reference | Evidence |
|----------|-----------|----------|
| requirements.md | RNF-01 | "Passwords must be stored using BCrypt hashing" |
| requirements.md | RNF-02 | "Authentication must be performed using JWT with configurable expiration" |
| requirements.md | RNF-05 | "All communications must use HTTPS exclusively" |
| requirements.md | RNF-08 | "Activation keys must be generated securely using UUID or equivalent" — gap vs V11.5.1 |
| requirements.md | RNF-23 | "Sensitive configurations must be injected via environment variables and never hard-coded" |
| mitigations.md | AC-001 | BCrypt password hashing policy |
| mitigations.md | AC-002 | JWT expiration and validation hardening |
| mitigations.md | AC-005 | HTTPS-only communication |
| mitigations.md | SC-001 | Secrets only in environment variables |
| mitigations.md | SC-002 | No sensitive values in logs |
| mitigations.md | SC-003 | Secure activation key generation entropy — must be updated from UUID to CSPRNG |
| securityTesting.md | ST-001 | Password hashing verification |
| securityTesting.md | ST-002 | JWT expiration and invalid token rejection |
| securityTesting.md | ST-005 | HTTPS-only communication enforcement |
| securityTesting.md | ST-009 | Entropy and uniqueness checks for activation keys |
| securityTesting.md | ST-016 | Configuration review — verify JWT secret not exposed |

---

## Summary

V11 Cryptography is partially applicable to ArcadeHaven. The platform's cryptographic footprint is intentionally narrow: BCrypt for password hashing, JWT with HMAC-SHA256 for authentication tokens, HTTPS/TLS for transport, and activation key generation. This limited scope means many V11 requirements are satisfied by platform-level TLS or by the design choice to not perform additional application-level encryption.

The most significant gap is **V11.5.1**: activation keys specified in RNF-08 as UUID-based do not meet the CSPRNG 128-bit entropy requirement. SC-003 must be updated to require `SecureRandom` or equivalent. A second gap is the absence of a formal **cryptographic inventory** (V11.1.2) and **key management policy** (V11.1.1) as standalone documents.

Among the Level 3 requirements: V11.2.5, V11.3.5, and V11.7.1 are Not Applicable (Padding Oracle requires CBC decryption; encrypt-then-MAC requires a custom cipher+MAC not used here; full memory encryption is a hardware/OS concern). V11.2.4 and V11.5.2 are In Progress: Spring Security and Java SecureRandom already implement constant-time operations and non-blocking CSPRNG behavior respectively. V11.6.2 is Not Applicable since key exchange is handled entirely by TLS. V11.1.3, V11.1.4, V11.3.4, and V11.7.2 remain Not Started (outside mandatory Level 2 scope).

**Status:** Partially compliant — gaps in V11.1.1, V11.1.2, V11.2.2, and V11.5.1 require action before implementation.
