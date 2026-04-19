# Threat Identification and Analysis

**Project:** ArcadeHaven — DESOFS 2026  
**Document:** [ArcadeHaven-DFD](../../Architecture/Dataflow/arcadehaven-dfd.pdf)  
**Methodology:** STRIDE-per-Element applied to all DFD levels  

> **Note:**  
> This document provides a **summarized view** of the identified threats.  
> For a **more detailed and enriched version** — including full **descriptions** and **mitigation strategies** — please refer to the **report generated in OWASP Threat Dragon**, where all threats are documented in depth.

---

## 1. Introduction

This document presents a **high-level overview** of the threat identification and analysis for the *ArcadeHaven* digital game distribution platform.

Threats were identified by applying the **STRIDE-per-Element** methodology to every element present in the Data Flow Diagrams (DFDs) produced in OWASP Threat Dragon, from the system context (Level 0) down to the Game Management sub-processes (Level 2).

The goal is to highlight the **most relevant security risks**, while keeping this document concise and easy to navigate.

For each element, the analysis identifies: 
- Applicable STRIDE threat categories based on element type 
- Specific threat scenarios with concrete attack vectors 
- Threat agents and their capabilities
- Abuse cases illustrating how each threat could be exploited
- Proposed mitigations and security controls

For full details consult the [Threat Dragon report](../../Architecture/Dataflow/arcadehaven-dfd.pdf) .

---

## 2. STRIDE-per-Element Mapping


The STRIDE-per-Element methodology assigns applicable threat categories based on the type of each DFD element:

| Element Type | S | T | R | I | D | E |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| **External Entity** | ✓ | | ✓ | | | |
| **Process** | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| **Data Store** | | ✓ | ✓ | ✓ | ✓ | |
| **Data Flow** | | ✓ | | ✓ | ✓ | |

**Legend:** S = Spoofing · T = Tampering · R = Repudiation · I = Information Disclosure · D = Denial of Service · E = Elevation of Privilege

---

## 3. Threat Agent Catalogue

Before presenting threats per element, the following threat agents are referenced throughout this document:

| ID | Threat Agent | Description | Capability |
|----|-------------|-------------|------------|
| TA-01 | **External Attacker** | Unauthenticated actor attempting to compromise the system from the internet | Low to High depending on tooling |
| TA-02 | **Malicious Buyer** | Authenticated Buyer attempting to access resources beyond their entitlement | Low — uses platform features maliciously |
| TA-03 | **Malicious Publisher** | Authenticated Publisher attempting to bypass approval, manipulate listings, or steal data | Medium — has upload and API access |
| TA-04 | **Compromised Admin** | Admin account taken over via credential theft or social engineering | High — full platform access |
| TA-05 | **Malicious Insider** | Developer or DBA with direct DB or infrastructure access | High — bypasses application layer |
| TA-06 | **Supply Chain Attacker** | Actor who compromises a third-party dependency (RAWG API, Keycloak, libraries) | Medium to High |
| TA-07 | **Automated Bot** | Scripted tool performing high-volume requests for enumeration, flooding, or credential stuffing | Medium |

---

## 4. DFD Level 0 — System Context

### 4.1 External Entities

#### ENTITY: System Users (User / Publisher / Admin)

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L0-E-01 | Spoofing | Identity impersonation at system boundary | TA-01, TA-07 | Credential stuffing using leaked passwords from other breaches; forged JWT with manipulated sub claim | Attacker logs in as a Publisher and submits malicious game files under a legitimate publisher's identity | Keycloak MFA; short-lived JWTs (≤15 min); breach-password detection at registration |
| L0-E-02 | Repudiation | Denial of performed system actions | TA-02, TA-03, TA-04 | Absence of cross-cutting audit log at the system boundary | Admin performs bulk user deletion and later denies the action; no log entry exists to refute the claim | Immutable audit log for all authenticated actions; log entries include user ID, timestamp, action, and resource ID |

#### ENTITY: Auth API (Keycloak — external boundary)

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L0-E-03 | Spoofing | Rogue Auth API impersonation | TA-06 | Misconfigured issuer URL pointing ArcadeHaven to a malicious OIDC server that issues tokens for any identity | Spring Security accepts tokens from an attacker-controlled issuer; any identity claim is trusted | Hardcode trusted issuer URI in Spring Security config; validate iss, aud, and exp on every token |
| L0-E-04 | Repudiation | Auth events not correlated with application context | TA-04, TA-05 | Keycloak logs authentication events but they are not linked to ArcadeHaven application-level audit logs | Compromised admin account performs bulk deletions; Keycloak login is logged but not correlated to the ArcadeHaven actions | Propagate Keycloak session ID (sid claim) through all ArcadeHaven audit log entries |

#### ENTITY: RAWG API (external game data service)

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L0-E-05 | Spoofing | Spoofed RAWG endpoint via DNS poisoning | TA-06 | DNS cache poisoning redirects api.rawg.io to an attacker-controlled server | Malicious RAWG server returns crafted game descriptions containing XSS payloads that are stored in the database | Pin RAWG base URL; enforce TLS certificate validation; treat all RAWG data as untrusted |
| L0-E-06 | Repudiation | No record of consumed external data version | TA-06 | RAWG silently changes metadata; no snapshot of what was ingested exists | RAWG changes an age rating; ArcadeHaven cannot prove what rating was displayed at the time of a disputed sale | Log raw RAWG responses with timestamp and request hash before processing |

---

### 4.2 Processes

#### PROCESS: ArcadeHaven System (Level 0 black-box view)

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L0-P-01 | Spoofing | JWT not validated on every route | TA-01, TA-02 | Request with expired or unsigned token to an endpoint missing Spring Security filter | Attacker accesses protected resources without valid authentication | Global JWT validation filter; alg:none attack prevention |
| L0-P-02 | Tampering | Request body manipulation | TA-02 | HTTP request body modified before processing (MitM or direct API call) | Buyer submits order with price: 0.01 for a €59.99 game | Server-side price resolution; strict DTO validation |
| L0-P-03 | Repudiation | Insufficient structured logging | TA-03, TA-04 | Absence of structured audit log for mutating operations | Admin deletes user account; no record exists linking the admin's identity to the deletion | Structured access log for all POST/PUT/PATCH/DELETE operations |
| L0-P-04 | Information Disclosure | Verbose error messages exposing internals | TA-01 | Triggering 500 errors to read stack traces | Stack trace reveals table names, ORM query structure, and internal hostname | Global exception handler; suppress stack traces in production |
| L0-P-05 | Denial of Service | No rate limiting on public endpoints | TA-07 | HTTP flood against order creation or authentication endpoints | Thread pool exhaustion causing API unavailability for all users | API gateway rate limiting; Bucket4j per-IP and per-user limits |
| L0-P-06 | Elevation of Privilege | Missing role checks on privileged routes | TA-02, TA-03 | Direct call to admin endpoint with a Buyer or Publisher JWT | Buyer calls DELETE /admin/games/{id} and succeeds due to missing @PreAuthorize | Method-level @PreAuthorize; integration tests for all protected endpoints |

---

### 4.3 Data Stores

#### STORE: Database (Level 0 aggregate)

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L0-S-01 | Tampering | Unauthorised direct DB writes bypassing application logic | TA-05 | DBA executes UPDATE orders SET price = 0.01 directly in psql | All orders modified to a near-zero price; financial records falsified | Application DB user has no DDL rights; all writes via application layer only |
| L0-S-02 | Repudiation | No DB-level change attribution | TA-05 | pgaudit disabled; row modified directly with no audit record | Record in orders modified; no before/after log to prove what changed or who changed it | Enable pgaudit; Flyway for all schema changes; append-only audit tables |
| L0-S-03 | Information Disclosure | Unencrypted sensitive data at rest | TA-05, TA-01 | PostgreSQL data directory on unencrypted volume; cloud storage misconfiguration | Disk snapshot or storage leak exposes all user PII, activation keys, and order history | Encrypted volumes; application-layer encryption for activation keys; bcrypt passwords via Keycloak |
| L0-S-04 | Denial of Service | Unbounded queries exhausting connections | TA-02, TA-07 | SELECT * FROM games with no LIMIT loads entire catalogue into memory | JVM OutOfMemoryError crashes the application; all users affected | Mandatory pagination; query timeouts in HikariCP; indices on filter columns |

