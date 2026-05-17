[← Back to index page](../../../Overview/overview.md)

# Encoding and Sanitization — ASVS Security Requirements (V1.1 – V1.5)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design
> **Note:** Encoding and Sanitization is very important for ArcadeHaven for the input processing, external data handling and file system output context.

---

## V1.1 — Encoding and Sanitization Architecture

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V1.1.1 | 2 |  Planned | ArcadeHeaven uses Spring Boot which automatically deserializes JSON inputs into Java objects ensuring a canonical form at entry. Validation will be enforced using Bean Validation (`@Valid`, `@Pattern`, `@Size`) |
| V1.1.2 | 2 |  Planned | Output is mainly JSON responses and server generated files (PDF invoices, activation keys). When writing files to filesystem, untrusted data will be properly escaped or sanitized |

---

## V1.2 — Injection Prevention

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V1.2.1 | 1 |  N/A | ArcadeHaven does not generate HTML or XML content |
| V1.2.2 | 1 |  Planned | URLs used for external API calls (RAWG.io) are validated and contructed using controlled inputs. No user-controlled URL concatenation is allowed |
| V1.2.3 | 1 |  Planned | JSON serialization is handled by Spring Boot. When writing structured data into files, all dynamic fields will be sanitized based on output context |
| V1.2.4 | 1 |  Planned | ArcadeHaven uses JPA/Hibernate with parameterized queries which protects against SQL injections. No dynamic query concatenation is allowed |
| V1.2.5 | 1 |  N/A | ArcadeHaven does not use operating system commands |
| V1.2.6 | 2 |  N/A | No LDAP integration is used in ArcadeHaven |
| V1.2.7 | 2 |  N/A | No XPath or XML quering is used in ArcadeHeaven |
| V1.2.8 | 2 |  N/A | No LaTeX processing is used in ArcadeHaven |
| V1.2.9 | 2 |  Planned | Regular expressions will be predefined and will not include raw user input. Input lenght contrainsts will be enforced to prevent ReDoS attacks |
| V1.2.10 | 3 |  N/A | No CSV or Spreedsheet export is implemented in ArcadeHaven |

---

## V1.3 — Sanitization

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V1.3.1 | 1 |  N/A | No WYSIWYG or HTML input is accepted |
| V1.3.2 | 1 |  Planned | ArcadeHaven does not support dynamic code execution. Any future usage will require strict validation of input validation |
| V1.3.3 | 2 |  Planned | All user input will be validated at entry using Bean Validation. Output enconding will be applied when data is rendered or written to files |
| V1.3.4 | 2 |  N/A | SVG files are explicitly rejected during file upload validation |
| V1.3.5 | 2 |  N/A | No scriptable content (Markdown, CSS, BBCode) is processed |
| V1.3.6 | 2 |  Planned | External API calls will be restricted using an allowlist of domains and fixed endpoints. Responses will be validated before processing |
| V1.3.7 | 2 |  Planned | Template-based PDF will use sanitized values of dynamic data. No user input will influence template logic or structure |
| V1.3.8 | 2 |  N/A | No JNDI usage is present |
| V1.3.9 | 2 |  N/A | No memcache usage is present |
| V1.3.10 | 2 |  Planned | Logging will use structured logging and parameterized messages. User input will not be directly concatenated into logs, preventing log injection |
| V1.3.11 | 2 |  N/A | No email functionality is implemented |
| V1.3.12 | 3 |  Planned | Regular expressions must be reviewed to avoid exponencial backtracking. Only safe, bounded patterns will be used |

---

## V1.4 — Memory, String and Unmanaged Code

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V1.4.1 | 2 |  N/A | Java provides built-in memory safety so no manual memory management is used |
| V1.4.2 | 2 |  Planned | Numerical inputs will be validated using constraints to prevent overflow and logical inconsistencies. |
| V1.4.3 | 2 |  Planned | Memory management is handled by the JVM. Input size limits will be enforced to prevent excessive memory consumption |

---

## V1.5 — Safe Deserialization

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V1.5.1 | 1 |  Planned | XML parsing is not currently used. If introduced, secure parser configurations will be enforced |
| V1.5.2 | 2 |  Planned | JSON deserialization will be restricted to expected DTO classes |
| V1.5.3 | 3 |  Planned | Consistent parsing rules will be enforced across all data flows, including API input, file generation, and external API responses |

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