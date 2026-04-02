# ArcadeHaven Security Testing Plan

## 1. Purpose and Scope

This document defines the Phase 1 security testing plan for ArcadeHaven. The plan is focused on:

- Defining the security testing methodology.
- Using abuse cases as the basis for test design.
- Documenting the threat model review process.
- Ensuring traceability between security requirements and planned tests.

This phase includes planning and traceability only. Test execution will occur during Sprint 1 and Sprint 2.

This document is self-contained; the threat model review workflow and phase roadmap are included inline.

## 2. Security Testing Methodology

The methodology combines:

- OWASP Web Security Testing Guide as the main testing reference.
- OWASP ASVS as the verification baseline for controls.
- Risk-based prioritization using STRIDE and DREAD.

### 2.1 Testing Layers

1. Requirements and design security review.
2. Static analysis and dependency analysis.
3. API security testing.
4. Authentication and authorization testing.
5. Input validation and file operation testing.
6. Logging and security monitoring validation.

### 2.2 Planned Techniques and Tools

| Test Type | Planned Technique | Planned Tooling | Main Output |
| --- | --- | --- | --- |
| SAST | Source code and rule-based analysis | SonarCloud, CodeQL | Findings and severity report |
| SCA | Dependency vulnerability analysis | OWASP Dependency-Check, Snyk, Dependabot | CVE report and fix backlog |
| DAST | Runtime API probing and attack simulation | OWASP ZAP | DAST report |
| Auth/Authz | Manual and automated scenario testing | Postman/Newman, API tests | Access-control validation results |
| Input and file validation | Negative testing with malicious payloads | Integration tests, request collections | Validation and rejection evidence |
| Logging and auditability | Event generation and verification | Application logs, SIEM-ready logs | Audit trail evidence |

### 2.3 Entry and Exit Criteria

Entry criteria:

- Security requirements are approved.
- Abuse cases are documented and linked to requirements.
- Threat model baseline is published.

Exit criteria:

- Each mapped security requirement has at least one planned test.
- High-risk abuse cases are covered by planned tests.
- Threat model review checklist is completed.

## 3. Abuse-Case Driven Testing Strategy

The seed abuse-case model covers authentication and authorization. The testing plan expands coverage to game, order, library, and file-operation flows.

Abuse-case diagram references:

- Abuse-case catalog: [../AbuseCases/abuse-cases-expanded.md](../AbuseCases/abuse-cases-expanded.md)
- Auth diagram: [../AbuseCases/auth-abuse-case.puml](../AbuseCases/auth-abuse-case.puml)
- Game diagram: [../AbuseCases/game-abuse-case.puml](../AbuseCases/game-abuse-case.puml)
- Orders and library diagram: [../AbuseCases/orders-library-abuse-case.puml](../AbuseCases/orders-library-abuse-case.puml)
- File operations diagram: [../AbuseCases/file-operations-abuse-case.puml](../AbuseCases/file-operations-abuse-case.puml)

### 3.1 Abuse Cases for Security Planning

The table below lists all 20 abuse cases used as the baseline for this plan.

| Abuse Case ID | Abuse Case | Target Flow | Related Requirements |
| --- | --- | --- | --- |
| AC-01 | Brute Force Login | Login | RF-02, RNF-09 |
| AC-02 | JWT Token Theft/Reuse | Authenticated API access | RF-02, RNF-02, RNF-03, RNF-05 |
| AC-03 | Privilege Escalation | Role changes and admin actions | RF-03, RF-04, RF-05, RNF-04 |
| AC-04 | SQL Injection in search/filter | Search and game listing | RF-11, RNF-06 |
| AC-05 | Malicious file upload | Image upload | RF-29, RF-30, RNF-10 |
| AC-06 | Path traversal on invoice download | Invoice retrieval | RF-19, RF-27 |
| AC-07 | Activation key disclosure | Library and key visualization | RF-23, RF-28, RNF-08 |
| AC-08 | Order tampering | Order create and cancel | RF-14, RF-20 |
| AC-09 | Username enumeration during login errors | Login error response | RF-02, RNF-07, RNF-09 |
| AC-10 | Password reset abuse and token guessing | Password reset flow | RF-02, RNF-02 |
| AC-11 | Forced browsing to publisher-only routes | Publisher game routes | RF-07, RF-08, RNF-04 |
| AC-12 | Forced browsing to admin approval endpoints | Admin game approval | RF-09, RNF-04 |
| AC-13 | Mass assignment in profile updates | Profile update endpoint | RF-06, RNF-06 |
| AC-14 | Stored XSS via game description fields | Game create/edit form | RF-07, RF-08, RNF-06 |
| AC-15 | Duplicate order race condition abuse | Order creation | RF-14, RF-15 |
| AC-16 | Unauthorized order cancellation | Order cancel endpoint | RF-20, RNF-04 |
| AC-17 | Unauthorized library entry revocation | Library management | RF-25, RNF-04 |
| AC-18 | Exposure of sensitive logs with tokens/passwords | Logging output | RNF-07, RNF-23 |
| AC-19 | Abuse of RAWG integration for payload pollution | External API enrichment | RF-12, RNF-06, RNF-16 |
| AC-20 | Abuse of startup directory creation with unsafe paths | File system initialization | RF-26, RF-27 |