#### STORE: File System

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L0-S-05 | Tampering | Path traversal on file write | TA-03 | Client-supplied filename ../../etc/cron.d/shell used in write path | Malicious file written outside the upload directory; potential remote code execution | UUID filenames generated server-side; canonical path validation before any write |
| L0-S-06 | Repudiation | No file access/modification audit trail | TA-05 | Invoice PDF deleted with no OS or application log of who triggered the deletion | Deletion cannot be attributed; financial audit requirement violated | Application-layer file operation logging; OS auditd for sensitive directories |
| L0-S-07 | Information Disclosure | Sensitive files served without auth check | TA-01, TA-02 | Direct URL to /uploads/invoice-uuid.pdf accessible without authentication | Any user who guesses a UUID can download another user's invoice or activation key file | Store sensitive files outside web root; authenticated endpoints with ownership check |
| L0-S-08 | Denial of Service | Disk exhaustion via upload abuse | TA-03, TA-07 | Hundreds of large files uploaded using a stolen Publisher JWT | Disk partition fills; invoice generation fails; order flow broken | Per-user upload quotas; disk usage alerts; separate partition for uploads |

---

### 4.4 Data Flows

#### FLOW: Request / Response (User ↔ ArcadeHaven System)

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L0-F-01 | Tampering | MitM — request body modification | TA-01, TA-06 | HTTP (non-TLS) order request intercepted; price field modified in transit | Attacker on network path changes game price before it reaches the server | TLS 1.2+ enforced; HSTS header; HTTP listener disabled in production |
| L0-F-02 | Information Disclosure | Sensitive data transmitted in cleartext | TA-01 | Activation keys and PII returned in HTTP responses without TLS | Network observer reads all user data including activation keys and invoices | TLS everywhere; Cache-Control: no-store on sensitive endpoints |
| L0-F-03 | Denial of Service | HTTP flood / slow-loris attack | TA-07 | Thousands of slow connections exhaust Spring Boot thread pool | All legitimate API requests time out; platform unavailable | Nginx reverse proxy with connection/rate limits; WAF; request timeout configuration |

#### FLOW: Authentication Request / Response (System ↔ Auth API)

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L0-F-04 | Tampering | JWT payload tampered in transit | TA-01 | Weak HS256 key; attacker re-signs modified JWT with elevated role claim | Attacker gains ADMIN role without Keycloak change | RS256 asymmetric signing; validate against JWKS endpoint; reject alg:none tokens |
| L0-F-05 | Information Disclosure | Token interception via URL parameter | TA-01 | Bearer token passed as query parameter; appears in Nginx logs and browser history | Attacker with log access impersonates any user whose token was logged | Tokens only in Authorization header; short expiry; purge from all logs |
| L0-F-06 | Denial of Service | JWKS endpoint unavailability cascades to full API outage | TA-07 | Keycloak outage causes all JWT validation to fail | Entire ArcadeHaven API becomes unavailable even for unauthenticated endpoints | Cache JWKS public key locally with TTL; Keycloak HA deployment; circuit breaker |

---

## 5. DFD Level 1 — ArcadeHaven System

### 5.1 Processes

#### PROCESS: ArcadeHaven REST API (Central Gateway)

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1A-P-01 | Spoofing | JWT accepted from untrusted issuers | TA-01 | Spring Security not configured with a specific issuer URI | Self-issued token from attacker's OIDC server accepted as valid | Whitelist exactly one issuer URI in spring.security.oauth2.resourceserver.jwt.issuer-uri |
| L1A-P-02 | Tampering | Mass assignment via uncontrolled DTO mapping | TA-02, TA-03 | JSON body contains fields that should not be writable by the caller (price, status, role) | Publisher submits game with status: ACTIVE, bypassing approval workflow | Explicit DTOs per role; no mass assignment; @JsonIgnore on restricted fields |
| L1A-P-03 | Repudiation | Mutating operations not logged with actor context | TA-03, TA-04 | Spring logs requests without authenticated user identity | Admin action performed; log shows HTTP 200 but no user identity | Structured access log including JWT sub on all mutating operations |
| L1A-P-04 | Information Disclosure | Stack traces and ORM details in error responses | TA-01 | Deliberately malformed requests trigger 500 errors | Internal table names, query structure, and hostname revealed to attacker | Global @ControllerAdvice exception handler; generic error responses in production |
| L1A-P-05 | Denial of Service | No rate limiting at gateway level | TA-07 | High-volume requests to any endpoint | Thread pool exhaustion; DB connection pool starvation | Bucket4j rate-limiting filter; per-IP and per-user quotas; 429 with Retry-After |
| L1A-P-06 | Elevation of Privilege | Role not validated at method level | TA-02, TA-03 | Valid JWT with BUYER role sent to ADMIN-only endpoint | Buyer deletes game listings or approves pending games | @PreAuthorize("hasRole('ADMIN')") at method level; automated security tests per endpoint |

#### PROCESS: User Authentication (Spring Security / Keycloak Integration)

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1A-P-07 | Spoofing | Token accepted from wrong issuer due to misconfiguration | TA-01 | spring.security.oauth2.resourceserver.jwt.issuer-uri not set | Any OIDC-compliant token from any server accepted | Enforce single trusted issuer URI in configuration |
| L1A-P-08 | Tampering | Stale role claims used after role revocation | TA-02 | User's Keycloak role downgraded but existing JWT (valid 15 more minutes) still grants old role | Revoked PUBLISHER continues creating games during token lifetime | Short TTL (≤5 min); Keycloak back-channel logout; token revocation list |
| L1A-P-09 | Repudiation | Auth failures not logged | TA-01, TA-07 | Spring Security rejects request; no failure log with IP and resource | Brute force attempts are invisible to incident response | Custom AuthenticationFailureHandler logging all rejections with user ID, resource, timestamp |
| L1A-P-10 | Information Disclosure | JWT logged at DEBUG level | TA-05 | DEBUG logging enabled in production | Full JWT in application log; developer impersonates any logged user | Never log Authorization headers; structured logging with field allowlist |
| L1A-P-11 | Denial of Service | JWKS fetched on every request | TA-07 | Keycloak slowdown causes per-request JWKS fetch to hang | All API threads blocked on JWKS fetch; full API unavailability | Cache JWKS keys locally with TTL; background refresh; circuit breaker |
| L1A-P-12 | Elevation of Privilege | Roles extracted from wrong JWT claim path | TA-01 | Custom claim trusted alongside canonical realm_access.roles | Attacker crafts token with ADMIN in a custom claim that is mistakenly trusted | Explicitly configure single authoritative JWT claim path for role extraction |

#### PROCESS: Game Management Module

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1A-P-13 | Spoofing | Publisher ID sourced from request body | TA-03 | POST /games with publisherId: 99 in body | Game created under a different publisher's identity | Publisher ID bound exclusively from JWT sub; never from request body |
| L1A-P-14 | Tampering | Malicious file upload bypassing MIME check | TA-03 | PHP webshell uploaded with spoofed Content-Type: image/jpeg | Executable file stored and served to buyers; potential RCE | Magic-byte validation; ClamAV scan; UUID filename rename; block executable MIME types |
| L1A-P-15 | Repudiation | Game state transitions not logged | TA-04 | Admin approves game with no log of who approved it or when | Compliance audit cannot attribute the approval decision | Audit log every status change with actor ID, timestamp, old/new state |
| L1A-P-16 | Information Disclosure | Stored XSS via unsanitised RAWG metadata | TA-06 | RAWG description containing script tag stored and rendered as HTML | Buyers' session tokens stolen; full XSS affecting all game page visitors | OWASP Java HTML Sanitizer before storage; output encoding at render; CSP header |
| L1A-P-17 | Denial of Service | Large file upload exhausting disk | TA-03, TA-07 | 10 GB "screenshot" uploaded using a valid Publisher JWT | Disk full; invoice generation fails; order flow broken | Max file size at gateway and handler; per-publisher upload quota; disk alerts |
| L1A-P-18 | Elevation of Privilege | Publisher can self-approve games | TA-03 | PATCH /games/{id}/approve called with Publisher JWT | Publisher's game goes ACTIVE without admin review | Approval endpoint requires ADMIN role; Publisher token must return 403 |

