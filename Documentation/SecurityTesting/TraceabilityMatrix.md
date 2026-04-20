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
| AC-01         | Inject Malicious Code            | Input Validation (RNF-10)                     | T-AC-01   |
| AC-02         | Hijack Account                   | JWT Authentication (RNF-02)                   | T-AC-02   |
| AC-03         | Dictionary Attack                | Brute Force Protection (RNF-16)               | T-AC-03   |
| AC-04         | Brute Force Login                | Brute Force Protection (RNF-16)               | T-AC-04   |
| AC-05         | JWT Token Theft                  | Token Revocation and Short Lifetime (RNF-03)  | T-AC-05   |
|               |                                  |                                               | T-ST-11   |
| AC-06 (A-BC)  | Privilege Escalation (Role Mgmt) | Role-Based Access Control (RNF-08)            | T-AC-06a  |
|               |                                  | Authorization Enforcement (RNF-18)            | T-ST-18   |
| AC-06 (G-MAC) | Privilege Escalation (Game Mgmt) | Role-Based Access Control (RNF-08)            | T-AC-06b  |
| AC-07         | JWT Token Theft (Protected Ops)  | Endpoint Authentication (RNF-04)              | T-AC-07   |
| AC-08         | Upload Malicious File            | File MIME Verification (RNF-22)               | T-AC-08   |
| AC-09         | Upload Oversized File            | Rate Limiting on Sensitive Endpoints (RNF-19) | T-AC-09   |
| AC-10         | Bypass MIME Verification         | File MIME Verification (RNF-22)               | T-AC-10   |
| AC-11         | Guess Game Key                   | Activation Key Generation (RNF-15)            | T-AC-11   |
| AC-12         | Modify Order                     | Authorization Enforcement (RNF-18)            | T-AC-12   |
| AC-13         | Force Duplicated Purchase        | Ownership Validation (RNF-07)                 | T-AC-13   |
| AC-14         | Bypass Payment                   | Authorization Enforcement (RNF-18)            | T-AC-14   |
| AC-15         | Invoice ID Enumeration           | Data Confidentiality (RNF-17)                 | T-AC-15   |
| AC-16         | Path Traversal Attack            | File Path Safety (RNF-21)                     | T-AC-16   |
|               |                                  |                                               | T-ST-06   |
| AC-17         | Unauthorized Invoice Access      | Secure File Storage (RNF-20)                  | T-AC-17   |
|               |                                  |                                               | T-ST-12   |

## 3. DFDs STRIDE Traceability Matrix

| STRIDE Category        | Threat (DFD)                           | Security Requirement                          | Test Case |
| ---------------------- | -------------------------------------- | --------------------------------------------- | --------- |
| Spoofing               | JWT not validated / impersonation      | JWT Authentication (RNF-02)                   | T-ST-01   |
|                        |                                        |                                               | T-AC-05   |
| Spoofing               | Credential brute force                 | Brute Force Protection (RNF-16)               | T-ST-02   |
|                        |                                        |                                               | T-AC-03   |
|                        |                                        |                                               | T-AC-04   |
| Tampering              | Request manipulation / mass assignment | Authorization Enforcement (RNF-18)            | T-ST-03   |
|                        |                                        |                                               | T-AC-12   |
| Tampering              | SQL Injection                          | Input Validation (RNF-10)                     | T-ST-04   |
|                        |                                        |                                               | T-AC-01   |
| Tampering              | JWT tampering                          | JWT Authentication (RNF-02)                   | T-ST-05   |
| Tampering              | Path traversal                         | File Path Safety (RNF-21)                     | T-ST-06   |
|                        |                                        |                                               | T-AC-16   |
| Tampering              | Malicious file upload                  | File MIME Verification (RNF-22)               | T-ST-07   |
|                        |                                        |                                               | T-AC-08   |
| Repudiation            | Missing audit logs                     | Critical Event Logging (RNF-13)               | T-ST-08   |
| Repudiation            | Missing auth logs                      | Security Monitoring (RNF-05)                  | T-ST-09   |
| Information Disclosure | Verbose errors / secrets exposure      | Error Responses (RNF-14)                      | T-ST-10   |
| Information Disclosure | Token leakage                          | External API Secret Protection (RNF-12)       | T-ST-11   |
|                        |                                        |                                               | T-AC-05   |
| Information Disclosure | Unauthorized file access               | Data Confidentiality (RNF-17)                 | T-ST-12   |
|                        |                                        |                                               | T-AC-17   |
| Information Disclosure | Data in transit exposure               | HTTPS Communication (RNF-09)                  | T-ST-13   |
| Denial of Service      | No rate limiting                       | Rate Limiting on Sensitive Endpoints (RNF-19) | T-ST-14   |
|                        |                                        |                                               | T-AC-03   |
| Denial of Service      | Resource exhaustion                    | Rate Limiting on Sensitive Endpoints (RNF-19) | T-ST-15   |
|                        |                                        |                                               | T-AC-09   |
| Denial of Service      | External dependency failure            | External Data Sanitization (RNF-11)           | T-ST-16   |
| Denial of Service      | Heavy processing abuse                 | Critical Event Logging (RNF-13)               | T-ST-17   |
| Elevation of Privilege | Missing role checks                    | Role-Based Access Control (RNF-08)            | T-ST-18   |
|                        |                                        |                                               | T-AC-06a  |
| Elevation of Privilege | Ownership bypass (IDOR)                | Ownership Validation (RNF-07)                 | T-ST-19   |
| Elevation of Privilege | Wrong JWT claims / stale roles         | Token Revocation and Short Lifetime (RNF-03)  | T-ST-20   |