### 3.2 Abuse-Case Coverage Rule

Each abuse case must have:

1. A mapped threat category.
2. At least one planned security test.
3. Expected security control and evidence type.

### 3.3 Domain Coverage Summary

| Domain | Count | Abuse Case IDs | Primary Threat IDs |
| --- | --- | --- | --- |
| Authentication | 4 | AC-01, AC-02, AC-09, AC-10 | TH-01, TH-02 |
| Authorization | 4 | AC-03, AC-11, AC-12, AC-17 | TH-03, TH-09 |
| Input Validation and Injection | 4 | AC-04, AC-13, AC-14, AC-19 | TH-04 |
| File and Storage Operations | 4 | AC-05, AC-06, AC-07, AC-20 | TH-05, TH-06, TH-07 |
| Orders and Business Logic | 3 | AC-08, AC-15, AC-16 | TH-08 |
| Logging and Secrets | 1 | AC-18 | TH-07, TH-09 |

## 4. Threat Model Review Process

### 4.1 Review Triggers

Threat model review is mandatory when any of the following occurs:

- New endpoint or exposed API contract change.
- Authentication or authorization rule change.
- Data flow or trust boundary change.
- New integration with external systems.
- New file-system operation triggered by user input.

### 4.2 Roles

- Threat Model Owner: maintains threat inventory and updates mappings.
- Security Reviewer: validates STRIDE classification and risk scoring quality.
- Technical Owner: confirms feasibility and implementation details.
- Approver: confirms acceptance of risk and planned actions.

### 4.3 Review Cadence

- Sprint planning: review planned changes and expected threats.
- Sprint end: validate implemented controls and update residual risk.
- Before release: perform final review of high-risk threats.

### 4.4 Review Workflow Steps

1. Change detection and review request.
2. Identify impacted assets and trust boundaries.
3. Re-run STRIDE classification for impacted scope.
4. Update DREAD scores in risk register.
5. Add or update mitigations.
6. Update traceability matrix with new tests.
7. Record approval decision and pending actions.

### 4.5 SLA and Outputs

- Review SLA: 3 business days after request.
- Outputs:
  - Updated threat entries.
  - Updated risk and mitigation mappings.
  - Updated planned tests and traceability links.
  - Open actions with owner and due date.

### 4.6 Quality Checklist

- Scope includes all changed trust boundaries.
- No new endpoint remains without threat classification.
- All Critical and High risks have treatment and planned tests.
- Risk acceptance is explicit and owner-approved.

## 5. Security Requirement to Planned Test Traceability Matrix

For the expanded matrix with ASVS references, owner roles, and sprint allocation, see [traceability-matrix-v2.md](traceability-matrix-v2.md). For ASVS-specific assessments see [ASVS/V16_Logging-ErrorHandling.md](ASVS/V16_Logging-ErrorHandling.md) and [ASVS/V17_Communications.md](ASVS/V17_Communications.md).

