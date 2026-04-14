# OAuth and OIDC Security — ASVS Security Requirements (V10.1 – V10.7)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design
> **Note:** ArcadeHaven uses internal JWT-based authentication in Phase 1. OAuth2/OIDC is not implemented in the current scope. All requirements in this section are marked accordingly.

---

## V10.1 — Generic OAuth and OIDC Security

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.1.1 | 2 |  N/A | ArcadeHaven does not use OAuth2 in Phase 1. Tokens are issued and consumed internally. No backend-for-frontend or token forwarding is used. |
| V10.1.2 | 2 |  N/A | No external authorization server is used. Token values are generated and validated entirely within the ArcadeHaven backend. |

---

## V10.2 — OAuth Client

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.2.1 | 2 |  N/A | ArcadeHaven does not implement an OAuth client or authorization code flow in Phase 1. |
| V10.2.2 | 2 |  N/A | No multiple authorization server interaction is used. Not applicable in the current scope. |
| V10.2.3 | 3 |  N/A | No OAuth client is implemented. Scope management is handled internally via role-based access control (ADMIN, PUBLISHER, BUYER). |

---

## V10.3 — OAuth Resource Server

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.3.1 | 2 |  Planned | ArcadeHaven acts as its own resource server. JWT tokens include an `aud` claim set to `arcadehaven-api`, validated on every request. Tokens not intended for ArcadeHaven will be rejected. |
| V10.3.2 | 2 |  Planned | Authorization decisions are based on the `role` claim in the JWT. Spring Security's method-level security (`@PreAuthorize`) enforces delegated authorization based on token claims. |
| V10.3.3 | 2 |  Planned | The user's unique identifier (`sub` claim, mapped to the user UUID) is extracted from the JWT for access control decisions. No separate introspection endpoint is used. |
| V10.3.4 | 2 |  N/A | ArcadeHaven does not require specific authentication strength or recentness for resource access beyond standard JWT validation. Not applicable in the current scope. |
| V10.3.5 | 3 |  N/A | Sender-constrained tokens are not implemented in Phase 1. Token theft prevention relies on short expiry (15 minutes) and HTTPS enforcement. |

---

## V10.4 — OAuth Authorization Server

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.4.1 | 1 |  N/A | ArcadeHaven does not implement an OAuth authorization server or redirect URI flows in Phase 1. |
| V10.4.2 | 1 |  N/A | No authorization code flow is implemented. Not applicable in the current scope. |
| V10.4.3 | 1 |  N/A | No authorization codes are issued. Not applicable in the current scope. |
| V10.4.4 | 1 |  N/A | No OAuth grant types are implemented. Access is controlled via internal JWT issuance only. |
| V10.4.5 | 1 |  N/A | No refresh token replay mitigation via sender-constrained tokens is needed. Refresh tokens will use rotation — each use issues a new token and invalidates the previous one. |
| V10.4.6 | 2 |  N/A | No authorization code flow or PKCE is implemented in Phase 1. |
| V10.4.7 | 2 |  N/A | No dynamic client registration is supported. ArcadeHaven has no external OAuth clients. |
| V10.4.8 | 2 |  Planned | Refresh tokens will have an absolute expiration (e.g. 7 days), regardless of usage. Sliding expiration will not be used to prevent indefinite session extension. |
| V10.4.9 | 2 |  Planned | Users will be able to revoke refresh tokens via the logout endpoint (`POST /api/auth/logout`). A token denylist will be maintained server-side to immediately invalidate revoked tokens. |
| V10.4.10 | 2 |  N/A | No confidential OAuth clients are used. ArcadeHaven issues tokens directly to authenticated users, not to external client applications. |
| V10.4.11 | 2 |  N/A | No OAuth client scope configuration is applicable. Permissions are managed via user roles defined in the JWT claims. |
| V10.4.12 | 3 |  N/A | No `response_mode` configuration is applicable. ArcadeHaven does not implement an OAuth authorization server. |
| V10.4.13 | 3 |  N/A | No authorization code flow or Pushed Authorization Requests (PAR) are used. |
| V10.4.14 | 3 |  N/A | Sender-constrained (Proof-of-Possession) tokens are out of scope for Phase 1. |
| V10.4.15 | 3 |  N/A | No server-side OAuth client is implemented. Not applicable in the current scope. |
| V10.4.16 | 3 |  N/A | No external OAuth client authentication is used. Not applicable in the current scope. |

---

## V10.5 — OIDC Client

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.5.1 | 2 |  N/A | ArcadeHaven does not implement an OIDC client or consume ID Tokens from an external provider in Phase 1. |
| V10.5.2 | 2 |  N/A | User identity is uniquely identified via the `sub` claim (user UUID) in the internally issued JWT. No external OIDC ID Token is used. |
| V10.5.3 | 2 |  N/A | No external authorization server is used. Issuer confusion attacks are not applicable in the current scope. |
| V10.5.4 | 2 |  N/A | No external ID Tokens are consumed. Audience validation is performed on internally issued JWTs as documented in V9.2.3. |
| V10.5.5 | 2 |  N/A | No OIDC back-channel logout is implemented. Not applicable in the current scope. |

---

## V10.6 — OpenID Provider

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.6.1 | 2 |  N/A | ArcadeHaven does not act as an OpenID Provider. Not applicable in the current scope. |
| V10.6.2 | 2 |  N/A | ArcadeHaven does not act as an OpenID Provider. Forced logout DoS mitigation is not applicable. |

---

## V10.7 — Consent Management

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.7.1 | 2 |  N/A | No external OAuth authorization flows requiring user consent screens are implemented. ArcadeHaven manages permissions internally via role assignment at registration. |
| V10.7.2 | 2 |  N/A | No OAuth consent prompts are displayed. Not applicable in the current scope. |
| V10.7.3 | 2 |  N/A | No OAuth consent grants are managed externally. Users can manage their account and revoke sessions via the ArcadeHaven API directly. |

---

## Summary

| Section | Total |  Planned |  N/A |
|---|---|---|---|
| V10.1 Generic OAuth and OIDC Security | 2 | 0 | 2 |
| V10.2 OAuth Client | 3 | 0 | 3 |
| V10.3 OAuth Resource Server | 5 | 3 | 2 |
| V10.4 OAuth Authorization Server | 16 | 2 | 14 |
| V10.5 OIDC Client | 5 | 0 | 5 |
| V10.6 OpenID Provider | 2 | 0 | 2 |
| V10.7 Consent Management | 3 | 0 | 3 |
| **Total** | **36** | **5** | **31** |

> The high N/A count reflects that ArcadeHaven does not implement OAuth2/OIDC in Phase 1.
> The 5 Planned items under V10.3 and V10.4 are relevant because ArcadeHaven acts as its own resource server,
> validating JWT audience, role claims, and managing refresh token lifecycle internally.