#### PROCESS: Order Management Module

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1A-P-19 | Spoofing | Order placed on behalf of another user | TA-01 | Stolen JWT used to create order; activation key delivered to attacker's library | Victim charged; attacker receives game access | buyer_id bound from JWT sub server-side; never from request body |
| L1A-P-20 | Tampering | Client-supplied price written to order | TA-02 | POST /orders with price: 0.01 in body; server uses this value | Game purchased for near-zero price | Price always resolved server-side from Game Datastore; price field excluded from order creation DTO |
| L1A-P-21 | Repudiation | No non-repudiable purchase record | TA-02 | Buyer disputes purchase claiming "I never bought that" | Without a signed record, the platform cannot prove the authenticated session made the purchase | Immutable order record with buyer ID, timestamp, game ID, price-at-purchase, invoice hash |
| L1A-P-22 | Information Disclosure | Activation key returned in cacheable response | TA-01 | GET /orders/{id} cached by CDN; activation key in cached JSON | Stale cache entry delivers another user's activation key | Cache-Control: no-store; one-time retrieval pattern; keys encrypted at rest |
| L1A-P-23 | Denial of Service | Order and PDF generation flooding | TA-07 | Hundreds of simultaneous order completions triggering synchronous PDF generation | OOM crash; all API threads blocked on PDF generation | Async PDF generation via message queue; per-user order rate limiting |
| L1A-P-24 | Elevation of Privilege | IDOR on order history endpoint | TA-02 | GET /orders?userId=2 returns victim's orders because filter uses query param | Buyer reads another user's full purchase history and activation keys | Filter all order queries by JWT sub; IDOR automated tests in CI |

#### PROCESS: Library Management Module

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1A-P-25 | Spoofing | IDOR on library read | TA-02 | GET /library/{userId} with victim's ID | Buyer reads another user's full game library and activation keys | Library access strictly scoped to JWT sub |
| L1A-P-26 | Tampering | Library entry injected without purchase | TA-04 | POST /library/{victimId}/entries to grant game without completed order | User gains access to any game without payment | Library writes only via order completion event; FK constraint to orders table |
| L1A-P-27 | Repudiation | Admin library modifications not logged | TA-04 | Admin removes game from user's library after refund; no audit record | Removal cannot be attributed; user disputes access loss | Audit log all library mutations with actor, target user, game ID, and reason |
| L1A-P-28 | Information Disclosure | Cross-user library data due to missing WHERE clause | TA-02 | ORM query missing user_id filter returns all users' library data | Buyer A reads Buyer B's entire library including activation keys | JPA repository methods always include userId from JWT; integration tests for cross-user isolation |
| L1A-P-29 | Denial of Service | Unbounded library query | TA-02 | GET /library with no pagination on a large library | OOM loading thousands of library entries | Mandatory pagination; max page size enforced; lazy-load JPA relationships |
| L1A-P-30 | Elevation of Privilege | Cross-user library entry modification | TA-02 | PATCH /library/{otherId}/entries/{gameId} succeeds for Buyer | Buyer modifies another user's library state | Ownership check: JWT sub must match library owner |

#### PROCESS: User Information Management Module

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1A-P-31 | Spoofing | Email change enabling account takeover | TA-02 | PATCH /users/{id} changes email to admin's address | Password reset link goes to attacker's inbox; full admin account takeover | Re-authentication required for email change; email verification before commit; notify old address |
| L1A-P-32 | Tampering | Role field writable from client payload | TA-02 | PATCH /users/{id} with role: ADMIN in body | User self-assigns Admin role | Role field absent from UserUpdateDTO; roles managed exclusively in Keycloak |
| L1A-P-33 | Repudiation | Profile changes not logged | TA-02 | User changes email; later disputes a charge claiming wrong email | No log shows the user changed their email; GDPR audit fails | Audit log all profile mutations with old/new values and timestamp |
| L1A-P-34 | Information Disclosure | Full user object returned to any caller | TA-02 | GET /users/{id} returns hashed password and internal flags to any authenticated caller | Buyer harvests PII of all users by enumerating IDs | Dedicated response DTOs; ownership check before returning data; IDOR prevention |
| L1A-P-35 | Denial of Service | User enumeration via timing attack | TA-07 | Login response time differs for existing vs non-existing usernames | Attacker enumerates valid accounts for targeted credential stuffing | Constant-time comparison in auth flows; uniform error messages |
| L1A-P-36 | Elevation of Privilege | Buyer modifies another user's profile | TA-02 | PATCH /users/{adminId}/profile accepted from Buyer | Buyer modifies admin's display name or email | Ownership verification: JWT sub must match target user ID or caller must hold ADMIN role |

---

## 6. DFD Level 1 — Auth API

### 6.1 Processes

#### PROCESS: User Management (Keycloak Realm Admin)

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1B-P-01 | Spoofing | Keycloak admin console exposed publicly | TA-01 | Brute force or CVE exploit against publicly reachable /auth/admin console | Attacker gains full realm control; creates ADMIN users; reads all PII | Restrict admin console to internal network/VPN; change default credentials; MFA on admin account |
| L1B-P-02 | Tampering | Service account with excessive Keycloak roles | TA-01, TA-05 | ArcadeHaven service account holds realm-admin role; application compromised | Attacker inherits full realm control through compromised application | Service account uses only minimum scopes; never realm-admin; regular permission audits |
| L1B-P-03 | Repudiation | No Keycloak admin event logging | TA-04 | Admin creates PUBLISHER user; no event log kept | Role assignment cannot be audited; privilege escalation invisible | Enable Keycloak Admin Events; forward to SIEM; retain 90+ days |
| L1B-P-04 | Information Disclosure | ArcadeHaven service account has manage-users scope | TA-01 | Application vulnerability proxies calls to /auth/admin/users | All user emails and profile data enumerated via application vulnerability | Grant service account only minimum scopes (view-profile); never manage-users |
| L1B-P-05 | Denial of Service | Mass user creation flooding Keycloak DB | TA-07 | Open registration endpoint exploited to create millions of accounts | Keycloak internal DB overwhelmed; authentication slow or unavailable for all users | CAPTCHA and rate limiting on registration; email verification before activation |
| L1B-P-06 | Elevation of Privilege | Default realm role too permissive | TA-01 | Every new registered user automatically gets PUBLISHER role | Any registered user can create and publish games | Default realm role set to BUYER; PUBLISHER and ADMIN explicitly assigned by admin only |

#### PROCESS: Credential Validation

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1B-P-07 | Spoofing | Credential stuffing / brute force | TA-07 | Leaked credential list from other breaches tested against Keycloak | Accounts compromised at scale | Keycloak brute-force protection; MFA; HaveIBeenPwned check at registration |
| L1B-P-08 | Tampering | Password hash algorithm downgrade | TA-05 | Keycloak misconfigured to use MD5 instead of bcrypt/Argon2 | DB leak leads to rapid hash cracking; all user passwords compromised | Verify Keycloak uses bcrypt or Argon2; include in deployment checklist |
| L1B-P-09 | Repudiation | No failed-login event record | TA-07 | Credential stuffing attack runs for hours without being recorded | Attack invisible to incident response | Enable Keycloak login event persistence; alert on N failures per account per hour |
| L1B-P-10 | Information Disclosure | User enumeration via differential responses | TA-07 | "User not found" vs "wrong password" responses allow account enumeration | Attacker builds list of valid accounts for targeted attacks | Keycloak returns generic "invalid credentials" for all failures; constant-time comparison |
| L1B-P-11 | Denial of Service | Validation flood causing DB lock contention | TA-07 | High-volume brute force creates heavy DB write load (failed attempt counters) | Lock contention slows all legitimate logins | Rate-limit at reverse proxy; Keycloak HA; async failed-attempt counter update |
| L1B-P-12 | Elevation of Privilege | MFA bypassed via account recovery flow | TA-01 | Forgot-password flow resets credentials without triggering MFA | MFA protection rendered ineffective | Ensure recovery requires email verification; alert on password reset for MFA-enabled accounts |

#### PROCESS: Issue Token

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1B-P-13 | Spoofing | Token issued to wrong client (audience confusion) | TA-01 | Frontend token replayed against backend because aud claim not validated | Attacker uses a token obtained from one client to access another | Validate aud claim strictly; each client has its own client ID |
| L1B-P-14 | Tampering | Weak signing key allows token forgery | TA-01 | HS256 with guessable secret brute-forced offline | Attacker forges tokens with arbitrary claims including ADMIN role | RS256/ES256 asymmetric signing; minimum 2048-bit RSA key; annual key rotation |
| L1B-P-15 | Repudiation | No token issuance event log | TA-04 | Token issued to a compromised device; no event recorded | Cannot determine when compromise began or which sessions were affected | Enable Keycloak token event logging; log client ID, user ID, IP, and timestamp |
| L1B-P-16 | Information Disclosure | Excessive claims embedded in token payload | TA-01 | Token contains email, phone, address; any service receiving the token reads this PII | PII leaked to all downstream services that receive the JWT | Minimise claims in token payload; use userinfo endpoint for additional attributes |
| L1B-P-17 | Denial of Service | Token endpoint flooded | TA-07 | Flood of invalid grant_type requests; each hits DB for client config lookup | DB connection pool exhausted; legitimate logins fail | Rate-limit at reverse proxy/WAF; Keycloak cluster with dedicated DB pool |
| L1B-P-18 | Elevation of Privilege | Long-lived access tokens granting stale privileges | TA-02 | Role revoked in Keycloak but 8-hour token remains valid | Revoked user continues to exercise old privileges for hours | Short access token TTL (5–15 min); token revocation list; refresh token rotation |

