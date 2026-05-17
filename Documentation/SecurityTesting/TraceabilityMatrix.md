[← Back to index page](../Overview/overview.md)

# Security Traceability Matrix v2

## 1. Purpose

This matrix links requirements, threats, abuse cases, ASVS references and planned tests. This matrix defines the matrix for:
- **Abuse Cases**
- **DFDs STRIDE**

The documents supporting and referenced by these traceability matrices can be accessed bellow:
- **[Abuse Cases](../ThreatModeling/AbuseCases/AbuseCases.md)**
- **[DFDs STRIDES](../Architecture/Dataflow/arcadehaven-dfd.pdf)**
- **[Security Tests](./securityTesting.md)**

## 2. Abuse Cases Traceability Matrix

| Abuse Case ID | Abuse Case                       | Security Requirement                          | Test Case |
| ------------- | -------------------------------- | --------------------------------------------- | --------- |
| AC-01         | Inject Malicious Code            | Input Validation (RNF-10)                     | T-01      |
| AC-02         | Hijack Account                   | JWT Authentication (RNF-02)                   | T-02      |
| AC-03         | Dictionary Attack                | Brute Force Protection (RNF-16)               | T-03      |
| AC-04         | Brute Force Login                | Brute Force Protection (RNF-16)               | T-04      |
| AC-05         | JWT Token Theft                  | Token Revocation and Short Lifetime (RNF-03)  | T-05      |
|               |                                  |                                               | T-11      |
| AC-06 (A-BC)  | Privilege Escalation (Role Mgmt) | Role-Based Access Control (RNF-08)            | T-06      |
|               |                                  | Authorization Enforcement (RNF-18)            | T-18      |
| AC-06 (G-MAC) | Privilege Escalation (Game Mgmt) | Role-Based Access Control (RNF-08)            | T-07      |
| AC-07         | JWT Token Theft (Protected Ops)  | Endpoint Authentication (RNF-04)              | T-08      |
| AC-08         | Upload Malicious File            | File MIME Verification (RNF-22)               | T-09      |
| AC-09         | Upload Oversized File            | Rate Limiting on Sensitive Endpoints (RNF-19) | T-10      |
| AC-10         | Bypass MIME Verification         | File MIME Verification (RNF-22)               | T-11      |
| AC-11         | Guess Game Key                   | Activation Key Generation (RNF-15)            | T-12      |
| AC-12         | Modify Order                     | Authorization Enforcement (RNF-18)            | T-13      |
| AC-13         | Force Duplicated Purchase        | Ownership Validation (RNF-07)                 | T-14      |
| AC-14         | Bypass Payment                   | Authorization Enforcement (RNF-18)            | T-15      |
| AC-15         | Invoice ID Enumeration           | Data Confidentiality (RNF-17)                 | T-16      |
| AC-16         | Path Traversal Attack            | File Path Safety (RNF-21)                     | T-17      |
|               |                                  |                                               | T-06      |
| AC-17         | Unauthorized Invoice Access      | Secure File Storage (RNF-20)                  | T-18      |
|               |                                  |                                               | T-12      |


## 3. DFDs STRIDE Traceability Matrix

| STRIDE Category        | Threat (DFD)                           | Security Requirement                          | Test Case |
| ---------------------- | -------------------------------------- | --------------------------------------------- | --------- |
| Spoofing               | JWT not validated / impersonation      | JWT Authentication (RNF-02)                   | T-01      |
|                        |                                        |                                               | T-05      |
| Spoofing               | Credential brute force                 | Brute Force Protection (RNF-16)               | T-02      |
|                        |                                        |                                               | T-03      |
|                        |                                        |                                               | T-04      |
| Tampering              | Request manipulation / mass assignment | Authorization Enforcement (RNF-18)            | T-03      |
|                        |                                        |                                               | T-13      |
| Tampering              | SQL Injection                          | Input Validation (RNF-10)                     | T-04      |
|                        |                                        |                                               | T-01      |
| Tampering              | JWT tampering                          | JWT Authentication (RNF-02)                   | T-05      |
| Tampering              | Path traversal                         | File Path Safety (RNF-21)                     | T-06      |
|                        |                                        |                                               | T-17      |
| Tampering              | Malicious file upload                  | File MIME Verification (RNF-22)               | T-07      |
|                        |                                        |                                               | T-09      |
| Repudiation            | Missing audit logs                     | Critical Event Logging (RNF-13)               | T-08      |
| Repudiation            | Missing auth logs                      | Security Monitoring (RNF-05)                  | T-09      |
| Information Disclosure | Verbose errors / secrets exposure      | Error Responses (RNF-14)                      | T-10      |
| Information Disclosure | Token leakage                          | External API Secret Protection (RNF-12)       | T-11      |
|                        |                                        |                                               | T-05      |
| Information Disclosure | Unauthorized file access               | Data Confidentiality (RNF-17)                 | T-12      |
|                        |                                        |                                               | T-18      |
| Information Disclosure | Data in transit exposure               | HTTPS Communication (RNF-09)                  | T-13      |
| Denial of Service      | No rate limiting                       | Rate Limiting on Sensitive Endpoints (RNF-19) | T-14      |
|                        |                                        |                                               | T-03      |
| Denial of Service      | Resource exhaustion                    | Rate Limiting on Sensitive Endpoints (RNF-19) | T-15      |
|                        |                                        |                                               | T-10      |
| Denial of Service      | External dependency failure            | External Data Sanitization (RNF-11)           | T-16      |
| Denial of Service      | Heavy processing abuse                 | Critical Event Logging (RNF-13)               | T-17      |
| Elevation of Privilege | Missing role checks                    | Role-Based Access Control (RNF-08)            | T-18      |
|                        |                                        |                                               | T-06      |
| Elevation of Privilege | Ownership bypass (IDOR)                | Ownership Validation (RNF-07)                 | T-19      |
| Elevation of Privilege | Wrong JWT claims / stale roles         | Token Revocation and Short Lifetime (RNF-03)  | T-20      |
