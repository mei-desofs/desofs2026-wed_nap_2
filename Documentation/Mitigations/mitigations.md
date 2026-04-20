# Mitigations

## 1. Objective

Mitigations are security controls and measures designed to reduce the impact of identified threats and vulnerabilities within a system. They define the rules to be followed and actions to be implemented within the system, so as to enforce proper security policies and to ensure the integrity, confidentiality and availability of the sytem.

This document defines the mitigation strategy for the ArcadeHaven system. As of this iteration of the project, the mitigations are currently being applied to the following elements:
- Abuse cases (referenced [here](../ThreatModeling/AbuseCases/AbuseCases.md))
- Dataflow Diagram STRIDES (referenced [here](../Architecture/Dataflow/arcadehaven-dfd.pdf))

These mitigations aim to provide a secure wway to address the identified security concerns within the ArcadeHaven system by providing a structured and traceable aproach to reducing the direct impact of these threats to the system.


## 2. Abuse Case Mitigation

| Mitigation                            | Description                                                                                     | Related Abuse Case                  |
| ------------------------------------- | ----------------------------------------------------------------------------------------------- | ----------------------------------- |
| **Input Sanitization**                | All user inputs must be validated and sanitized before being processed to prevent injection attacks. | Inject Malicious Code (AC-01)  |
| **Multi-Factor Authentication (MFA)** | Add an additional authentication factor to prevent unauthorized account access                  | Hijack Account (AC-02)              |
|                                       |                                                                                                 | Dictionary Attack (AC-03)           |
|                                       |                                                                                                 | Brute Force Login (AC-04)           |
| **Rate Limiting and Account Lockout** | Limits repeated login attempts to prevent automated attacks                                     | Brute Force Login (AC-04)           |
|                                       |                                                                                                 | Dictionary Attack (AC-03)           |
| **Secure Token Management**           | Configure short-lived tokens and secure storage to prevent token reuse                          | JWT Token Theft (AC-05 and AC-07)   |
| **Endpoint Authentication**           | Ensure all protected endpoints implement authentication validation through the use of the tokens | JWT Token Theft (AC-07)            |
|                                       |                                                                                                 | Modify Order (AC-12)                |
| **Role-Based Access Control (RBAC)**  | Restrict actions and operations to specific roles so as to mitigate privilege escalation        | Privilege Escalation                |
| **Ownership Validation**              | Ensure owned resources can only be accessed by their owners                                     | Modify Order (AC-12)                |
|                                       |                                                                                                 | Unauthorized Invoice Access (AC-17) |
|                                       |                                                                                                 | Invoice ID Enumeration (AC-15)      |
|                                       |                                                                                                 | Invoice ID Enumeration (AC-15)      |
| **File Name Sanitization**            | Prevent malicious file naming and path manipulation                                             | Upload Malicious File (AC-08)       |
| **File Size Validation**              | Restrict file sizes to prevent resource abuse and mitigate malicious resource injections        | Upload Oversized File (AC-09)       |
| **MIME Type Verification**            | Validate actual file type to prevent malicious file uploads                                     | Bypass MIME-Verification (AC-10)    |
|                                       |                                                                                                 | Upload Malicious File (AC-08)       |
| **Server-Side Validation**            | Ensure all operations are validated on backend to prevent tampering and previlege escalation    | Modify Order (AC-12)                |
|                                       |                                                                                                 | Force Duplicated Purchase (AC-13)   |
|                                       |                                                                                                 | Bypass Payment (AC-14)              |
| **Duplicate Purchase Check**          | Prevent purchasing the same game multiple times                                                 | Force Duplicated Purchase (AC-13)   |
| **Payment Validation**                | Ensure order completion requires valid payment confirmation                                     | Bypass Payment (AC-14)              |
| **Secure Key Generation**             | Generate unpredictable activation keys and securely storage them to prevent guessing            | Guess Game Key (AC-11)              |
| **Authorization Check**               | Ensure authorization and access to sensitive resources is validated                             | Invoice ID Enumeration (AC-15)      |
|                                       |                                                                                                 | Unauthorized Invoice Access (AC-17) |
| **Secure File Storage**               | Store files outside public access and implemen required authentication to retrieve them         | Path Traversal Attack (AC-16)       |
|                                       |                                                                                                 | Unauthorized Invoice Access (AC-17) |
| **Path Validation**                   | Prevent directory traversal attacks by sanitizing file paths                                    | Path Traversal Attack (AC-16)       |



