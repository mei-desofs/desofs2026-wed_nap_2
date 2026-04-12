# Mitigation Plan and Control Catalog

## 1. Objective

This document defines the mitigation strategy for the initial threat and risk baseline of ArcadeHaven.

## 2. Mitigation Strategy

Mitigations are prioritized by risk level:

- Critical and High: mandatory in Sprint 1 or early Sprint 2.
- Medium: implemented before release unless explicitly accepted.
- Low: tracked in backlog with periodic review.

## 3. Control Catalog

## 3.1 Authentication and Authorization Controls

| Control ID | Control | Mapped Threats | Mapped Risks | Mapped Requirements |
| --- | --- | --- | --- | --- |
| AC-001 | BCrypt password hashing policy | TH-01 | R-01 | RNF-01 |
| AC-002 | JWT expiration and validation hardening | TH-02 | R-02 | RNF-02 |
| AC-003 | Mandatory endpoint authentication | TH-02 | R-02 | RNF-03 |
| AC-004 | Role-based access control matrix | TH-03 | R-03 | RF-03, RF-04, RF-05, RNF-04 |
| AC-005 | HTTPS-only communication | TH-02 | R-02 | RNF-05 |
| AC-006 | Rate limiting and temporary account lockout | TH-01 | R-01 | RNF-09 |

## 3.2 Input Validation Controls

| Control ID | Control | Mapped Threats | Mapped Risks | Mapped Requirements |
| --- | --- | --- | --- | --- |
| IV-001 | Server-side input validation for all user fields | TH-04 | R-04 | RNF-06 |
| IV-002 | Parameterized persistence/query patterns | TH-04 | R-04 | RNF-06 |
| IV-003 | Output sanitization for user-provided content | TH-04 | R-04 | RNF-06 |

## 3.3 File Operation Controls

| Control ID | Control | Mapped Threats | Mapped Risks | Mapped Requirements |
| --- | --- | --- | --- | --- |
| FO-001 | MIME and extension verification | TH-05 | R-05 | RF-30, RNF-10 |
| FO-002 | File size limits and reject policy | TH-05 | R-05 | RF-29, RF-30 |
| FO-003 | Safe path handling for file retrieval | TH-06 | R-06 | RF-19, RF-27 |
| FO-004 | Access checks before invoice/key retrieval | TH-06, TH-07 | R-06, R-07 | RF-19, RF-23 |
| FO-005 | Generated file names independent of user input | TH-05, TH-06 | R-05, R-06 | RF-27, RF-28 |

## 3.4 Logging and Monitoring Controls

| Control ID | Control | Mapped Threats | Mapped Risks | Mapped Requirements |
| --- | --- | --- | --- | --- |
| LM-001 | Security event logging for login outcomes | TH-01, TH-09 | R-01, R-09 | RNF-07 |
| LM-002 | Audit logging for role changes and admin actions | TH-03, TH-09 | R-03, R-09 | RNF-07 |
| LM-003 | Access logging for invoice and key operations | TH-07, TH-09 | R-07, R-09 | RNF-07 |

## 3.5 Secrets and Configuration Controls

| Control ID | Control | Mapped Threats | Mapped Risks | Mapped Requirements |
| --- | --- | --- | --- | --- |
| SC-001 | Secrets only in environment variables | TH-02, TH-07 | R-02, R-07 | RNF-23 |
| SC-002 | No sensitive values in logs | TH-07, TH-09 | R-07, R-09 | RNF-07 |
| SC-003 | Secure activation key generation entropy | TH-07 | R-07 | RNF-08 |

## 3.6 Dependency and Container Controls

| Control ID | Control | Mapped Threats | Mapped Risks | Mapped Requirements |
| --- | --- | --- | --- | --- |
| DC-001 | Dependency scanning in CI | TH-02, TH-04 | R-02, R-04 | RNF-20 |
| DC-002 | Container image vulnerability scanning | TH-02 | R-02 | RNF-21, RNF-22 |
| DC-003 | SLA-based vulnerability remediation policy | TH-02, TH-04, TH-05 | R-02, R-04, R-05 | RNF-20 |

## 4. Control-to-Test Mapping

| Control ID | Planned Tests |
| --- | --- |
| AC-001 | ST-001 |
| AC-002, AC-003, AC-005 | ST-002, ST-003, ST-005 |
| AC-004 | ST-004 |
| AC-006 | ST-010 |
| IV-001, IV-002, IV-003 | ST-006, ST-007 |
| FO-001, FO-002 | ST-011, ST-013, ST-014 |
| FO-003, FO-004, FO-005 | ST-012 |
| LM-001, LM-002, LM-003 | ST-008 |
| SC-001, SC-002 | ST-016 |
| SC-003 | ST-009 |
| DC-001, DC-002, DC-003 | Security pipeline checks in Sprint 1 and Sprint 2 |

## 5. Implementation Priorities

1. Sprint 1 priority: AC-001 to AC-006, IV-001 to IV-003, FO-001 to FO-003, LM-001 to LM-002.
2. Sprint 2 priority: FO-004 to FO-005, LM-003, SC-001 to SC-003, DC-001 to DC-003 hardening.
3. Release gate: all controls mapped to Critical and High risks must be implemented and evidenced.