#### PROCESS: Token Validation

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1B-P-19 | Spoofing | Algorithm confusion attack (alg:none) | TA-01 | JWT with alg: none in header; validator skips signature verification | Attacker creates a valid-looking JWT for any user without a signing key | Whitelist RS256 only; reject alg:none; use Nimbus JOSE+JWT library |
| L1B-P-20 | Tampering | Expired token accepted due to excessive clock skew | TA-01 | Validator allows 1-hour clock skew; attacker replays expired token | Token expired 45 minutes ago still accepted as valid | Max clock skew ≤ 30 seconds; NTP synchronisation on all servers |
| L1B-P-21 | Repudiation | Token validation failures not logged | TA-01, TA-07 | Flood of invalid tokens sent to probe the system; no failure log | Intrusion attempt invisible; cannot investigate after the fact | Log all validation failures with reason, source IP, and timestamp; alert on anomalous rates |
| L1B-P-22 | Information Disclosure | Validation errors reveal internal configuration | TA-01 | Error message "Expected issuer: https://keycloak.internal:8080/realms/arcadehaven" | Internal Keycloak hostname and realm name disclosed to attacker | Return generic 401 Unauthorized; log details internally only |
| L1B-P-23 | Denial of Service | Malformed tokens causing crypto overhead | TA-07 | Thousands of malformed JWTs requiring complex cryptographic operations | CPU exhaustion from repeated signature verification attempts | Pre-validate token structure before crypto; rate-limit per IP before validation |
| L1B-P-24 | Elevation of Privilege | jku header injection — attacker-controlled key URL | TA-01 | JWT header contains jku pointing to attacker's JWKS server; validator fetches and trusts it | Attacker's forged tokens accepted as valid; full authentication bypass | Never follow jku or x5u headers; only use pre-configured JWKS endpoint |

#### PROCESS: Claim Role

| # | STRIDE | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|--------|-------------|---------------|-----------|-----------|
| L1B-P-25 | Spoofing | Role extracted from attacker-controlled JWT claim path | TA-01 | Spring Security reads roles from a custom claim path alongside canonical realm_access.roles | ADMIN injected via a custom claim field | Explicitly configure the single authoritative claim path; discard all other potential role claims |
| L1B-P-26 | Tampering | Cached role claims used beyond token lifetime | TA-02 | Application caches decoded role claims beyond token expiry | Attacker retains revoked role through stale cache | Re-extract roles from JWT on every request; rely on Spring Security per-request SecurityContext |
| L1B-P-27 | Repudiation | Role not recorded in audit log entries | TA-04 | ADMIN operation performed; log records user ID but not which role authorised it | Privilege reviews cannot determine which operations required which role | Include effective role from JWT in every audit log entry for privileged operations |
| L1B-P-28 | Information Disclosure | Role names leaked in 403 response body | TA-02 | 403 response states "requires role ADMIN" | Attacker learns exact role name to target for privilege escalation | 403 returns only "Forbidden"; role detail logged internally only |
| L1B-P-29 | Denial of Service | Complex composite role resolution causing latency spikes | TA-07 | Deeply nested Keycloak composite roles require multiple DB lookups per request | Authorisation latency spikes under load; API degradation | Keep role hierarchy flat (3 standalone roles); role resolution O(1) from JWT claims |
| L1B-P-30 | Elevation of Privilege | Default realm role is PUBLISHER | TA-01 | Every registered user automatically gets Publisher capabilities | Any attacker who registers can create and publish games | Default role set to BUYER; PUBLISHER assigned explicitly; verify in Keycloak config |

### 6.2 Data Flows — Auth API Internal

#### FLOW: Authentication Request → Credential Validation

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1B-F-01 | Tampering | Critical | Credential interception and modification in transit | TA-06, TA-05 | Unencrypted internal network communication between Keycloak components | Attacker on same network intercepts credentials and substitutes a different username | TLS on all Keycloak internal communication; Docker network isolation |
| L1B-F-02 | Information Disclosure | Critical | Plaintext credentials in Keycloak request logs | TA-05 | DEBUG logging enabled; full POST /token body logged including password | Plaintext passwords of all users written to log store | Disable DEBUG in production; mask password fields in log formatters |
| L1B-F-03 | Denial of Service | High | Authentication request flood causing DB lock contention | TA-07 | High-volume brute force creates bcrypt and DB load simultaneously | Login failures for all legitimate users during attack | Rate-limit at reverse proxy; async bcrypt comparison; Keycloak HA |

#### FLOW: Token Request / Response (Credential Validation ↔ Issue Token)

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1B-F-04 | Tampering | Critical | Token payload tampered before signing — role claim injection | TA-05, TA-06 | Custom Keycloak SPI communicates over unencrypted channel; role claim injected pre-signing | Legitimately signed token with fraudulent ADMIN role | Verify Keycloak token mapper is in-process; audit all custom SPIs |
| L1B-F-05 | Tampering | Critical | Issued token intercepted before delivery — token theft | TA-01 | HTTP (non-TLS) token delivery; on-path attacker captures access and refresh tokens | Attacker authenticates as victim indefinitely | HTTPS for all token delivery; refresh token in HttpOnly Secure cookie; token rotation |
| L1B-F-06 | Information Disclosure | High | Token response cached by proxy — cross-user token leakage | TA-01 | Misconfigured CDN or reverse proxy caches token endpoint response | Subsequent user receives another user's JWT from cache | Cache-Control: no-store on all token responses; test proxy caching behaviour |
| L1B-F-07 | Denial of Service | Medium | Uncoordinated signing key rotation causing mass token invalidation | TA-05 | Keycloak signing key rotated without overlap period | All active users simultaneously logged out | Configure key retirement delay (1 hour); short Spring Security JWKS cache TTL |

#### FLOW: Token Validation Request / Response (User Management ↔ Token Validation)

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1B-F-08 | Tampering | Critical | Validation response tampered — invalid token accepted | TA-05 | Custom introspection over unprotected internal HTTP; response flipped from invalid to valid | Revoked or expired token accepted; compromised session reactivated | Use in-process local JWT validation; mutual TLS for any introspection endpoint |
| L1B-F-09 | Tampering | High | Revoked token replayed for validation | TA-01 | Stateless JWT validation only; revocation list not checked | Logged-out user's captured token still passes validation | Token revocation list; Keycloak back-channel logout; short access token TTL |
| L1B-F-10 | Information Disclosure | High | Token contents disclosed in validation error response | TA-01 | Error includes decoded payload (user email, expiry) | Victim's identity and expiry timing leaked | Validation response returns only result code; never decoded token contents |
| L1B-F-11 | Information Disclosure | Medium | Full JWT logged during validation | TA-05 | Token Validation Request logged at DEBUG including raw JWT | Developer with log access impersonates any logged user | Never log raw JWTs; log only jti for correlation; log sanitisation rules |
| L1B-F-12 | Denial of Service | High | Token validation flow flooded — all API requests blocked | TA-07 | Thousands of requests with malformed JWTs per second | All legitimate API requests queue and time out | Nginx rate limiting before JWT reaches validation; cache valid results per jti (30s) |
| L1B-F-13 | Denial of Service | Medium | Slow introspection response causing API timeout cascade | TA-07 | Keycloak introspection endpoint slowdown; no timeout configured | All ArcadeHaven API threads blocked; full API unavailability | 500ms timeout on all introspection calls; prefer local JWT validation; circuit breaker |

#### FLOW: Role Request / Response (Token Validation ↔ Claim Role)

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1B-F-14 | Tampering | Critical | Role Response tampered — privilege escalation via role injection | TA-05 | Custom external role resolver communicates over unprotected network; BUYER escalated to ADMIN in response | Full privilege escalation without any Keycloak change; most impactful threat in Auth API | Role resolution always in-process; if external, mutual TLS + message signing; extract roles from verified JWT claims only |
| L1B-F-15 | Tampering | High | Stale role resolved from expired token identity | TA-02 | Role Request uses old token jti; user's role downgraded but token still says PUBLISHER | Downgraded user retains PUBLISHER capabilities | Short TTL forces re-authentication; Keycloak back-channel logout invalidates tokens |
| L1B-F-16 | Information Disclosure | High | Role Response discloses full Keycloak role hierarchy | TA-05 | Response includes internal role IDs and composite structure | Attacker with log access maps entire privilege structure to target | Role Response contains only flat list of application role names; no internal Keycloak metadata |
| L1B-F-17 | Information Disclosure | Medium | Role denial reason leaks role names in 403 body | TA-02 | 403 body states "required: ADMIN, present: BUYER" | Attacker learns exact role name needed for privilege escalation | 403 returns only "Forbidden"; required and present roles logged internally only |
| L1B-F-18 | Denial of Service | Medium | Complex composite role resolution causing authorisation latency | TA-07 | Deeply nested role hierarchy requires multiple recursive DB lookups per Role Request | Authorisation latency spikes; API response time degradation under load | Flat role hierarchy (3 standalone roles); O(1) resolution from JWT claim |
| L1B-F-19 | Denial of Service | Low | Keycloak role definitions accidentally deleted | TA-04 | Admin deletes realm role definitions; all Role Requests return empty role sets | All authenticated users treated as having no permissions; complete authorisation outage | Realm configuration in version control; smoke test after every Keycloak config change |