## 3. Stride Mitigations

| STRIDE Category            | Mitigation                         | Description                                                                                      |
| -------------------------- | ---------------------------------- | ------------------------------------------------------------------------------------------------ |
| **Spoofing**               | Multi-Factor Authentication (MFA)  | Enforce Keycloack MFA to prevent identity impersonation using stolen credentials                 |
|                            | Strong Password Policies           | Require complex passwords and detect breached credentials to reduce account takeover risk        |
|                            | JWT Validation and Verification    | Validate token signature, issuer, audience, and expiry on requests                               |
|                            | Trusted Token Issuer Configuration | Only accept tokens from a trusted identity provider                                              |
|                            | Secure Session Binding             | Bind sessions to device/IP and use short-lived tokens to prevent reuse                           |
|                            | Credential Protection              | Avoid logging credentials or tokens and ensure secure storage and transmission                   |
| **Tampering**              | Input Validation and Sanitization  | Validate and sanitize all inputs                                                                 |
|                            | Server-Side Enforcement            | Never trust client input and compute critical values server-side                                 |
|                            | Parameterized Queries              | Prevent SQL injection using prepared statements or ORM bindings                                  |
|                            | File Path Validation               | Use canonical paths and UUID filenames to prevent path traversal attacks                         |
|                            | Secure File Upload Handling        | Validate file type via magic bytes, scan for malware, restrict executable files                  |
|                            | Data Integrity Checks              | Ensure integrity of generated artifacts                                                          |
| **Repudiation**            | Centralized Audit Logging          | Log all critical actions with timestamps and user IDs                                            |
|                            | Immutable Logs / SIEM              | Store logs in tamper-evident systems for forensic traceability                                   |
|                            | Cross-System Correlation           | Propagate session IDs across services to correlate logs                                          |
|                            | Action Attribution                 | Track who performed database, file and system actions                                            |
|                            | Non-Repudiation Records            | Store signed or verifiable transaction records                                                   |
| **Information Disclosure** | Encryption in Transit (TLS)        | Enforce HTTPS, HSTS, and disable plaintext HTTP to prevent interception                          |
|                            | Encryption at Rest                 | Encrypt sensitive data                                                                           |
|                            | Least Privilege Access             | Restrict access based on roles and ownership                                                     |
|                            | Secure Error Handling              | Avoid leaking stack traces or internal system details in responses                               |
|                            | Secure Token Handling              | Never expose tokens in URLs, logs, or client storage                                             |
|                            | Secrets Management                 | Store credentials in environment variables or secret managers                                    |
|                            | Secure File Access                 | Serve sensitive files via authenticated endpoints only                                           |
|                            | Data Sanitization (External APIs)  | Treat external API data as untrusted and sanitize it before use                                  |
| **Denial of Service**      | Rate Limiting                      | Limit requests per IP/user                                                                       |
|                            | Resource Limits                    | Enforce file size limits, pagination, and query limits                                           |
|                            | Connection and Timeout Controls    | Use reverse proxy with timeouts and connection limits                                            |
|                            | Caching and Circuit Breakers       | Cache external dependencies and degrade gracefully                                               |
|                            | Asynchronous Processing            | Offload heavy tasks to background workers                                                        |
|                            | Monitoring and Alerting            | Detect abnormal usage patterns and trigger alerts                                                |
|                            | High Availability (HA)             | Deploy critical services redundantly                                                             |
| **Elevation of Privilege** | Role-Based Access Control (RBAC)   | Enforce roles at endpoint and method level                                                       |
|                            | Ownership Validation               | Ensure users can only access their own resources                                                 |
|                            | Strict Authorization Checks        | Validate permissions on every request, not just authentication                                   |
|                            | Separation of Duties               | Separate admin vs user operations                                                                |
|                            | Secure Role Assignment             | Restrict admin interfaces and protect role assignment mechanisms                                 |
|                            | Token Claim Validation             | Ensure roles are extracted from trusted JWT claims only                                          |
