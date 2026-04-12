# Security Traceability Matrix v2

## 1. Purpose

This matrix links requirements, threats, abuse cases, ASVS references, planned tests, and evidence.

## 2. Matrix

| Requirement ID | Security Requirement | Threat IDs | Abuse Case IDs | ASVS Reference | Planned Test ID | Test Type | Technical Verification | Owner Role | Planned Evidence | Status | Sprint |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RNF-01 | Password hashing with BCrypt | TH-01 | AC-01 | v5.0.0-2.x | ST-001 | Unit + SAST | Verify no plaintext password storage and hashing config | Backend Owner | Unit output and code scan | Planned | S1 |
| RNF-02 | JWT authentication with expiration | TH-02 | AC-02 | v5.0.0-3.x | ST-002 | API Security | Validate token expiration and invalid token rejection | API Owner | API test evidence | Planned | S1 |
| RNF-03 | Protected endpoint authentication | TH-02 | AC-02 | v5.0.0-4.x | ST-003 | Access Control | Verify protected routes deny anonymous access | API Owner | Access matrix test output | Planned | S1 |
| RNF-04 | Role-based access control | TH-03 | AC-03 | v5.0.0-4.x | ST-004 | Authorization | Validate role isolation across admin/publisher/buyer | API Owner | Authorization test report | Planned | S1 |
| RNF-05 | HTTPS-only transport | TH-02 | AC-02 | v5.0.0-9.x | ST-005 | Config + Network | Verify TLS-only deployment and secure headers | Ops Owner | Environment config evidence | Planned | S1 |
| RNF-06 | Input validation and sanitization | TH-04 | AC-04 | v5.0.0-5.x | ST-006 | Negative API Tests | SQL injection payload rejection | Backend Owner | DAST output and logs | Planned | S1 |
| RNF-06 | Input validation and sanitization | TH-04 | AC-04 | v5.0.0-5.x | ST-007 | Negative API Tests | XSS payload neutralization/rejection | Backend Owner | Validation output | Planned | S1 |
| RNF-07 | Critical event logging | TH-09 | AC-03 | v5.0.0-8.x | ST-008 | Logging Verification | Confirm audit logs for login and role changes | Security Owner | Structured log samples | Planned | S1 |
| RNF-08 | Secure activation key generation | TH-07 | AC-07 | v5.0.0-6.x | ST-009 | Unit + Integration | Validate randomness and uniqueness expectations | Backend Owner | Unit/integration evidence | Planned | S1 |
| RNF-09 | Brute-force protection | TH-01 | AC-01 | v5.0.0-2.x | ST-010 | Abuse Simulation | Verify lockout/rate-limit behavior | API Owner | Simulation report | Planned | S1 |
| RNF-10 | MIME verification on upload | TH-05 | AC-05 | v5.0.0-5.x | ST-011 | Upload Security | Reject MIME/extension mismatch | Backend Owner | Upload test report | Planned | S1 |
| RNF-23 | Sensitive configuration management | TH-02, TH-07, TH-09 | AC-18 | v5.0.0-14.x | ST-016 | Config and Secrets Review | Verify secrets are not hardcoded and logs mask sensitive values | Security Owner | Config scan and log review evidence | Planned | S1 |
| RF-19 | Secure invoice download | TH-06 | AC-06 | v5.0.0-12.x | ST-012 | File Access Security | Block traversal payloads and enforce ownership | Backend Owner | Endpoint test output | Planned | S2 |
| RF-29 | Authenticated image upload | TH-05 | AC-05 | v5.0.0-15.x | ST-013 | Integration | Ensure only publisher uploads and valid content | Backend Owner | Integration evidence | Planned | S1 |
| RF-30 | File type and size validation | TH-05 | AC-05 | v5.0.0-15.x | ST-014 | Validation | Reject oversized and unsupported files | Backend Owner | Validation logs | Planned | S1 |
| RF-14 | Order integrity on creation | TH-08 | AC-08 | v5.0.0-11.x | ST-015 | Business Security | Verify server-side integrity and ownership checks | API Owner | Business test report | Planned | S2 |

## 3. Traceability Rules

1. Every security-relevant requirement must map to at least one planned test.
2. Every planned test must reference at least one threat or abuse case.
3. Every Critical or High risk must have at least one mapped control and one test.
4. Status transitions follow Planned, Ready, Executed, Failed, Mitigated.
