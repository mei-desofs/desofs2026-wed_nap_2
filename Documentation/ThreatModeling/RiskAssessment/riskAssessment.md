# Risk Assessment

## 1. Objective

This document defines the complete risk assessment for ArcadeHaven. It covers the threat group reference, risk treatment plan, post-mitigation residual risk analysis, and the release acceptance policy.

## 2. Threat Group Reference

TH-01 to TH-09 are planning-level threat groups used in the treatment plan and residual risk tables below. Each group aggregates related individual threats from the OWASP Threat Dragon catalogue.

For per-threat DREAD scores (all 76 threats, each anchored to CWE, OWASP Top 10 2021, and CVSS 3.1), see [DREAD Analysis — Individual Threat Scoring](../../SSDLC/DREAD.md#individual-threat-dread-scoring).

| Threat Group | Description | STRIDE |
| --- | --- | --- |
| TH-01 | Brute-force login | S, D |
| TH-02 | JWT theft and replay | S, I |
| TH-03 | Privilege escalation | E |
| TH-04 | SQL injection and malicious input | T, I |
| TH-05 | Malicious file upload | T, I |
| TH-06 | Path traversal in file retrieval | T, I |
| TH-07 | Activation key disclosure | I |
| TH-08 | Order tampering | T |
| TH-09 | Missing critical event evidence | R |

## 3. Risk Treatment Plan

| Threat ID | Planned Controls | Linked Planned Tests | Owner Role | Status |
| --- | --- | --- | --- | --- |
| TH-01 | AC-001 (BCrypt), AC-006 (rate limit + lockout), LM-001 (structured logging) | ST-010 | Security Owner | Planned |
| TH-02 | AC-002 (JWT expiry), AC-003 (endpoint auth), AC-005 (HTTPS), SC-001 (encryption) | ST-002, ST-003, ST-005 | API Owner | Planned |
| TH-03 | AC-004 (RBAC matrix), LM-002 (auth/authz event logging) | ST-004, ST-008 | API Owner | Planned |
| TH-04 | IV-001 (whitelist validation), IV-002 (parameterized queries), IV-003 (output encoding), DC-001 (dependency hygiene) | ST-006, ST-007 | Backend Owner | Planned |
| TH-05 | FO-001 (MIME check), FO-002 (extension check), FO-005 (size limit) | ST-011, ST-013, ST-014 | Backend Owner | Planned |
| TH-06 | FO-003 (safe path handling), FO-004 (access ownership check) | ST-012 | Backend Owner | Planned |
| TH-07 | FO-004 (access ownership check), SC-003 (key visibility restriction), SC-002 (no secrets in logs) | ST-009 | Backend Owner | Planned |
| TH-08 | AC-004 (RBAC), IV-001 (server-side validation) | ST-015 | API Owner | Planned |
| TH-09 | LM-001 (structured logging), LM-002 (auth/authz logging), LM-003 (security event logging), SC-002 (no secrets in logs) | ST-008 | Security Owner | Planned |

## 4. Residual Risk Analysis

Expected risk levels after planned mitigations are implemented and verified. Residual DREAD averages are estimates pending Sprint 1 and Sprint 2 test results.

| Threat ID | Initial DREAD | Planned Mitigations | Expected Residual DREAD | Residual Level | Acceptance Condition |
| --- | --- | --- | --- | --- | --- |
| TH-01 | 8.6 | AC-006, AC-001, LM-001 | 5.8 | Medium | Rate limit and lockout verified by ST-010 |
| TH-02 | 7.6 | AC-002, AC-003, AC-005, SC-001 | 5.9 | Medium | Token hardening and HTTPS validated |
| TH-03 | 8.0 | AC-004, LM-002 | 5.7 | Medium | Authorization matrix fully enforced |
| TH-04 | 8.4 | IV-001, IV-002, IV-003, DC-001 | 6.0 | Medium | Injection payload tests pass |
| TH-05 | 7.6 | FO-001, FO-002, FO-005 | 5.6 | Medium | Upload security tests pass |
| TH-06 | 6.8 | FO-003, FO-004 | 4.9 | Medium | Traversal and ownership tests pass |
| TH-07 | 6.6 | FO-004, SC-003, SC-002 | 4.8 | Medium | Key exposure paths closed |
| TH-08 | 7.0 | AC-004, IV-001 | 5.2 | Medium | Order integrity tests pass |
| TH-09 | 7.2 | LM-001, LM-002, LM-003, SC-002 | 4.9 | Medium | Audit coverage validated |

Post-mitigation target: all threats reduced to Medium or below. No Critical or High residual risk accepted at release without explicit team approval.

## 5. Review and Update Policy

- Risk assessment is reviewed at the beginning and end of each sprint.
- Any threat-model change must trigger re-scoring in [DREAD Analysis](../../SSDLC/DREAD.md).
- Critical and High risks cannot be marked accepted without explicit team approval and documented rationale.
