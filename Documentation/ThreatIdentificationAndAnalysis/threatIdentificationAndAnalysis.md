# Threat Identification and Analysis

## 1. Objective

This document establishes the initial threat inventory for ArcadeHaven and defines how threats are identified, reviewed, and linked to security controls and planned tests.

## 2. Scope and Assets

In scope:

- Authentication and user management.
- Game management and uploads.
- Order, invoice, and activation key flows.
- Library management.
- API endpoints and data persistence.
- Server-side file operations.

Main assets:

- User credentials and JWT tokens.
- User profile data and role assignments.
- Order and payment-related data.
- Activation keys and invoices.
- Uploaded media files.
- Audit logs and security events.

## 3. Threat Identification Method

Threats are identified using STRIDE over the current architecture and trust boundaries:

- S: Spoofing identity.
- T: Tampering with data.
- R: Repudiation.
- I: Information disclosure.
- D: Denial of service.
- E: Elevation of privilege.

## 4. Initial Threat Register

| Threat ID | STRIDE | Description | Affected Assets | Related Abuse Case | Related Requirements |
| --- | --- | --- | --- | --- | --- |
| TH-01 | S, D | Brute-force against login endpoint | Credentials, accounts | AC-01 | RF-02, RNF-09 |
| TH-02 | S, I | JWT theft and replay | JWT tokens, protected APIs | AC-02 | RNF-02, RNF-03, RNF-05 |
| TH-03 | E | Privilege escalation to admin routes | Role assignments, admin endpoints | AC-03 | RF-03, RF-04, RF-05, RNF-04 |
| TH-04 | T, I | SQL injection in search/filter endpoints | Game and user data | AC-04 | RF-11, RNF-06 |
| TH-05 | T, I | Malicious upload bypassing MIME/type checks | File storage, consumers of files | AC-05 | RF-29, RF-30, RNF-10 |
| TH-06 | T, I | Path traversal on invoice download | Invoice files, server filesystem | AC-06 | RF-19, RF-27 |
| TH-07 | I | Activation key exposure and leakage | Activation keys, library assets | AC-07 | RF-23, RF-28, RNF-08 |
| TH-08 | T | Order tampering and ownership bypass | Order integrity, invoice generation | AC-08 | RF-14, RF-20 |
| TH-09 | R | Missing evidence for privileged actions | Audit logs and accountability | AC-03, AC-09, AC-18 | RNF-07, RNF-23 |

## 5. Threat Model Review Process

### 5.1 Trigger Events

Threat model review is required when:

- New endpoint is introduced.
- Existing endpoint behavior changes.
- Authentication or role policy changes.
- New external integration is added.
- User-triggered file operation is added or modified.

### 5.2 Review Steps

1. Identify changed components and trust boundaries.
2. Re-evaluate STRIDE threats for impacted elements.
3. Update risk scoring in the risk register.
4. Confirm mitigation status and residual risk.
5. Update planned tests and traceability matrix.
6. Record reviewer decision.

### 5.3 Review Outcomes

Each review must produce:

- Updated threat register entries.
- Updated risk assessment entries.
- Updated security testing traceability links.
- List of required mitigation actions.

## 6. Current Gaps to Address Next

- Add endpoint-level threat granularity for all modules.
- Add threat-tree breakdown for high-risk threats.
- Record threat review decisions with owner and due date.