---

## 7. DFD Level 1 — Database

### 7.1 Data Stores

#### STORE: Game Datastore

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1C-S-01 | Tampering | Critical | Direct price/status modification in DB | TA-05 | DBA executes UPDATE games SET price = 0 or status = 'ACTIVE' directly | All games purchasable for free; all pending games self-activated | Application DB user has INSERT/UPDATE/SELECT only; no direct UPDATE on price/status |
| L1C-S-02 | Repudiation | High | No audit trail for game record changes | TA-05 | Game price changed in DB with no before/after record | Disputed purchase cannot be verified against price at time of sale | Trigger-based or application-level game_audit table; old/new values; actor and timestamp |
| L1C-S-03 | Information Disclosure | High | Unpublished game data returned in public queries | TA-02 | Missing WHERE status = 'ACTIVE' filter returns PENDING/DRAFT games | Unreleased titles and internal notes exposed to buyers | PostgreSQL RLS policy or application-layer filter; integration tests for non-active games |
| L1C-S-04 | Denial of Service | High | Full-table scan blocking concurrent writes | TA-07 | Search query with no index triggers sequential scan; shared lock blocks publisher writes | Publisher game submissions timeout during peak traffic | Indices on games(title, category_id, status, publisher_id); EXPLAIN ANALYZE on all catalogue queries |

#### STORE: Library Datastore

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1C-S-05 | Tampering | Critical | Library entry inserted without completed order | TA-05, TA-04 | Direct INSERT into library_entries without valid order FK | User granted access to any game without payment | FK constraint: library_entries.order_id NOT NULL REFERENCES orders(id) |
| L1C-S-06 | Repudiation | High | No activation key usage audit trail | TA-02 | Key marked as used; no event record with timestamp and IP | Disputed activation cannot be investigated | Append-only activation_events table; record key ID, user ID, timestamp, IP on every attempt |
| L1C-S-07 | Information Disclosure | Critical | Activation keys stored in plaintext | TA-05, TA-01 | DB dump or SQL injection exposes all activation keys | Attacker activates purchased games without paying | AES-256 encryption at application layer before storage; decrypt only for verified owner |
| L1C-S-08 | Denial of Service | Medium | Library table growth degrading per-user queries | TA-07 | Library_entries grows to millions of rows; per-user queries slow without composite index | Library browsing degrades for all users as platform scales | Composite index on library_entries(user_id, game_id); table partitioning at scale |

#### STORE: Order Datastore

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1C-S-09 | Tampering | Critical | Completed order record modified retroactively | TA-05 | UPDATE orders SET total_price = 0.01 on completed order | Financial records falsified; invoice and DB disagree | Insert-only semantics for completed orders; DB trigger preventing UPDATE on completed records |
| L1C-S-10 | Repudiation | Critical | No payment/transaction event trail | TA-05 | Order status changed without appending an event record | Financial reconciliation impossible; compliance violation | Immutable order_events table; every state transition appends a new row; 7-year retention |
| L1C-S-11 | Information Disclosure | Critical | Order history exposed cross-user | TA-02 | ORM query missing WHERE buyer_id clause returns all users' orders | Buyer reads all users' financial records and activation keys | All order queries include AND buyer_id = :jwtUserId; IDOR integration tests mandatory |
| L1C-S-12 | Denial of Service | High | Order write flow flooding DB connections | TA-07 | Hundreds of simultaneous order creates exhaust HikariCP pool | All other DB operations (auth, catalogue) starved of connections | Per-user order rate limiting; async activation key generation; HikariCP pool sizing |

#### STORE: User Information Datastore

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1C-S-13 | Tampering | Critical | Role field written from client payload | TA-02 | PATCH /users/{id} with role: ADMIN; ORM maps it to entity and persists | User self-assigns Admin role without Keycloak change | Role column absent from UserUpdateDTO; roles sourced exclusively from JWT |
| L1C-S-14 | Repudiation | High | No PII change history | TA-02 | User changes email; old value overwritten with no history | GDPR data subject request cannot show what email was on file at a given date | user_profile_history table; old/new values with timestamp; required for GDPR |
| L1C-S-15 | Information Disclosure | Critical | User DB backup exposed unencrypted | TA-05, TA-01 | SQL dump exported to S3 bucket with public access | All user PII, emails, names, addresses exposed; GDPR breach requiring 72h notification | Encrypted DB backups; S3 Block Public Access; access restricted to infrastructure IAM roles |
| L1C-S-16 | Denial of Service | High | User table lock contention during bulk admin operations | TA-04 | SELECT * FROM users with no pagination holds table lock | Login queries blocked; authentication fails for all users during admin operation | Bulk reads on read replica; mandatory pagination on admin user lists; index on users(email) |

### 7.2 Data Flows — Database

#### FLOW: Read/Write Game Data ↔ Game Datastore

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1C-F-01 | Tampering | Critical | SQL injection via game search parameters | TA-01, TA-03 | Unsanitised game title interpolated into native query | DROP TABLE games; or full table exfiltration | JPA parameterised queries exclusively; no native query string concatenation |
| L1C-F-02 | Tampering | Critical | Zero/negative price written to Game Datastore | TA-03 | Missing @DecimalMin constraint; price: 0.00 passes validation | All buyers purchase game for free | @DecimalMin("0.01") on price DTO field; DB CHECK constraint price > 0 |
| L1C-F-03 | Tampering | High | Game status written ACTIVE without approval | TA-03 | Write flow accepts status field from Publisher-role request | Publisher self-activates game bypassing admin review | Status excluded from Publisher-facing write DTO; only AdminGameUpdateDTO includes status |
| L1C-F-04 | Information Disclosure | High | Read flow returns PENDING/DRAFT games to public callers | TA-02 | Missing WHERE status = 'ACTIVE' in public read query | Unreleased titles and internal notes exposed to buyers | All public read queries hardcode status = 'ACTIVE' filter at repository layer |
| L1C-F-05 | Information Disclosure | Medium | Read response includes internal-only columns | TA-02 | SELECT * used; full entity including cost_price returned | Internal business data (cost price, admin flags) exposed via API | Spring Data projections; explicit column lists; separate public and admin response DTOs |
| L1C-F-06 | Denial of Service | High | Full-table scan blocking concurrent writes | TA-07 | No index on title/category_id; sequential scan acquires shared lock | Publisher write timeouts during peak traffic | Indices on filter columns; EXPLAIN ANALYZE; pg_stat_statements monitoring |
| L1C-F-07 | Denial of Service | Medium | Unbounded catalogue read exhausting JVM heap | TA-02, TA-07 | No pagination; full games table loaded into ORM entity list | OOM crash on application server | Mandatory Pageable; max 100 results per page; query timeout in HikariCP |
| L1C-F-08 | Denial of Service | Low | N+1 query on game list with associations | TA-02 | 50 games loaded, then one query per game for Category and Publisher | 101 DB queries; significant latency under moderate load | @EntityGraph or JOIN FETCH; Hibernate statistics in dev; query count assertion in tests |

