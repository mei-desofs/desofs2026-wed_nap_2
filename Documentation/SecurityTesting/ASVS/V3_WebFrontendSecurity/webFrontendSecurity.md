[← Back to index page](../../../Overview/overview.md)

# Web Frontend Security — ASVS Security Requirements (V3.1 – V3.7)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design
> **Note:** ArcadeHaven is a back-end only REST API. Most front-end browser security requirements are not applicable by design.

---

## V3.1 — Web Frontend Security Documentation

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V3.1.1 | 3 |  N/A | ArcadeHaven is a REST API with no browser-facing front-end. No browser security features need to be documented. |

---

## V3.2 — Unintended Content Interpretation

| Req ID | Level | Status | Observations                                                                                                                          |
|---|---|---|---------------------------------------------------------------------------------------------------------------------------------------|
| V3.2.1 | 1 |  N/A | API responses don't include `Content-Disposition: attachment` for file downloads and validate `Sec-Fetch-*` headers where applicable. |
| V3.2.2 | 1 |  N/A | No HTML rendering occurs in the API. All responses are JSON or binary file content.                                                   |
| V3.2.3 | 3 |  N/A | No client-side JavaScript is used. ArcadeHaven is a back-end REST API only.                                                           |

---

## V3.3 — Cookie Setup

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V3.3.1 | 1 |  N/A | ArcadeHaven uses JWT tokens in the `Authorization` header, not cookies. |
| V3.3.2 | 2 |  N/A | No cookies are used. Authentication is handled via JWT Bearer tokens. |
| V3.3.3 | 2 |  N/A | No cookies are used. Authentication is handled via JWT Bearer tokens. |
| V3.3.4 | 2 |  N/A | No session cookies are used. JWTs are transmitted via the `Authorization` header only. |
| V3.3.5 | 3 |  N/A | No cookies are used in ArcadeHaven. |

---

## V3.4 — Browser Security Mechanism Headers

| Req ID | Level | Status | Observations                                                                                                                                      |
|---|---|---|---------------------------------------------------------------------------------------------------------------------------------------------------|
| V3.4.1 | 1 |  Planned | HSTS header (`Strict-Transport-Security: max-age=31536000; includeSubDomains`) will be enforced on all API responses via Spring Security.         |
| V3.4.2 | 1 |  Planned | CORS will be configured with a strict allowlist of trusted origins. `Access-Control-Allow-Origin: *` will not be used on authenticated endpoints. |
| V3.4.3 | 2 |  N/A | CSP not applicable to JSON REST API without HTML responses.                                                                                       |
| V3.4.4 | 2 |  Planned | `X-Content-Type-Options: nosniff` will be included on all API responses via Spring Security headers configuration.                                |
| V3.4.5 | 2 |  N/A | Referrer-Policy has minimal relevance in API-only context.                                                                                        |
| V3.4.6 | 2 |  N/A | Frame-ancestors not applicable (no browser rendering).                                                                                            |
| V3.4.7 | 3 |  N/A | CSP reporting not applicable without CSP usage.                                                                                                  |
| V3.4.8 | 3 |  N/A | ArcadeHaven does not serve HTML documents. `Cross-Origin-Opener-Policy` is not applicable to a JSON REST API.                                     |

---

## V3.5 — Browser Origin Separation

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V3.5.1 | 1 |  Planned | All sensitive endpoints require a valid JWT Bearer token in the `Authorization` header, which is not a CORS-safelisted header, effectively preventing CSRF. |
| V3.5.2 | 1 |  Planned | All state-changing API calls require `Content-Type: application/json`, which triggers a CORS preflight. Requests without this header will be rejected. |
| V3.5.3 | 1 |  Planned | All sensitive operations use appropriate HTTP methods (POST, PUT, PATCH, DELETE). GET endpoints are read-only and do not expose sensitive functionality. |
| V3.5.4 | 2 |  N/A | Single backend; hostname separation not applicable. |
| V3.5.5 | 2 |  N/A | No client-side messaging (postMessage). |
| V3.5.6 | 3 |  N/A | JSONP not used. |
| V3.5.7 | 3 |  N/A | API does not serve JavaScript files or executable client-side code. |
| V3.5.8 | 3 |  N/A |  |

---

## V3.6 — External Resource Integrity

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V3.6.1 | 3 |  N/A | ArcadeHaven does not serve or load any external client-side assets (JS libraries, CSS, fonts). No CDN resources are used in the back-end API. |

---

## V3.7 — Other Browser Security Considerations

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V3.7.1 | 2 |  N/A | ArcadeHaven uses no client-side technologies. No plugins, Flash, ActiveX, or applets are used. |
| V3.7.2 | 2 |  Planned | Any redirect issued by the API (e.g. after OAuth2 flows in future phases) will validate the destination against a strict allowlist. |
| V3.7.3 | 3 |  N/A | ArcadeHaven is a REST API. No user-facing redirect notifications are applicable. |
| V3.7.4 | 3 |  Planned | The ArcadeHaven domain will be submitted to the HSTS preload list once a production domain is established. Applicable from Phase 2 Sprint 2 onwards. |
| V3.7.5 | 3 |  N/A | ArcadeHaven is a REST API with no browser-facing interface. Browser feature detection is not applicable. |

---

## Summary

| Section | Total |  Planned |  N/A |
|---|---|---|---|
| V3.1 Web Frontend Security Documentation | 1 | 0 | 1 |
| V3.2 Unintended Content Interpretation | 3 | 1 | 2 |
| V3.3 Cookie Setup | 5 | 0 | 5 |
| V3.4 Browser Security Mechanism Headers | 8 | 6 | 2 |
| V3.5 Browser Origin Separation | 8 | 5 | 3 |
| V3.6 External Resource Integrity | 1 | 0 | 1 |
| V3.7 Other Browser Security Considerations | 5 | 2 | 3 |
| **Total** | **31** | **14** | **17** |

> The high number of N/A requirements reflects that ArcadeHaven is a back-end REST API with no front-end.
> The planned items focus on HTTP security headers and CORS configuration, which are relevant even for API-only projects.
