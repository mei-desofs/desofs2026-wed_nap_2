[← Back to index page](../../../Overview/overview.md)

# API and Web Service Security — ASVS Security Requirements (V4.1 – V4.4)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design

---

## V4.1 — Generic Web Service Security

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V4.1.1 | 1 |  Planned | All API responses will include `Content-Type: application/json; charset=UTF-8`. File download endpoints will use the appropriate MIME type (e.g. `application/pdf`, `image/png`). Enforced globally via Spring Boot's response configuration. |
| V4.1.2 | 2 |  Planned | All HTTP traffic is redirected to HTTPS at the reverse proxy level. The application itself is only exposed over HTTPS.  |
| V4.1.3 | 2 |  Planned | Headers such as `X-Real-IP` and `X-Forwarded-For` set by intermediary layers (e.g. reverse proxy, Docker network) will be validated server-side. End-users will not be able to override these headers. Configuration will be enforced in the Spring Boot application and documented in the deployment guide. |
| V4.1.4 | 3 |  N/A | Due to the academic scope of this project, this requirement is considered out of scope and is therefore not implemented. |
| V4.1.5 | 3 |  N/A | ArcadeHaven does not handle highly sensitive inter-system transactions requiring per-message digital signatures. Transport-level TLS is considered sufficient for the current scope. |

---

## V4.2 — HTTP Message Structure Validation

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V4.2.1 | 2 |  Planned | ArcadeHaven will use HTTP/1.1 initially via Spring Boot embedded Tomcat, which handles `Transfer-Encoding` and `Content-Length` boundaries correctly by default. HTTP/2 support will be evaluated in Phase 2 with explicit validation of DATA frame consistency. |
| V4.2.2 | 3 |  Planned | Spring Boot / Tomcat handles `Content-Length` generation internally. No manual header construction will be performed. This will be validated during security testing in Phase 2. |
| V4.2.3 | 3 |  Planned | If HTTP/2 is enabled in Phase 2, connection-specific header fields such as `Transfer-Encoding` will be explicitly rejected. Spring Boot's HTTP/2 configuration will be reviewed against this requirement. |
| V4.2.4 | 3 |  Planned | Input validation will reject any header field values containing CR, LF, or CRLF sequences. This will be enforced at the Spring Security filter level. |
| V4.2.5 | 3 |  Planned | Request size limits will be configured in Spring Boot (`spring.servlet.multipart.max-request-size`, `server.max-http-header-size`) to prevent denial of service via oversized headers or request bodies. |

---

## V4.3 — GraphQL

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V4.3.1 | 2 |  N/A | ArcadeHaven uses a REST API, not GraphQL. This requirement is not applicable. |
| V4.3.2 | 2 |  N/A | ArcadeHaven uses a REST API, not GraphQL. Introspection queries do not exist in this context. |

---

## V4.4 — WebSocket

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V4.4.1 | 1 |  N/A | ArcadeHaven does not implement WebSocket connections. All communication is via standard HTTPS REST API calls. |
| V4.4.2 | 2 |  N/A | No WebSocket handshake is performed. Not applicable to the current project scope. |
| V4.4.3 | 2 |  N/A | No WebSocket connections are used. Session management is handled via JWT Bearer tokens over HTTPS. |
| V4.4.4 | 2 |  N/A | No WebSocket connections are used. Not applicable to the current project scope. |

---

## Summary

| Section | Total |  Planned |  N/A |
|---|---|---|---|
| V4.1 Generic Web Service Security | 5 | 4 | 1 |
| V4.2 HTTP Message Structure Validation | 5 | 5 | 0 |
| V4.3 GraphQL | 2 | 0 | 2 |
| V4.4 WebSocket | 4 | 0 | 4 |
| **Total** | **16** | **9** | **7** |

> GraphQL and WebSocket sections are fully N/A as ArcadeHaven uses a standard REST API over HTTPS.
> The focus for this section is on HTTP headers, method restrictions, and request structure validation — all handled via Spring Boot and Spring Security configuration.
