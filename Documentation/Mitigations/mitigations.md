[← Back to index page](../Overview/overview.md)

# Mitigations

## 1. Objective

Mitigations are security controls and measures designed to reduce the impact of identified threats and vulnerabilities within a system. They define the rules to be followed and actions to be implemented within the system, so as to enforce proper security policies and to ensure the integrity, confidentiality and availability of the sytem.

This document defines the mitigation strategy for the ArcadeHaven system. As of this iteration of the project, the mitigations are currently being applied to the following elements:
- Abuse cases (referenced [here](../ThreatModeling/AbuseCases/AbuseCases.md))
- Dataflow Diagram STRIDES (referenced [here](../Architecture/Dataflow/arcadehaven-dfd.pdf))

These mitigations aim to provide a secure wway to address the identified security concerns within the ArcadeHaven system by providing a structured and traceable aproach to reducing the direct impact of these threats to the system.

## 2. Mitigations Priority

| **Level**     | **Description**                                                                                                  |
| ------------- | ---------------------------------------------------------------------------------------------------------------- |
| **Low**       | Mitigation with minimal security impact. Can be implemented later without significantly increasing risk.         |
| **Medium**    | Important mitigation that reduces risks, but not critical for the initial system security.                       | 
| **High**      | Significant mitigation that addresses serious threats and significantly improves system security.                |
| **Essential** | Critical mitigation required to prevent high-impact attacks that may compromise the system. Must be implemented. |

This table defines the priority assigned to each mitigation. Higher priority mitigations should be implemented first taking into account their greater impact on the system security.


## 3. Abuse Case Mitigation

| Mitigation             | Description | Priority  | Related Abuse Case |
| ---------------------- | ----------- | --------- | ------------------ |
| **Input Sanitization** | All user inputs must be validated and sanitized before being processed to prevent injection attacks. | Essential | Inject Malicious Code (AC-01) |
| **Multi-Factor Authentication (MFA)** | Add an additional authentication factor to prevent unauthorized account access | High | Hijack Account (AC-02) |
|                                       |                                                                                |      | Dictionary Attack (AC-03) |
|                                       |                                                                                |      | Brute Force Login (AC-04) |
| **Rate Limiting and Account Lockout** | Limits repeated login attempts to prevent automated attacks | High | Brute Force Login (AC-04) |
|                                       |                                                             |      | Dictionary Attack (AC-03) |
| **Secure Token Management** | Configure short-lived tokens and secure storage to prevent token reuse | Essential | JWT Token Theft (AC-05 and AC-07) |
| **Endpoint Authentication** | Ensure all protected endpoints implement authentication validation through the use of the tokens | Essential | JWT Token Theft (AC-07) |
|                             |                                                                                                  |           | Modify Order (AC-12) |
| **Role-Based Access Control (RBAC)**  | Restrict actions and operations to specific roles so as to mitigate privilege escalation | Essential | Privilege Escalation |
| **Ownership Validation** | Ensure owned resources can only be accessed by their owners | Essential | Modify Order (AC-12) |
|                          |                                                             |           | Unauthorized Invoice Access (AC-17) |
|                          |                                                             |           | Invoice ID Enumeration (AC-15) |
| **File Name Sanitization** | Prevent malicious file naming and path manipulation | Medium | Upload Malicious File (AC-08) |
| **File Size Validation** | Restrict file sizes to prevent resource abuse and mitigate malicious resource injections | Medium | Upload Oversized File (AC-09) |
| **MIME Type Verification** | Validate actual file type to prevent malicious file uploads | High | Bypass MIME-Verification (AC-10) |
|                                       |                                                  |      | Upload Malicious File (AC-08) |
| **Server-Side Validation** | Ensure all operations are validated on backend to prevent tampering and previlege escalation | Essential | Modify Order (AC-12) |
|                            |                                                                                              |           | Force Duplicated Purchase (AC-13) |
|                            |                                                                                              |           | Bypass Payment (AC-14) |
| **Duplicate Purchase Check** | Prevent purchasing the same game multiple times | High | Force Duplicated Purchase (AC-13) |
| **Payment Validation** | Ensure order completion requires valid payment confirmation | Essential | Bypass Payment (AC-14) |
| **Secure Key Generation** | Generate unpredictable activation keys and securely storage them to prevent guessing | High | Guess Game Key (AC-11) |
| **Authorization Check** | Ensure authorization and access to sensitive resources is validated | Essential | Invoice ID Enumeration (AC-15) |
|                         |                                                                     |           | Unauthorized Invoice Access (AC-17) |
| **Secure File Storage** | Store files outside public access and implemen required authentication to retrieve them | High | Path Traversal Attack (AC-16) |
|                         |                                                                                         |      | Unauthorized Invoice Access (AC-17) |
| **Path Validation** | Prevent directory traversal attacks by sanitizing file paths                                | High | Path Traversal Attack (AC-16) |



