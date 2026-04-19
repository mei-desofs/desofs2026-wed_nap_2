# Encoding and Sanitization — ASVS Security Requirements (V1.1 – V1.5)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design
> **Note:** Encoding and Sanitization is very important for ArcadeHaven for the input processing, external data handling and file system output context.

---

## V1.1 — Encoding and Sanitization Architecture

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V1.1.1 | 2 |  Planned | ArcadeHeaven uses Spring Boot which automatically deserializes JSON inputs into Java objects ensuring a canonical form at entry |
| V1.1.2 | 2 |  Planned | Output is mainly JSON responses and server generated files (PDF invoices, activation keys). When writing files to filesystem, untrusted data must be properly escaped or sanitized |

---

## V1.2 — Injection Prevention

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V1.2.1 | 1 |  N/A | ArcadeHaven does not generate HTML or XML content |
| V1.2.2 | 1 |  Planned | URLs used for external API calls (RAWG.io) are validated and contructed using controlled inputs |
| V1.2.3 | 1 |  Planned | JSON serialization is handled by Spring Boot. When writing structured data into files, sanitization must be ensured |
| V1.2.4 | 1 |  Planned | ArcadeHaven uses JPA/Hibernate with parameterized queries which protects against SQL injections |
| V1.2.5 | 1 |  N/A | ArcadeHaven does not use operating system commands |
| V1.2.6 | 2 |  N/A | No LDAP integration is used in ArcadeHaven |
| V1.2.7 | 2 |  N/A | No XPath or XML quering is used in ArcadeHEaven |
| V1.2.8 | 2 |  N/A | No LaTeX processing is used in ArcadeHaven |
| V1.2.9 | 2 |  Planned | Regular expressions using user input must be escaped and constrained |
| V1.2.10 | 3 |  N/A | No CSV or Spreedsheet export is implemented in ArcadeHaven |

---

## V1.3 — Sanitization

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V1.3.1 | 1 |  N/A | No WYSIWYG or HTML input is accepted |
| V1.3.2 | 1 |  Planned | ArcadeHaven avoids dynamic code execution. Any future usaeg must ensure strict validation of input |
| V1.3.3 | 2 |  Planned | User input must be sanitized and validated before storage and before being written into files |
| V1.3.4 | 2 |  N/A |  |
| V1.3.5 | 2 |  N/A | No scriptable content (Markdown, CSS, BBCode) is processed. |
| V1.3.6 | 2 |  Planned | External API calls must be restricted vai allowlist to mitigate risks |
| V1.3.7 | 2 |  Planned | Template-based PDF must not include untrusted input in template logic. ALl dynamic content must be sanitized before rendering |
| V1.3.8 | 2 |  N/A | No JNDI usage is present |
| V1.3.9 | 2 |  N/A | No memcache usage is present |
| V1.3.10 | 2 |  Planned | Logging and formatted outputs must avoid direct inclusion of unsanitized user input to prevent injection into logs or generated files |
| V1.3.11 | 2 |  N/A | No email functionality is implemented |
| V1.3.12 | 3 |  Planned | Regular expressions must be reviewed to avoid exponencial backtracking and must not be built from raw user input |

---

## V1.4 — Memory, String and Unmanaged Code

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V1.4.1 | 2 |  N/A | Java provides built-in memory safety so no manual memory management is used |
| V1.4.2 | 2 |  Planned | Numerical inputs must be validated to prevent logical inconsistencies and overflow-related issues |
| V1.4.3 | 2 |  Planned | Memory management is handled by the JVM eliminating risks |

---

## V1.5 — Safe Deserialization

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V1.5.1 | 1 |  Planned | No XML parsing is used. If introduced, secure parser configurations must be enforced |
| V1.5.2 | 2 |  Planned | JSON deserialization  must be restricted to expected object types |
| V1.5.3 | 3 |  Planned | Parsing consistency must be maintained across all flows, including file generation and external API data handling |

---

## Summary

| Section | Total |  Planned |  N/A |
|---|---|---|---|
| V5.1 Encoding and Sanitization Architecture | 2 | 2 | 0 |
| V5.2 Injection Prevention | 10 | 4 | 6 |
| V5.3 Sanitization | 12 | 6 | 6 |
| V5.4 Memory, String and Unmanaged Code | 3 | 2 | 1 |
| V5.5 Safe Deserialization | 3 | 3 | 0 |
| **Total** | **30** | **17** | **13** |