# V16 Logging and Error Handling

## 1. Overview & Scope

**Context:** OWASP ASVS v5.0 Level 1 evaluation for ArcadeHaven digital game sales platform.

**Coverage:** 17 ASVS V16 requirements mapped to existing security requirements and mitigation controls.

**Primary Mapping Thread:** RNF-07 (Logging Requirements) → LM-001/002/003 (Logging Controls) → ST-008 (Logging Test)

**Secondary Mapping Threads:**
- RNF-06 (Input Validation) → IV-001/003 (Input Validation Controls) → ST-006/007 (Injection Tests)
- RNF-23 (Secrets Management) → SC-001/002 (Secrets Controls) → ST-016 (Configuration Review)

---

## 2. Requirement-by-Requirement Mapping

### V16.1 Logging Documentation (1 requirement)

| V16 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V16.1.1 | Verify there is a common log format across the application including all admin activities, authentication successes and failures, access control failures, input validation failures, encoding failures, and any detected security events. | RNF-07 | LM-001, LM-002, LM-003 | ST-008 | Planned |

**Mapping Rationale:** RNF-07 requires "all security-relevant events shall be logged with sufficient detail". LM-001/002/003 controls define structured logging with metadata, comprehensive auth/authz logging, and security event logging. ST-008 verifies structured log samples contain expected fields.

---

### V16.2 General Logging (5 requirements)

| V16 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V16.2.1 | Verify that all authentication decisions are logged, including both successful and failed attempts, with sufficient metadata to identify the user account, system account, type of authentication, timestamp, and source IP address. | RNF-07 | LM-001, LM-002, LM-003 | ST-008 | Planned |
| V16.2.2 | Verify that all access control failures are logged with sufficient metadata to identify the user account, system account, type of auth, timestamp, and source IP address. | RNF-07 | LM-001, LM-002, LM-003 | ST-008 | Planned |
| V16.2.3 | Verify that logs are protected from unauthorised access and modification. | RNF-23 | SC-001, SC-002 | ST-016 | Planned (Config externalized) |
| V16.2.4 | Verify that all administrative and security-related events are logged including changes to any security parameters, administrative activities and access by administrators and operators. | RNF-07 | LM-001, LM-002, LM-003 | ST-008 | Planned |
| V16.2.5 | Verify that all failed access attempts to objects are logged. | RNF-07 | LM-002 | ST-008 | Planned |

**Mapping Rationale:** V16.2 focuses on metadata-rich logging. LM-001 (structured logging) ensures timestamps, user identity, and source are captured. LM-002 (comprehensive auth/authz logging) specifically handles auth events. LM-003 (security event logging) covers control failures. V16.2.3 on log protection maps to RNF-23 and SC-002 (no secrets in logs/configs).

---

### V16.3 Security Events (4 requirements)

| V16 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V16.3.1 | Verify that log records contain sufficient information to detect attacks on or misuse of the system. | RNF-07, RNF-06 | LM-001, IV-001, IV-003 | ST-008 | Planned |
| V16.3.2 | Verify that all events that modify, create, or delete application or user-created data contain sufficient information to determine what was changed, when it was changed, and by whom. | RNF-07 | LM-001, LM-002, LM-003 | ST-008 | Planned |
| V16.3.3 | Verify that security events are appropriately logged at the application, service and infrastructure level. | RNF-07 | LM-001, LM-002, LM-003 | ST-008 | Planned |
| V16.3.4 | Verify that access to sensitive data is logged, not the data itself. | RNF-07, RNF-23 | LM-001, SC-002 | ST-008, ST-016 | Planned |

**Mapping Rationale:** V16.3 emphasizes security event logging (attacks, data modifications, sensitive access). LM-001 provides structured logging for all operations. IV-001/003 ensure that attack patterns are logged without injection vulnerabilities. SC-002 ensures sensitive data (PII, credentials, payment data) is not inadvertently logged.

---

### V16.4 Log Protection (3 requirements)

| V16 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V16.4.1 | Verify that user-supplied data included in log records is not executed as code in the intended log viewing software. | RNF-06 | IV-001, IV-003 | ST-006, ST-007 | Planned |
| V16.4.2 | Verify that log records are kept for a defined period of time. | RNF-07 | LM-001, LM-002 | ST-008 | Planned (Retention policy Sprint 1) |
| V16.4.3 | Verify that log data cannot be lost due to events outside of the application's control. | RNF-07, RNF-23 | AC-005 (HTTPS) | ST-005 | Not Applicable [Infrastructure/Phase 2] |

**Mapping Rationale:** V16.4 covers log protection from tampering and loss. V16.4.1 (injection prevention) ties to RNF-06 (Input Validation) and IV-003 (output encoding in logs). V16.4.3 (SIEM/log backup) is infrastructure responsibility, marked Not Applicable for Phase 1.

---

### V16.5 Error Handling (4 requirements)

| V16 ID | ASVS Requirement | Mapped RNF/RF | Control(s) | Test ID | Status |
|--------|------------------|---|---|---|---|
| V16.5.1 | Verify that the application uses purpose-built tools to centrally log all authentication attempts and access decisions. | RNF-07 | LM-001, LM-002, LM-003 | ST-008 | Planned |
| V16.5.2 | Verify that the application uses well-designed logging APIs with built-in methods to prevent leakage of sensitive data. | RNF-07, RNF-23 | LM-001, SC-002 | ST-008, ST-016 | Planned |
| V16.5.3 | Verify that the application does not log authentication credentials, session identifiers, access tokens or API keys. | RNF-23 | SC-001, SC-002 | ST-016 | Planned |
| V16.5.4 | Verify that the application does not log sensitive encoded data. | RNF-23 | SC-001, SC-002, IV-001 | ST-016, ST-006 | Planned |

