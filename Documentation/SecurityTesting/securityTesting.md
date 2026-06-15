[← Back to index page](../Overview/overview.md)

# ArcadeHaven Security Testing Plan

## 1. Purpose and Scope

This document defines the Phase 1 security testing plan for ArcadeHaven.

## 2. Security Tests

| ID   | What to Test                             | Implementation                                                                 | Expected Results |
|------|------------------------------------------|--------------------------------------------------------------------------------| ---------------- |
| T-01 | Input sanitization                       | Submit script payloads, malformed JSON, and SQL meta-characters in user inputs | Payload is rejected or sanitized. No scripts are executed and no SQL query manipulation occurs |
| T-02 | Session and token protection             | Attempt reuse of an previous session JWT token                                 | Access denied and suspicious token reuse is logged |
| T-03 | Dictionary attacks login protection      | Attempt logins using common password dictionaries                              | Rate limiting, account lock or MFA challenge triggered |
| T-04 | Brute force login protection             | Repeated failed login attempts                                                 | Rate limiting, IP/user lock or MFA enforcement |
| T-05 | Privilege escalation                     | Call admin endpoints with non-admin token                                      | Operation blocked with 403 Forbidden |
| T-06 | Endpoint authentication enforcement      | Call protected endpoints with invalid/expired/stolen tokens                    | Request is rejected with 403 Forbidden |
| T-07 | Malicious file upload                    | Upload executable/scripts disguised as image/PDF                               | Upload is rejected or sanitized to a valid file type; no execution possible |
| T-08 | Oversized file upload                    | Upload files above allowed size limit                                          | Request is rejected with size limit error |
| T-09 | MIME type bypass                         | Upload file with mismatched extension and content                              | File is rejected after content validation |
| T-10 | Game key guessing attacks                | Attempt brute force activation keys                                            | Invalid keys rejected and rate limiting applied |
| T-11 | Order modification                       | Alter order data in request body                                               | Server validates and rejects tampered values |
| T-12 | Duplicate purchase abuse                 | Attempt repeated purchase of same game                                         | Duplicate purchase is blocked or safely handled |
| T-13 | Payment bypass attempt                   | Complete order without valid payment                                           | Order is rejected; payment is verified server-side |
| T-14 | Invoice ID enumeration                   | Request sequential/random invoice IDs                                          | Access denied for unauthorized invoices |
| T-15 | Path traversal attack                    | Use `../` or encoded traversal in file requests                                | Request is blocked; no unauthorized file access |
| T-16 | Unauthorized file access                 | Direct access to files without authentication                                  | Access denied (403 Forbidden) |
| T-17 | External dependency abuse                | Simulate API failure or timeout                                                | System degrades gracefully without crash |
| T-18 | Concurrent heavy operations              | Trigger multiple simultaneous operations                                       | System applies limits/queueing and stays responsive |
| T-19 | Verbose error exposure                   | Trigger exceptions/errors                                                      | No sensitive information exposed in responses or logs |
| T-20 | Token leakage via logs/URLs              | Pass tokens in query or inspect logs                                           | Token is not exposed or is rejected if misused |
| T-21 | Data or resource exposure of other users | Access other users’ resources                                                  | Only owner/admin access allowed |
| T-22 | HTTPS enforcement                        | Attempt HTTP downgrade                                                         | All traffic forced to HTTPS |
| T-23 | Security logging verification            | Perform sensitive actions                                                      | All actions are logged and traceable |
| T-24 | Authentication event logging             | Login/logout/failure events                                                    | All authentication events are recorded |