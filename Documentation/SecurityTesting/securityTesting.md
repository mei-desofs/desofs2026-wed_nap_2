# ArcadeHaven Security Testing Plan

## 1. Purpose and Scope

This document defines the Phase 1 security testing plan for ArcadeHaven. The plan is focused on defining the:
- **Abuse Cases** testing methodology;
- **DFDs STRIDE** testing methodology.

## 2. Abuse Tests Cases

| Test ID  | Abuse Case ID | Abuse Case | What to Test| Test Procedure| Expected Result |
| -------- | ------------- | ---------- | ----------- | ------------- | --------------- |
| T-AC-01  | AC-01         | Inject Malicious Code | Input sanitization | Submit script payloads, malformed JSON, and SQL meta-characters in user inputs | Payload is rejected or sanitized. No scripts are executed and no SQL query manipulation occurs |
| T-AC-02  | AC-02         | Hijack Account | Session and token protection | Attempt the reuse of an existing session JWT Token | Access denied and report suspicious reuse of the token in logs |
| T-AC-03  | AC-03         | Dictionary Attack | Login protection | Attempt logins using common password dictionaries | Rate limiting, lock endpoint to user/IP or MFA challenge triggered. No account takeover. |
| T-AC-04  | AC-04         | Brute Force Login | Authentication resilience | Repeated failed login attempts | Lock endpoint to user/IP, throttling or MFA enforcement |
| T-AC-05  | AC-05         | JWT Token Theft | Token misuse detection | Replay a valid JWT from another client or use expired token | Invalid/expired token rejected with no authorization access |
| T-AC-06a | AC-06 (A-BC)  | Privilege Escalation (Role Management) | Admin-only operations | Call role change endpoint with non-admin tokens | Non-execute operation with 403 Fordbidden response |
| T-AC-06b | AC-06 (G-MAC) | Privilege Escalation (Game Management) | Restricted game-management actions | Attempt protected game management operations with non-publisher token | Non-execute operation with 403 Fordbidden response |
| T-AC-07  | AC-07         | JWT Token Theft (Protected Operations) | Endpoint authentication | Call protected role-management endpoints with invalid, expired or stolen tokens | Non-execute operation with 403 Fordbidden response |
| T-AC-08  | AC-08         | Upload Malicious File | File upload validation | Upload executable and scripts files renamed as image/PDF | Upload rejected or filename sanitized. No file execution occurs |
| T-AC-09  | AC-09         | Upload Oversized File | Upload images above specified size | Upload images above allowed size | Upload rejected with size limit rejection response |
| T-AC-10  | AC-10         | Bypass MIME Verification | File type verification | Upload file with safe extension but malicious content or mismatched MIME type | Server validates contents and either rejects or sanitizes malicious file |
| T-AC-11  | AC-11         | Guess Game Key | Key entropy and validation | Try random key activations and brute-force attempts | Invalid keys rejected, trigger rate limiting and monitorig |
| T-AC-12  | AC-12         | Modify Order | Server-side order integrity            | Alter order data in body | Server recalculates and validates values. Order rejected |
| T-AC-13  | AC-13         | Force Duplicated Purchase | Business logic integrity | Attempt repeated purchase of a game | Server validates game purchase and rejects or handles rest of the order safely |
| T-AC-14  | AC-14         | Bypass Payment | Order finalization validation | Attempt to complete order without valid payment confirmation | Server-side verifies payment confirmation and order is not completed |
| T-AC-15  | AC-15         | Invoice ID Enumeration | Object-level authorization | Request invoices by random/sequential IDs of other users invoices | Access denied with 403 Fordbidden response |
| T-AC-16  | AC-16         | Path Traversal Attack | File path handling | Request `../` paths and encoded traversal variants in download endpoints | Traversal path blocked with rejected operation or sanited path file |
| T-AC-17  | AC-17         | Unauthorized Invoice Access | Secure file access | Try direct file download without proper token | Access denied with 403 Fordbidden response |

