# ArcadeHaven Security Testing Plan

## 1. Purpose and Scope

This document defines the Phase 1 security testing plan for ArcadeHaven.

## 2. Security Tests

| ID | What to Test | Implementation | Expected Results |
| -- |--------------| -------------- | ---------------- |
| T-01 | Input sanitization | Submit script payloads, malformed JSON, and SQL meta-characters in user inputs | Payload is rejected or sanitized. No scripts are executed and no SQL query manipulation occurs |
| T-02 | Session and token protection | Attempt reuse of an existing session JWT token | Access denied and suspicious token reuse is logged |
| T-03 | Login protection (dictionary attacks) | Attempt logins using common password dictionaries | Rate limiting, account lock or MFA challenge triggered |
| T-04 | Brute force login protection | Repeated failed login attempts | Rate limiting, IP/user lock or MFA enforcement |
| T-05 | JWT token theft and replay | Replay valid or expired JWT from another client | Token is rejected and access is denied |
| T-06 | Privilege escalation (role management) | Call role-change/admin endpoints with non-admin token | Operation blocked with 403 Forbidden |
| T-07 | Privilege escalation (game management) | Attempt restricted game-management actions with non-publisher token | Operation blocked with 403 Forbidden |
| T-08 | Endpoint authentication enforcement | Call protected endpoints with invalid/expired/stolen tokens | Request is rejected with 403 Forbidden |
| T-09 | Malicious file upload | Upload executable/scripts disguised as image/PDF | Upload is rejected or sanitized; no execution possible |
| T-10 | Oversized file upload | Upload files above allowed size limit | Request is rejected with size limit error |
| T-11 | MIME type bypass | Upload file with mismatched extension and content | File is rejected after content validation |
| T-12 | Game key guessing attacks | Attempt brute force activation keys | Invalid keys rejected and rate limiting applied |
| T-13 | Order modification | Alter order data in request body | Server validates and rejects tampered values |
| T-14 | Duplicate purchase abuse | Attempt repeated purchase of same game | Duplicate purchase is blocked or safely handled |
| T-15 | Payment bypass attempt | Complete order without valid payment | Order is rejected; payment is verified server-side |
| T-16 | Invoice ID enumeration | Request sequential/random invoice IDs | Access denied for unauthorized invoices |
| T-17 | Path traversal attack | Use `../` or encoded traversal in file requests | Request is blocked; no unauthorized file access |
| T-18 | Unauthorized file access | Direct access to files without authentication | Access denied (403 Forbidden) |
| T-19 | JWT claim manipulation | Modify JWT claims or roles manually | Token is invalidated and rejected |
| T-20 | SQL injection attempts | Inject SQL in login or request fields | No query execution or database manipulation |
| T-21 | Session/token reuse detection | Reuse stolen or expired tokens across clients | Token rejected and anomaly logged |
| T-22 | Rate limiting abuse | Burst requests to endpoints | Requests throttled with 429 responses |
| T-23 | Large payload/resource exhaustion | Send large uploads or unbounded requests | Server enforces limits and remains stable |
| T-24 | External dependency abuse | Simulate API failure or timeout | System degrades gracefully without crash |
| T-25 | Concurrent heavy operations | Trigger multiple simultaneous operations | System applies limits/queueing and stays responsive |
| T-26 | Role-based access control bypass | Try unauthorized admin operations | Access denied (403 Forbidden) |
| T-27 | Ownership bypass (IDOR) | Access/modify another user’s resources | Request rejected; ownership enforced |
| T-28 | Stale privilege usage | Reuse old JWT after role change | Old privileges invalidated |
| T-29 | Verbose error exposure | Trigger exceptions/errors | No sensitive information exposed in responses or logs |
| T-30 | Token leakage via logs/URLs | Pass tokens in query or inspect logs | Token is not exposed or is rejected if misused |
| T-31 | Data exposure of other users | Access other users’ resources | Only owner/admin access allowed |
| T-32 | HTTPS enforcement | Attempt HTTP downgrade | All traffic forced to HTTPS |
| T-33 | Security logging verification | Perform sensitive actions | All actions are logged and traceable |
| T-34 | Authentication event logging | Login/logout/failure events | All authentication events are recorded |
| T-35 | File path safety validation | Attempt unsafe file path access | Path is normalized and blocked if invalid |