| Requirement ID | Security Requirement | Abuse Case | Planned Test ID | Test Focus | Planned Evidence | Status |
| --- | --- | --- | --- | --- | --- | --- |
| RNF-01 | Password Storage with BCrypt | AC-01 | ST-001 | Hashing policy and no plaintext storage | Code scan result and unit test output | Planned |
| RNF-02 | JWT authentication with expiration | AC-02 | ST-002 | Token expiration and invalid token rejection | API test report | Planned |
| RNF-03 | Endpoint authentication enforcement | AC-02 | ST-003 | Anonymous access denied on protected routes | Access-control test log | Planned |
| RNF-04 | Role-based access control | AC-03 | ST-004 | Role isolation for admin and publisher routes | Authorization matrix test result | Planned |
| RNF-05 | HTTPS-only communication | AC-02 | ST-005 | Insecure transport rejection and TLS enforcement | Environment and gateway config evidence | Planned |
| RNF-06 | Input validation and sanitization | AC-04 | ST-006 | SQL injection payload rejection | Negative test run output | Planned |
| RNF-06 | Input validation and sanitization | AC-04 | ST-007 | XSS payload neutralization/rejection | Validation test output | Planned |
| RNF-07 | Critical event logging | AC-03 | ST-008 | Log generation for login and role-change events | Structured log samples | Planned |
| RNF-08 | Secure activation key generation | AC-07 | ST-009 | Entropy and uniqueness checks for keys | Unit/integration test evidence | Planned |
| RNF-09 | Brute-force protection | AC-01 | ST-010 | Rate limiting and temporary lockout behavior | API attack simulation report | Planned |
| RNF-10 | MIME verification on uploads | AC-05 | ST-011 | Extension/MIME mismatch rejection | Upload validation report | Planned |
| RNF-23 | Sensitive configuration management | AC-18 | ST-016 | Verify secrets are externalized and masked in logs | Config and log review evidence | Planned |
| RF-19 | Download invoice | AC-06 | ST-012 | Authorization and path traversal prevention | Endpoint security test output | Planned |
| RF-29 | Upload game images | AC-05 | ST-013 | Authenticated-only upload and file constraints | Integration test report | Planned |
| RF-30 | Validate uploaded files | AC-05 | ST-014 | Oversized and unsupported-type rejection | Validation evidence | Planned |
| RF-14 | Create order | AC-08 | ST-015 | Server-side ownership and integrity checks | Business-security test report | Planned |

## 6. Sprint and Phase Roadmap

### 6.1 Phase 1 (Current — Planning)

- Define methodology and test categories.
- Link abuse cases to requirements.
- Define threat model review process.
- Build initial traceability matrix.
- Deliverable: this security testing plan document.

Success criteria:
- No security requirement left without a planned test.
- All high-risk abuse cases mapped to at least one test.
- Threat model review process documented and approved.

### 6.2 Sprint 1 (Execution Start)

- Implement and execute ST-001 to ST-011 and ST-016.
- Run SAST, SCA, and first DAST pass.
- Open remediation backlog for findings.
- Define evidence templates per test (ST-001 to ST-016).
- Activate findings triage and SLA workflow.
- Use threat model review checklist for each PR with security impact.

Success criteria:
- All Sprint 1 tests executed and evidenced.
- No unresolved Critical risk at Sprint 2 gate.

### 6.3 Sprint 2 (Execution Close)

- Implement and execute remaining tests (ST-012 to ST-015).
- Re-test mitigations and close high-risk findings.
- Re-score DREAD with residual-risk updates after Sprint 1 findings.
- Validate ASVS checklist status to Verified where applicable.
- Produce final security testing evidence bundle.
- Produce release decision record with accepted residual risks.

Success criteria:
- Release gate rule satisfied.
- Residual-risk acceptance documented and approved.
- Final evidence bundle complete and referenced in deliverable.

## 7. Evidence and Findings Governance

### 7.1 Finding Severity and SLA

| Severity | Required Action | SLA |
| --- | --- | --- |
| Critical | Fix before release — blocks release gate | Immediate |
| High | Fix within current sprint or document accepted risk | Same sprint |
| Medium | Backlog with assigned owner and due date | Next sprint |
| Low | Document as accepted or defer with justification | Release |

### 7.2 Evidence Format

Each executed test must record:

- Test ID (ST-001 to ST-016).
- Date executed.
- Executor name.
- Tool and method used.
- Result: pass, fail, or finding with description.
- Artifact reference (log file, scan report, screenshot, test output).

### 7.3 Release Gate Rule

No Critical-severity finding may remain unresolved at release. High-severity findings require either a fix or a documented risk acceptance approved by the Security Reviewer. The release decision record must list all accepted residual risks with owner sign-off.

## 8. Completion Checklist

- Methodology section approved.
- Abuse-case mapping approved (20 abuse cases, 6 domains covered).
- Threat model review workflow approved.
- Traceability matrix approved.
- No security requirement left without a planned test.
- Evidence format defined for each planned test (ST-001 to ST-016).
- Release gate rule documented and accepted by team.