## 3. STRIDE Tests Cases

| Test Family ID | STRIDE Category        | Threats | What to Test | Implementation | Expected Result |
| -------------- | ---------------------- | ------- | ------------ | -------------- | --------------- |
| T-ST-01        | Spoofing               | User impersonation, stolen credentials/JWT, untrusted issuer acceptance | Authentication and token trust | Test missing token, invalid signature, expired token, wrong issuer, wrong audience or replayed token | All invalid tokens rejected |
| T-ST-02        | Spoofing               | Credential stuffing, brute forcing  | Login defense controls | Automated repeated login attempts, common-password attempts | Lock endpoint to user/IP, endpoint throttling, MFA triggered. No takeover occurs |
| T-ST-03        | Tampering              | Request body manipulation, client-side changes | Server-side validation | Modify protected fields in requests | Server validates and rejects tampered fields. Uses canonical values for operations/recalculations |
| T-ST-04        | Tampering              | SQL injection | Injection rejection | Submit SQL payloads in credentials or requests parameter | No query manipulation occurs |
| T-ST-05        | Tampering              | JWT Token/Header payload tampering | Token integrity | Modify claims, role fields or header values | Token and access rejected with no privilege gain |
| T-ST-06        | Tampering              | Path traversal, arbitrary file read-write | File system integrity and safety | Use traversal payloads in file requests | Access denied with 403 Fordbidden response |
| T-ST-07        | Tampering              | Malicious external content from RAWG | External input sanitization | Send content from external metadata in request | Stored content sanitized and no malicious code/script executed |
| T-ST-08        | Repudiation            | Missing audit logs for protected operations | Security logging | Perform protected operations and verify proper logging | Logs are complete, attributable, and tamper-evident |
| T-ST-09        | Repudiation            | Missing auth event logging | Authentication event logging | Trigger login success and failure, logout, denied access and token failure | Events are logged, complete, attributable and tamper-evident |
| T-ST-10        | Information Disclosure | Verbose errors, stack traces, secrets in logs/config | Error handling and secret exposure | Exception occurs in CI output | No sensitive internals or secrets exposed |
| T-ST-11        | Information Disclosure | Tokens in URLs/logs, insecure storage | Token confidentiality | Pass token in query or logs | Token rejected and not logged |
| T-ST-12        | Information Disclosure | Unauthenticated file access | Access to protected files | Request protected resources belonging to another user | Rejected access with only authorized access to proper owner or admin |
| T-ST-13        | Information Disclosure | Data in transit exposure | Transport security | Attempt HTTP access, downgrade attempts, inspect headers/cookies | HTTPS enforce and no sensitive data exposure |
| T-ST-14        | Denial of Service      | No rate limiting on public/protected endpoints | API abuse resistance | Burst requests to endpoints | Access denied with 429 Too Many Requests. Service remains available |
| T-ST-15        | Denial of Service      | Oversized uploads, unbounded queries, no pagination | Resource exhaustion protection | Large uploads, huge page sizes, unbounded list requests | Server-side limits enforced with no excessive memory/disk/CPU consumption |
| T-ST-16        | Denial of Service      | External dependency failures | Resilience and graceful degradation | Simulate external API/dependency timeout/unavailability | Cached keys/data or fallback used. Graceful failure |
| T-ST-17        | Denial of Service      | Expensive synchronous processing | Backpressure and async processing | Trigger many concurrent invoice/order operations | Queueing/limits applied and system remains responsive |
| T-ST-18        | Elevation of Privilege | Missing role checks | RBAC enforcement | User attempts unauthorized operations | Access denied with 403 Fordbidden response |
| T-ST-19        | Elevation of Privilege | Ownership bypass | Object-level authorization | Access/modify another user's resources | Ownership check enforced on every request |
| T-ST-20        | Elevation of Privilege | Wrong JWT claim mapping or stale claims after role change | Authorization trust verification | Change user data, reuse old token, craft role in unexpected claim | Old privileges invalidated. Only trusted claim path honored |