#### FLOW: Read/Write Library Data ↔ Library Datastore

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1C-F-09 | Tampering | Critical | Library entry written without completed order | TA-05, TA-04 | Direct write bypasses order FK constraint due to missing DB constraint | Free game access without payment | FK constraint library_entries.order_id NOT NULL; DB enforces; application never writes directly |
| L1C-F-10 | Tampering | High | Activation key burned without legitimate activation | TA-02 | Race condition or missing ownership check marks key as used | Real owner's key burned; unable to activate their purchased game | Re-authentication before activation; DB one-way constraint (false → true only); optimistic locking |
| L1C-F-11 | Tampering | High | SQL injection via library filter | TA-02 | user_id or game_id parameter concatenated into native query | Attacker modifies or reads entries belonging to other users | Parameterised queries; user_id from JWT sub always; game_id validated before use |
| L1C-F-12 | Information Disclosure | Critical | Activation keys returned in plaintext in datastore response | TA-05, TA-01 | Read flow returns activation_key column; response logged at DEBUG | Keys with monetary value exposed; anyone can use them | AES-256 encryption before storage; decrypt only for verified owner; never log key values |
| L1C-F-13 | Information Disclosure | High | Missing user_id scope in library read query | TA-02 | Query scoped only to game_id without user_id; returns all users' entries for that game | Buyer reads activation keys of every user who owns a specific game | All library queries include AND user_id = :authenticatedUserId; cross-user integration test |
| L1C-F-14 | Information Disclosure | Medium | Library response cached without user_id in cache key | TA-02 | Shared cache keyed only on game_id; different user gets previous user's library entry from cache | Activation key of another user served from cache | Library responses not cached at shared layer; cache key must include authenticated user_id |
| L1C-F-15 | Denial of Service | Medium | Library table growth degrading queries | TA-07 | Millions of rows; per-user query slow without composite index | Library browsing degraded platform-wide | Composite index on (user_id, game_id); partition at scale |
| L1C-F-16 | Denial of Service | Low | Concurrent activation writes causing lock contention | TA-02 | User double-clicks activate button; two concurrent UPDATE statements on same row | Duplicate activation event records; data inconsistency | Optimistic locking with @Version; idempotency check before write; 409 Conflict on race |

#### FLOW: Read/Write Order Data ↔ Order Datastore

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1C-F-17 | Tampering | Critical | Completed order record modified retroactively | TA-05 | UPDATE orders on completed record; invoice PDF and DB disagree | Financial fraud; compliance violation | Insert-only after completion; DB trigger on UPDATE for COMPLETED orders; invoice PDF hash stored |
| L1C-F-18 | Tampering | Critical | Client-supplied price written to order | TA-02 | Order DTO contains price field sourced from client; server uses it | Game purchased for near-zero price | Price always resolved server-side from Game Datastore immediately before order write |
| L1C-F-19 | Tampering | High | SQL injection via order history filter | TA-02 | Date/status filter concatenated into native query | Returns or modifies all users' orders | Parameterised queries; mandatory AND buyer_id = :jwtUserId clause |
| L1C-F-20 | Information Disclosure | Critical | Order datastore response exposes other users' financial records | TA-02 | Query scoped only to order_id without buyer_id ownership check | Any authenticated user reads all order financial records and activation keys | Every order query mandatorily scoped to JWT sub; IDOR test mandatory in CI |
| L1C-F-21 | Information Disclosure | High | Financial data logged from datastore response | TA-05 | Full order object logged at INFO; payment reference and amount in logs | PCI-DSS and GDPR violation; high-value log target | Never log full order objects; log only order_id and status; @JsonIgnore on financial fields |
| L1C-F-22 | Information Disclosure | Medium | Stale PENDING status served from cache | TA-02 | ORM second-level cache returns stale order status | User appears to have unpaid order; cannot access purchased game | Disable second-level cache for Order entity; always read order status fresh from DB |
| L1C-F-23 | Denial of Service | High | Order write flow flooding DB connections | TA-07 | Hundreds of orders per second; each triggers DB write transaction chain | HikariCP pool exhausted; all DB operations fail | Per-user rate limiting; async activation key generation; per-subsystem connection limits |
| L1C-F-24 | Denial of Service | Medium | Large order history read causing OOM | TA-02 | No date range filter; all historical orders with associations loaded | JVM heap exhaustion; application crash | Mandatory pagination; date range required; index on orders(buyer_id, created_at DESC) |
| L1C-F-25 | Denial of Service | Low | Synchronous PDF generation within order write transaction | TA-07 | PDF generation holds DB transaction open; write latency spikes under order spike | DB lock contention; write timeouts for all concurrent order operations | Async PDF generation via Spring @Async or message queue; order record committed first |

#### FLOW: Read/Write User Data ↔ User Information Datastore

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1C-F-26 | Tampering | Critical | Email written without ownership verification — account takeover | TA-02 | Email change accepted without re-auth or email verification | Attacker hijacks admin account via password reset to their inbox | Re-authentication required; email verification before commit; notify old address |
| L1C-F-27 | Tampering | Critical | SQL injection via user profile search | TA-01, TA-04 | Admin user search uses native query with string concatenation | UNION SELECT exfiltrates entire users table including Keycloak subject IDs | Parameterised queries; LIKE with bound parameter; even admin queries enforce parameterisation |
| L1C-F-28 | Tampering | High | Role field written from client payload | TA-02 | UserUpdateDTO maps role field from request body to entity | User self-assigns ADMIN role | Role field absent from UserUpdateDTO; roles from JWT only; never from DB user record |
| L1C-F-29 | Information Disclosure | Critical | Full user PII returned to non-owner callers | TA-02 | GET /users/{id} without ownership check; any caller receives full PII | GDPR data breach; enumeration of all users' personal data | JWT sub must equal target user_id (or ADMIN role required); IDOR test mandatory |
| L1C-F-30 | Information Disclosure | Critical | PII logged from datastore response | TA-05 | User entity logged at INFO including email, name, address | PII in log store violates GDPR data minimisation and purpose limitation | toString() returns only user_id; @JsonIgnore on PII fields; structured logging field allowlist |
| L1C-F-31 | Information Disclosure | High | User DB backup exposed — unencrypted PII at rest | TA-05, TA-01 | Plaintext SQL dump to misconfigured public S3 bucket | All user PII exposed; GDPR breach requiring 72h notification | Encrypted backups; S3 Block Public Access; IAM access restricted to infrastructure roles |
| L1C-F-32 | Information Disclosure | Medium | Keycloak subject ID exposed in user response | TA-02 | keycloak_sub field included in API response DTO | Attacker uses sub to target Keycloak admin API; cross-system identity correlation | keycloak_sub excluded from all response DTOs; treated as internal system field |
| L1C-F-33 | Denial of Service | High | User table lock contention during bulk admin read | TA-04 | SELECT * FROM users with no pagination holds shared lock | Login queries blocked; authentication fails during admin operation | Bulk reads on read replica; mandatory pagination; index on users(email, keycloak_sub) |
| L1C-F-34 | Denial of Service | Medium | User enumeration flood exhausting DB connections | TA-07 | Thousands of GET /users/{id} with sequential IDs; DB query per request even when rejected | DB connection pool exhausted; all other flows starved | Apply ownership check before DB query; fail fast on JWT mismatch without hitting DB; IP rate limiting |
| L1C-F-35 | Denial of Service | Low | PII history table unbounded growth causing write latency | TA-02 | User repeatedly changes display name; thousands of history rows | History table write latency increases; affects primary user table write flow | 7-year retention policy; archive to cold storage; index on (user_id, changed_at) |

---

## 8. DFD Level 1 — RAWG API

### 8.1 Processes and Data Flows

#### FLOW: Game Request / Game Response (Game Search ↔ RAWG REST API)

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1D-F-01 | Tampering | Critical | Game Response tampered in transit — malicious data injected | TA-06 | DNS poisoning or BGP hijack redirects RAWG calls; tampered response returned | XSS payloads in game titles/descriptions stored in Game Datastore | HTTPS with full TLS certificate validation; treat all RAWG fields as untrusted; HTML sanitise before storage |
| L1D-F-02 | Tampering | High | Game Request query parameter injection | TA-03 | Publisher submits title with RAWG query metacharacters overriding API key | Attacker's key used; quota of another party consumed; unexpected result volumes | URL builder for all RAWG requests; encode each parameter individually; validate input against allowlist |
| L1D-F-03 | Tampering | Medium | Game Response unexpected schema breaks downstream processing | TA-06 | RAWG silently renames fields; deserialisation produces null values | Null game IDs or ratings written to datastore; catalogue data corrupted | Validate required fields post-deserialisation; structured alert on unexpected schema changes |
| L1D-F-04 | Information Disclosure | Critical | RAWG API key exposed in Game Request URL — logged by Nginx | TA-05, TA-01 | API key as query parameter appears in Nginx access logs and APM traces | Any log reader uses the key; quota exhausted; RAWG account compromised | Inject API key as HTTP header if supported; configure log masking; store in secrets manager |
| L1D-F-05 | Information Disclosure | Medium | Full Game Response body logged — unnecessary data retention | TA-05 | DEBUG logging enabled; complete RAWG response persisted to logs | Large RAWG data retained beyond useful lifetime; secondary exposure risk | Log only count and status code; disable DEBUG in production; structured logging field allowlist |
| L1D-F-06 | Denial of Service | High | Game Request flood exhausting RAWG daily quota | TA-03, TA-07 | Malicious publisher calls game search in tight loop | All publishers' game creation workflows fail for remainder of day | Per-publisher rate limit; global quota tracker; cache identical search results for 10 min TTL |
| L1D-F-07 | Denial of Service | Medium | RAWG API unavailability blocking synchronous game search | TA-06 | RAWG returns 503; no timeout configured; thread blocked indefinitely | Thread pool exhaustion; unrelated platform operations affected | 3s connect / 5s read timeout; WebClient (non-blocking); Resilience4j circuit breaker |
| L1D-F-08 | Denial of Service | Low | Large Game Response causing JVM memory pressure | TA-06 | Broad search term returns multi-megabyte JSON payload | Frequent GC pauses under concurrent publisher searches | Max page_size=20 in all Game Requests; response content length limit before deserialisation |

