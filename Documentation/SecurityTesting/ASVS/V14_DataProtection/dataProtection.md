# Data Protection — ASVS Security Requirements (V14.1 – V14.3)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design

---

## V14.1 — Data Protection Documentation

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V14.1.1 | 2 | ⚠️ Planned | All sensitive data in ArcadeHaven will be identified and classified. Key sensitive data includes: password hashes, JWT tokens, activation keys, personal user data (email, username), order history, and payment-related data. JWT payloads will be treated as sensitive despite being only Base64-encoded. Classification will be documented in the data protection policy. |
| V14.1.2 | 2 | ⚠️ Planned | A protection requirements document will be created for each data classification level, covering encryption at rest and in transit, integrity verification, retention periods, logging restrictions (e.g. no sensitive data in logs), database-level encryption, and GDPR compliance requirements applicable to EU users. |

---

## V14.2 — General Data Protection

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V14.2.1 | 1 | ⚠️ Planned | ArcadeHaven API will never include sensitive data (API keys, tokens, passwords) in URL query strings. All sensitive data will be transmitted in the HTTP request body (POST/PUT) or in the `Authorization` header. Enforced by API design guidelines and validated in code reviews. |
| V14.2.2 | 2 | ⚠️ Planned | Sensitive API responses (e.g. activation keys, invoice data, user profile) will include `Cache-Control: no-store` headers to prevent caching in intermediary layers such as load balancers or reverse proxies. |
| V14.2.3 | 2 | ⚠️ Planned | ArcadeHaven does not integrate any third-party trackers or analytics services. The only external service calls are to RAWG API (game metadata, no user data sent) and HaveIBeenPwned API (k-anonymity model, no plain-text password sent). No user data is shared with untrusted parties. |
| V14.2.4 | 2 | ⚠️ Planned | Controls defined in V14.1.2 will be implemented and verified: BCrypt for password hashing, TLS for data in transit, `Cache-Control: no-store` for sensitive responses, role-based access to sensitive log data, and data retention policies enforced via scheduled database cleanup jobs. |
| V14.2.5 | 3 | ⚠️ Planned | ArcadeHaven will configure the reverse proxy (Nginx) to return 404 for non-existent resources. Content-Type validation will be enforced on all responses to prevent Web Cache Deception attacks. Dynamic and sensitive endpoints will explicitly set `Cache-Control: no-store`. |
| V14.2.6 | 3 | ⚠️ Planned | API responses will return only the minimum required data. For example, user profile responses will not include the password hash, internal UUIDs will only be exposed where necessary, and activation keys will only be returned once at purchase time and never repeated in subsequent responses. |
| V14.2.7 | 3 | ⚠️ Planned | A data retention policy will be defined and documented. Inactive user accounts, expired activation keys, old order records, and orphaned file uploads will be subject to automated cleanup based on defined retention periods. Implementation planned for Phase 2 Sprint 2. |
| V14.2.8 | 3 | ⚠️ Planned | Game image uploads will be stripped of EXIF and metadata before storage, using a server-side processing step (e.g. via Java ImageIO or a dedicated library). Users will not be required to consent to metadata storage as it will be removed by default. |

---

## V14.3 — Client-side Data Protection

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V14.3.1 | 1 | 🔵 N/A | ArcadeHaven is a back-end REST API with no browser-facing front-end. Client-side storage clearing and `Clear-Site-Data` headers are not applicable in the current scope. |
| V14.3.2 | 2 | ⚠️ Planned | All API responses containing sensitive data will include `Cache-Control: no-store` to prevent browser caching. This will be enforced globally via a Spring Security response header filter, with endpoint-specific overrides where necessary. |
| V14.3.3 | 2 | 🔵 N/A | ArcadeHaven does not control any browser storage (localStorage, sessionStorage, cookies). Authentication uses JWT Bearer tokens passed in the `Authorization` header, managed entirely by the API consumer. |

---

## Summary

| Section | Total | ⚠️ Planned | 🔵 N/A |
|---|---|---|---|
| V14.1 Data Protection Documentation | 2 | 2 | 0 |
| V14.2 General Data Protection | 8 | 8 | 0 |
| V14.3 Client-side Data Protection | 3 | 1 | 2 |
| **Total** | **13** | **11** | **2** |

> V14 is one of the most relevant sections for ArcadeHaven given the sensitive data handled:
> user credentials, activation keys, order history, and personal information.
> The 2 N/A items reflect the absence of a browser front-end.
> Key implementation priorities: no sensitive data in URLs, Cache-Control headers,
> metadata stripping from uploads, and a documented data classification and retention policy.