## 4. Stride Mitigations

| STRIDE Category            | Mitigation                         | Description                                                                               | Priority  |
| -------------------------- | ---------------------------------- | ----------------------------------------------------------------------------------------- | --------- |
| **Spoofing**               | Multi-Factor Authentication (MFA)  | Enforce Keycloack MFA to prevent identity impersonation using stolen credentials          | Essential |
|                            | Strong Password Policies           | Require complex passwords and detect breached credentials to reduce account takeover risk | High      |
|                            | JWT Validation and Verification    | Validate token signature, issuer, audience, and expiry on requests                        | Essential |
|                            | Trusted Token Issuer Configuration | Only accept tokens from a trusted identity provider                                       | Essential |
|                            | Secure Session Binding             | Bind sessions to device/IP and use short-lived tokens to prevent reuse                    | High      |
|                            | Credential Protection              | Avoid logging credentials or tokens and ensure secure storage and transmission            | Essencial |
| **Tampering**              | Input Validation and Sanitization  | Validate and sanitize all inputs                                                          | Essential |
|                            | Server-Side Enforcement            | Never trust client input and compute critical values server-side                          | Essential |
|                            | Parameterized Queries              | Prevent SQL injection using prepared statements or ORM bindings                           | Essential |
|                            | File Path Validation               | Use canonical paths and UUID filenames to prevent path traversal attacks                  | Essential |
|                            | Secure File Upload Handling        | Validate file type via magic bytes, scan for malware, restrict executable files           | High      |
|                            | Data Integrity Checks              | Ensure integrity of generated artifacts                                                   | Medium    |
| **Repudiation**            | Centralized Audit Logging          | Log all critical actions with timestamps and user IDs                                     | Essential |
|                            | Immutable Logs / SIEM              | Store logs in tamper-evident systems for forensic traceability                            | High      |
|                            | Cross-System Correlation           | Propagate session IDs across services to correlate logs                                   | Medium    |
|                            | Action Attribution                 | Track who performed database, file and system actions                                     | Essential |
|                            | Non-Repudiation Records            | Store signed or verifiable transaction records                                            | High      |
| **Information Disclosure** | Encryption in Transit (TLS)        | Enforce HTTPS, HSTS, and disable plaintext HTTP to prevent interception                   | Essential |
|                            | Encryption at Rest                 | Encrypt sensitive data                                                                    | Essential |
|                            | Least Privilege Access             | Restrict access based on roles and ownership                                              | Essential |
|                            | Secure Error Handling              | Avoid leaking stack traces or internal system details in responses                        | Essential |
|                            | Secure Token Handling              | Never expose tokens in URLs, logs, or client storage                                      | Essential |
|                            | Secrets Management                 | Store credentials in environment variables or secret managers                             | Essential |
|                            | Secure File Access                 | Serve sensitive files via authenticated endpoints only                                    | Essential |
|                            | Data Sanitization (External APIs)  | Treat external API data as untrusted and sanitize it before use                           | High      |
| **Denial of Service**      | Rate Limiting                      | Limit requests per IP/user                                                                | Essential |
|                            | Resource Limits                    | Enforce file size limits, pagination, and query limits                                    | High      |
|                            | Connection and Timeout Controls    | Use reverse proxy with timeouts and connection limits                                     | Medium    |
|                            | Caching and Circuit Breakers       | Cache external dependencies and degrade gracefully                                        | Low       |
|                            | Asynchronous Processing            | Offload heavy tasks to background workers                                                 | Low       |
|                            | Monitoring and Alerting            | Detect abnormal usage patterns and trigger alerts                                         | Essential |
|                            | High Availability (HA)             | Deploy critical services redundantly                                                      | Medium    |
| **Elevation of Privilege** | Role-Based Access Control (RBAC)   | Enforce roles at endpoint and method level                                                | Essential |
|                            | Ownership Validation               | Ensure users can only access their own resources                                          | Essential |
|                            | Strict Authorization Checks        | Validate permissions on every request, not just authentication                            | Essential |
|                            | Separation of Duties               | Separate admin vs user operations                                                         | Medium    |
|                            | Secure Role Assignment             | Restrict admin interfaces and protect role assignment mechanisms                          | High      |
|                            | Token Claim Validation             | Ensure roles are extracted from trusted JWT claims only                                   | Essential |