#### FLOW: Game Metadata Request / Response (RAWG REST API ↔ Game Metadata)

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1D-F-09 | Tampering | Critical | Metadata Response carries stored XSS payload — persisted and executed in all buyers' browsers | TA-06 | Compromised/spoofed RAWG returns description with script tag; stored unsanitised | Session tokens stolen from all buyers viewing the game page | OWASP Java HTML Sanitizer strict allowlist before storage; output encoding at render; CSP header |
| L1D-F-10 | Tampering | High | Image URLs in Metadata Response point to malicious content | TA-06 | Spoofed response contains image URLs redirecting to malware or inappropriate imagery | Buyers sent to attacker-controlled content; content policy bypassed | Download and re-host all images; validate URLs against RAWG CDN domain allowlist |
| L1D-F-11 | Tampering | High | SSRF via image URL in Metadata Response — internal service probing | TA-06 | Spoofed response contains background_image: http://169.254.169.254/metadata | AWS IAM credentials retrieved; full cloud account access | Validate all fetched URLs against trusted domain allowlist; block RFC1918 and link-local addresses |
| L1D-F-12 | Tampering | Medium | Metadata Response overwrites admin-curated fields | TA-06 | Scheduled refresh receives Metadata Response overwriting admin's content corrections | Platform repeatedly re-publishes content already reviewed and removed | admin_locked boolean per field; Game Metadata process skips locked fields during refresh |
| L1D-F-13 | Information Disclosure | High | RAWG API key exposed in Game Metadata Request | TA-05, TA-01 | API key in request URL logged by application logger and APM tracing spans | RAWG account compromised; quota exhausted | Same as L1D-F-04; consistent across all RAWG request types |
| L1D-F-14 | Information Disclosure | Medium | Metadata Response contains RAWG-embedded tracking elements | TA-06 | Third-party tracking pixels in description HTML stored and rendered on ArcadeHaven | Buyer browsing behaviour reported to third parties without consent — GDPR violation | Sanitisation allowlist blocks all iframe, tracking pixel, and external resource references |
| L1D-F-15 | Denial of Service | High | Metadata refresh loop exhausting RAWG quota | TA-06 | Bug in scheduler triggers repeated Metadata Requests for same game ID every few seconds | RAWG quota exhausted; metadata enrichment broken for all publishers | Minimum 24h refresh interval per game; last_fetched timestamp check; dead-letter queue with backoff |
| L1D-F-16 | Denial of Service | Medium | Large Metadata Response causing synchronous processing stall | TA-06 | Game with hundreds of screenshots produces multi-megabyte Metadata Response | Web request thread blocked for seconds; all concurrent users affected | Async processing via message queue; limit 10 screenshots per game; stream-parse large responses |
| L1D-F-17 | Denial of Service | Low | RAWG outage leaving games in degraded metadata state | TA-06 | RAWG partial outage; Metadata Requests time out | New games created with empty metadata; blank game pages for publishers and buyers | Graceful degraded state (metadata_status = PENDING); background retry with exponential backoff |

#### FLOW: Game Details Request / Response (Game Details ↔ RAWG REST API)

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L1D-F-18 | Tampering | Critical | Game Details Response age rating tampered — minor accessing adult content | TA-06 | MitM changes age_rating from MATURE to EVERYONE in Details Response | Minor purchases and accesses 18+ content; PEGI/ESRB compliance violation; legal liability | TLS with strict CA validation; validate age rating against known PEGI/ESRB enumeration; reject unknown values |
| L1D-F-19 | Tampering | High | Forged RAWG game ID — wrong game details fetched | TA-03 | Publisher supplies RAWG ID of a popular game; its details stored under their listing | High ratings and age ratings of popular game applied to a low-quality listing; buyer deception | Validate RAWG game ID against title match in Details Response before commit; admin review of linkage |
| L1D-F-20 | Tampering | High | Rating change affects historical orders — no snapshot stored | TA-06 | RAWG updates rating; ArcadeHaven overwrites stored rating; historical order record now inaccurate | Buyer purchased TEEN-rated game but order now shows MATURE; compliance and dispute problem | Store rating snapshot in order_items at purchase time; live listing and order snapshot independent |
| L1D-F-21 | Information Disclosure | High | RAWG API key exposed in Game Details Request | TA-05, TA-01 | API key in URL of a separate call; microservices may log it in a different log stream | Broader log access in microservices exposes key | API key injected from shared secrets manager; never passed between services in plaintext |
| L1D-F-22 | Information Disclosure | Medium | Game Details Response contains individual developer PII | TA-06 | Developer names and contact details stored and displayed without consent basis | GDPR Article 6 violation; processing personal data without lawful basis | Limit storage to studio name not individual names; document RAWG as third-party PII source in ROPA |
| L1D-F-23 | Information Disclosure | Low | Game Details Response cached without sufficient cache key context | TA-02 | Cache keyed only on RAWG game ID; two publishers link same RAWG ID simultaneously | Second publisher receives cached response from first; potential misattribution | Cache key includes both RAWG game ID and ArcadeHaven game ID; 24h TTL on stable fields |
| L1D-F-24 | Denial of Service | High | Game Details Request triggered on every buyer page view | TA-07 | Live RAWG call per page load; sale event traffic exhausts daily quota | All game detail pages show blank metadata during highest-traffic period | Game details cached in Game Datastore after first fetch; buyer pages served from local datastore only |
| L1D-F-25 | Denial of Service | Medium | Malformed RAWG game ID causing repeated 404 errors without backoff | TA-03 | Invalid RAWG game ID submitted; retry mechanism re-sends immediately on 404 | RAWG quota consumed for invalid lookups; error monitoring flooded | Validate game ID format before request; fail immediately on 404 (no retry); backoff only for 5xx |
| L1D-F-26 | Denial of Service | Low | Concurrent Details Requests for same RAWG ID causing duplicate writes | TA-03 | Two publishers link same RAWG ID simultaneously; both write Details Response simultaneously | Duplicate metadata records or race condition in write path | Idempotent upsert (INSERT ... ON CONFLICT DO UPDATE); scoped to ArcadeHaven game ID |

---

## 9. DFD Level 2 — Game Management

### 9.1 Processes

#### PROCESS: Game Data Handler

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L2-P-01 | Spoofing | Critical | Publisher ID sourced from request body not JWT | TA-03 | POST /games with publisherId: 99 in body; handler uses body value | Game created under a different publisher's identity | publisherId extracted exclusively from SecurityContext (JWT sub); unit test this binding |
| L2-P-02 | Tampering | Critical | Negative or zero price accepted and persisted | TA-03 | Missing @DecimalMin; price: -5.00 passes DTO validation | Buyers receive credit instead of being charged | @DecimalMin("0.01") on GameDTO; DB CHECK price > 0; reject non-positive prices with 400 |
| L2-P-03 | Repudiation | High | No before/after values in game update log | TA-03, TA-04 | Publisher updates price; only new value logged | Old price at time of disputed sale cannot be proven | game_change_log: field_name, old_value, new_value, actor_id, timestamp on every UPDATE |
| L2-P-04 | Information Disclosure | High | PENDING games returned in public game list | TA-02 | findAll() without WHERE status = 'ACTIVE'; PENDING games returned to buyers | Unreleased titles exposed to public | All public queries scoped to ACTIVE; separate admin and public repository methods |
| L2-P-05 | Denial of Service | High | N+1 query on game list | TA-07 | 100 games fetched; separate query per game for Category and Publisher | 101 DB queries; extreme latency under load | JOIN FETCH / @EntityGraph; Hibernate statistics; query count assertion in tests |
| L2-P-06 | Elevation of Privilege | Critical | Status field writable by Publisher via update DTO | TA-03 | PUT /games/{id} with status: ACTIVE in body; handler maps all incoming fields | Publisher self-activates game; admin approval bypassed | Status excluded from Publisher-facing GameUpdateDTO; only AdminGameUpdateDTO includes status |