**Mapping Rationale:** V16.5 focuses on preventing sensitive data exposure in error messages and logs. SC-001/002 ensure secrets (JWT tokens, API keys, credentials) are never logged. ST-016 validates configuration and log files for accidental exposure. IV-001 ensures encoded data (base64 credentials) is also filtered from logs.

---

## 3. Key Traceability Threads

### Primary Thread: RNF-07 → Logging Controls → ST-008

**RNF-07 Definition:** "All security-relevant events shall be logged with sufficient detail to enable post-incident forensics and intrusion detection."

**Mapping:**
- **LM-001 (Structured Logging):** Logs contain timestamp, user identity, action type, source IP, and success/failure indicator
- **LM-002 (Comprehensive Auth/Authz Logging):** All authentication attempts (success + failure) and authorization decisions are logged
- **LM-003 (Security Event Logging):** Control failures, injection attempts, and security events are recorded

**Test Coverage (ST-008):**
- Extract structured log samples from application output
- Verify presence of required fields (timestamp, user, action, source)
- Confirm auth/authz decisions are visible in logs
- Validate control failure logging

**V16 Requirements Covered:** V16.1.1, V16.2.1-5, V16.3.1-3, V16.4.2, V16.5.1-2

---

### Secondary Thread: RNF-06 → Input Validation → ST-006/007

**RNF-06 Definition:** "Input validation shall prevent injection attacks (SQL, OS command, XXE, etc.)."

**Mapping to V16:**
- **IV-001 (Whitelist Validation):** Logs contain only expected characters; user input is sanitized before logging
- **IV-003 (Output Encoding):** Log entries are HTML/JSON-encoded to prevent injection when viewed in logs

**Test Coverage (ST-006, ST-007):**
- ST-006: Attempt SQL injection via user input and verify payload does not appear in logs unsanitized
- ST-007: Attempt XSS/command injection via login fields and verify encoded in logs

**V16 Requirements Covered:** V16.4.1 (log injection prevention), V16.3.1 (attack detection)

---

### Tertiary Thread: RNF-23 → Secrets Management → ST-016

**RNF-23 Definition:** "Sensitive data (credentials, payment data, PII) shall be protected through encryption and restricted access; secrets shall not be exposed in code/configs/logs."

**Mapping to V16:**
- **SC-001 (Encryption at Rest):** Logs stored securely if persisted beyond application runtime
- **SC-002 (No Secrets in Logs/Configs):** JWT tokens, API keys, passwords, credit card numbers are never logged; sanitization rules prevent accidental exposure

**Test Coverage (ST-016):**
- Review application configuration files and log samples
- Search for hardcoded secrets, API keys, passwords
- Verify no JWT tokens, session IDs, or PII in logs

**V16 Requirements Covered:** V16.2.3 (log protection), V16.3.4 (no sensitive data in logs), V16.4.1 (injection prevention), V16.5.2-4 (no credentials/tokens/keys in logs)

---

## 4. Documented Gaps & Caveats

| V16 Item | Gap Description | Status | Sprint | Rationale |
|----------|---|---|---|---|
| V16.2.2 | UTC timestamps synchronized across distributed systems | Planned (Dependency) | Sprint 1 | Requires NTP/time-sync infrastructure; currently not enforced in Phase 1 |
| V16.2.4 | Logs correlated across services with transaction IDs | Planned (Enhancement) | Sprint 1 | Requires JSON logging format + distributed tracing; basic logging in place, enhancement for future |
| V16.4.2 | Log retention policy enforcement (e.g., 90 days) | Planned (Policy) | Sprint 1 | Requires log storage infrastructure config; not implemented at application level |
| V16.4.3 | Logs encrypted in transit and at rest; SIEM integration | Not Applicable [Phase 2] | Infrastructure | Log backup/SIEM is DevOps responsibility; application sends structured logs; centralization handled post-deployment |

---

## 5. Evidence & References

**Cross-Reference Table:**

| Document | Reference | Evidence |
|----------|-----------|----------|
| requirements.md | RNF-07 | "All security-relevant events shall be logged with sufficient detail to enable post-incident forensics" |
| requirements.md | RNF-06 | "Input validation shall prevent injection attacks" |
| requirements.md | RNF-23 | "Sensitive data shall be protected through encryption and restricted access" |
| mitigations.md | LM-001/002/003 | Logging control catalog with metadata, auth/authz, and event specifications |
| mitigations.md | IV-001/003 | Input validation controls for whitelist + output encoding |
| mitigations.md | SC-001/002 | Secrets control catalog (encryption at rest, no secrets in logs) |
| securityTesting.md | ST-008 | Logging test specification (structured log verification) |
| securityTesting.md | ST-016 | Configuration review test (verify no exposed secrets) |
| securityTesting.md | ST-006/007 | Injection test specifications (SQL, XSS prevention) |
| threatIdentificationAndAnalysis.md | TH-04 | Configuration Exposure threat; logging controls mitigate information disclosure |
| threatIdentificationAndAnalysis.md | TH-09 | Audit Failure threat; comprehensive logging required for forensics |

---

## Summary

V16 Logging and Error Handling is fully applicable to ArcadeHaven. The 17 requirements map cleanly to 3 existing non-functional requirements (RNF-07, RNF-06, RNF-23), 10 existing mitigation controls (LM-001/002/003, IV-001/003, SC-001/002, AC-005), and 4 existing security tests (ST-008, ST-016, ST-006/007, ST-005). 

Gaps (timezone sync, log retention, SIEM) are marked as "Planned [Sprint 1]" or "Not Applicable [Infrastructure]" to be addressed in subsequent sprints or DevOps configuration.

**Status:** Ready for Phase 1 implementation and testing.