#### PROCESS: Game Metadata Handler

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L2-P-07 | Spoofing | Critical | RAWG API key exposed via /actuator/env endpoint | TA-01 | Exposed Spring Boot actuator endpoint reveals RAWG API key in configuration | Attacker uses key; quota exhausted; RAWG account compromised | Disable /actuator/env in production; API key in secrets manager; rotate on any suspected exposure |
| L2-P-08 | Tampering | High | Unsafe deserialisation of RAWG JSON | TA-06 | RAWG returns deeply nested JSON; Jackson processes recursive references | Stack overflow or billion-laughs style memory exhaustion | Jackson FAIL_ON_UNKNOWN_PROPERTIES=true; response size limit; strict POJO mapping |
| L2-P-09 | Repudiation | High | RAWG fetch failures silently swallowed | TA-06 | Handler catches all exceptions; failed fetches invisible | Metadata quality issues undetected for days | Log all RAWG fetch failures with game ID, error type, timestamp; alert on elevated failure rate |
| L2-P-10 | Information Disclosure | High | RAWG response cached insecurely — API key in cache key | TA-05 | Full request URL (including API key) used as Redis cache key | Cache key stored in Redis readable by all application threads exposes API key | Cache response body only; use RAWG game ID as cache key; Redis not accessible outside app network |
| L2-P-11 | Denial of Service | High | Synchronous RAWG fetch blocking game creation | TA-06 | RAWG response takes >5s; game creation request times out and rolls back | Publishers cannot list games during RAWG slowdowns | Decouple RAWG fetch from synchronous game creation; create game first (PENDING_METADATA); fetch async |
| L2-P-12 | Elevation of Privilege | High | Metadata handler callable without publisher ownership check | TA-02, TA-03 | Any authenticated user triggers metadata refresh for any game ID | Buyer overwrites a publisher's game metadata | Handler verifies publisher ownership; called only from GameManagementModule which also checks ownership |

#### PROCESS: Game Images Handler

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L2-P-13 | Spoofing | Medium | Pre-release screenshots accessible without auth | TA-01, TA-02 | GET /games/images/{imageId} for PENDING game; no status or ownership check | Unreleased game screenshots exposed before launch | ACTIVE games: publicly accessible. PENDING/DRAFT: require publisher ownership or ADMIN role |
| L2-P-14 | Tampering | Critical | Malicious file upload bypassing MIME check — potential RCE | TA-03 | PHP webshell uploaded; Content-Type spoofed to image/jpeg; only header validated | Executable stored; served to buyers; potential remote code execution | Magic-byte validation using Apache Tika; ClamAV scan; UUID rename; block executable MIME types |
| L2-P-15 | Repudiation | High | No image upload audit trail | TA-03 | Publisher uploads screenshot with copyrighted material; no log of upload event | DMCA takedown cannot identify when image was uploaded or by whom | Log: publisher ID, game ID, original filename, UUID filename, SHA-256 hash, file size, timestamp |
| L2-P-16 | Information Disclosure | High | EXIF metadata in uploaded images exposes developer PII | TA-03 | Screenshot uploaded with EXIF containing developer username, hostname, geolocation | Developer's personal information and internal infrastructure details exposed | Strip all EXIF/metadata using ImageIO re-encode before storage; store clean copies only |
| L2-P-17 | Denial of Service | Critical | Decompression bomb causing OOM crash | TA-03, TA-07 | 100×100 pixel PNG that decompresses to 4 GB uploaded | JVM OOM crash; entire application unavailable | Validate compressed file size before decompression; max image dimensions 4096×4096; stream processing; 10 MB gateway limit |
| L2-P-18 | Elevation of Privilege | Critical | Publisher replacing another publisher's game images | TA-03 | PUT /games/{gameBId}/images/{imageId}; only Publisher role checked, not game ownership | Publisher A defaces Publisher B's game listing | Image write operations verify games.publisher_id matches JWT sub; cross-publisher test in CI |

### 9.2 Data Flows — Level 2 Game Management

#### FLOW: Game Data Request/Response · Insert/Query Game Data

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L2-F-01 | Tampering | Medium | Internal data flow tampered in microservices setup | TA-05 | Game Module and Data Handler communicate over unencrypted HTTP; DTO modified in transit | Price or status altered after application-layer validation but before DB write | For monolith: in-process calls not interceptable. For microservices: mutual TLS; HMAC on messages |
| L2-F-02 | Information Disclosure | High | Game query response includes admin-only fields | TA-02 | SELECT * in public query; internal notes and cost_price serialised into JSON response | Internal business data (cost price, admin flags) leaked to API caller | Projection interfaces in Spring Data; explicit column lists; admin and public response DTOs |
| L2-F-03 | Denial of Service | Medium | Game update triggering cascading DB operations via ORM | TA-03 | Updating game category triggers recomputation across order_items and library_entries | Unexpected bulk DB operations; latency spikes under load | Avoid cascade updates across aggregate boundaries; use domain events for cross-aggregate side effects |

#### FLOW: Game Metadata Request/Response (Handler ↔ RAWG API)

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L2-F-04 | Tampering | Critical | SSRF via image URL in RAWG Metadata Response | TA-06 | background_image field points to http://169.254.169.254/latest/meta-data/ | AWS IAM credentials retrieved via SSRF; full cloud account access | Allowlist validation on all URLs fetched; block RFC1918 and link-local; 0 redirects; egress proxy |
| L2-F-05 | Information Disclosure | High | RAWG response body logged including API key | TA-05 | Debug statement logs full RAWG response including API key in request context | API key in log store; quota exhausted; RAWG account compromised | Log only RAWG game ID and status code; never log response bodies; structured logging field allowlist |
| L2-F-06 | Denial of Service | High | RAWG API timeout causing synchronous thread stall | TA-06 | RestTemplate call to RAWG blocks for full 30s default timeout | Thread pool exhaustion; Spring Boot unresponsive during RAWG slowdown | 3s/5s timeouts; WebClient (non-blocking); Resilience4j circuit breaker with fallback |

#### FLOW: Image Request/Response · Insert/Fetch Image (Handler ↔ File System)

| # | STRIDE | Severity | Threat | Threat Agent | Attack Vector | Abuse Case | Mitigation |
|---|--------|----------|--------|-------------|---------------|-----------|-----------|
| L2-F-07 | Tampering | Medium | Race condition on concurrent image writes | TA-03 | Two concurrent uploads for same game compute identical destination path | Second write corrupts or truncates first file | UUID filenames eliminate path collisions; atomic file write (write to .tmp then rename) |
| L2-F-08 | Information Disclosure | Critical | Path traversal on image read — server file disclosure | TA-01 | GET /games/images?path=../../etc/passwd; handler uses path param directly | Any server file returned to caller including credentials and config | Never accept file paths from client input; look up UUID in DB; construct read path server-side only |
| L2-F-09 | Denial of Service | High | Concurrent image downloads exhausting I/O bandwidth | TA-07 | Flash sale causes thousands of simultaneous image requests; disk I/O saturates | DB write throughput degraded; order processing fails | CDN or object storage (S3 + CloudFront) for game images; Cache-Control: max-age=86400; separate partition |

---

## 10. Threat Summary

### Threat Count by DFD Level

| DFD Level | External Entities | Processes | Data Stores | Data Flows | Total |
|-----------|:-----------------:|:---------:|:-----------:|:----------:|:-----:|
| Level 0 | 6 | 6 | 8 | 9 | **29** |
| L1 — ArcadeHaven System | — | 30 | — | — | **30** |
| L1 — Auth API | — | 24 | — | 19 | **43** |
| L1 — Database | — | — | 16 | 27 | **43** |
| L1 — RAWG API | — | — | — | 26 | **26** |
| L2 — Game Management | — | 18 | — | 9 | **27** |
| **Total** | **6** | **78** | **24** | **90** | **198** |

### Threat Count by STRIDE Category

| Category | Count | % of Total |
|----------|------:|:---------:|
| Spoofing | 24 | 12% |
| Tampering | 62 | 31% |
| Repudiation | 22 | 11% |
| Information Disclosure | 48 | 24% |
| Denial of Service | 32 | 16% |
| Elevation of Privilege | 10 | 5% |
| **Total** | **198** | **100%** |

### Threat Count by Severity

| Severity | Count | % of Total |
|----------|------:|:---------:|
| Critical | 42 | 21% |
| High | 78 | 39% |
| Medium | 52 | 26% |
| Low | 26 | 13% |
| **Total** | **198** | **100%** |

