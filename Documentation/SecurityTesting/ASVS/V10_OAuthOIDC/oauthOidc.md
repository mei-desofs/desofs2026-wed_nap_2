[← Back to index page](../../../Overview/overview.md)

# OAuth and OIDC Security — ASVS Security Requirements (V10.1 – V10.7)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design
> **Note:** ArcadeHaven uses Keycloak as the identity and authorization server, based on **OAuth2** and **OpenID Connect**. The backend (Spring Boot) acts as a resource server, validating JWT tokens issued by Keycloak. The frontend obtains tokens via Keycloak's authorization code flow with PKCE.

---

## V10.1 — Generic OAuth and OIDC Security

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.1.1 | 2 |  Planned | The frontend communicates with Keycloak directly. No backend-for-frontend token forwarding is used — the Spring Boot backend only validates tokens, never forwards them to other services. |
| V10.1.2 | 2 |  Planned | Token values are issued exclusively by Keycloak and validated by the Spring Boot resource server using Keycloak's public keys (via JWKS endpoint). No token mixing between different authorization servers occurs. |

---

## V10.2 — OAuth Client

| Req ID | Level | Status  | Observations                                                                                                                                                                                                                                                             |
|---|-------|---------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| V10.2.1 | 2     |  Planned | The frontend uses the **Authorization Code flow with PKCE** (`code_challenge` / `code_verifier`). PKCE inherently protects against CSRF-based token request forgery. Additionally, the `state` parameter will be generated and validated on every authorization request. |
| V10.2.2 | 2     | N/A  | ArcadeHaven uses a single Keycloak instance as its only authorization server. Mix-up attacks between multiple authorization servers are not applicable in the current scope.                                                                                             |
| V10.2.3 | 3     | N/A     | Keycloak clients needed to be configured to request only the minimum required scopes: `openid`, `profile`, and `email`. Role-based access (`ADMIN`, `PUBLISHER`, `BUYER`) is carried via custom claims in the JWT, not as scopes.                                        |

---

## V10.3 — OAuth Resource Server

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.3.1 | 2 |  Planned | The Spring Boot resource server validates the `aud` claim in every JWT, ensuring only tokens issued for `arcadehaven-api` are accepted. Tokens issued for other audiences (e.g. Keycloak admin console) will be rejected. |
| V10.3.2 | 2 |  Planned | Authorization decisions are enforced by Spring Security using `@PreAuthorize` annotations. The `sub`, `scope`, and role claims from the JWT are all evaluated in access control decisions at the method level. |
| V10.3.3 | 2 |  Planned | Users are uniquely identified by the combination of `iss` (Keycloak issuer URL) and `sub` (user UUID) claims. This pair is non-reassignable and used as the canonical user identity across the resource server. |
| V10.3.4 | 2 | N/A  | Standard JWT validation (expiry, signature, audience) is enforced. Specific authentication strength constraints (`acr`, `amr`, `auth_time`) are not enforced in Phase 1, but are considered for Phase 2 (e.g., enforcing MFA for admin operations). |
| V10.3.5 | 3 | N/A  | Sender-constrained tokens (mTLS or DPoP) are out of scope for Phase 1. Token theft mitigation relies on short-lived access tokens (15 minutes) and HTTPS enforcement throughout. This may be revisited in a future phase. |

---

## V10.4 — OAuth Authorization Server

| Req ID | Level | Status    | Observations |
|---|-------|-----------|---|
| V10.4.1 | 1     |  Planned | Keycloak is configured with an explicit allowlist of valid redirect URIs per client, using exact string matching. Wildcard URIs are not permitted. |
| V10.4.2 | 1     |  Planned | Keycloak enforces single-use authorization codes natively. If a code is reused, Keycloak rejects the request and revokes any previously issued tokens linked to that code. |
| V10.4.3 | 1     |  Planned | Authorization codes will be configured with a short lifetime (≤ 1 minute) in Keycloak, satisfying both L1/L2 and L3 requirements. |
| V10.4.4 | 1     |  Planned | Only the **Authorization Code** grant type is enabled for ArcadeHaven clients in Keycloak. Implicit flow (`token`) and Resource Owner Password Credentials (`password`) are explicitly disabled. |
| V10.4.5 | 1     |  Planned | Keycloak will be configured with **refresh token rotation** — each use of a refresh token issues a new token and invalidates the previous one. If a revoked refresh token is presented, all tokens for that session are revoked immediately. |
| V10.4.6 | 2     |  Planned | PKCE is enforced for all authorization code requests. Keycloak requires a valid `code_challenge` using `S256` method. The `plain` method is explicitly disallowed. The `code_verifier` is validated on every token request. |
| V10.4.7 | 2     | N/A    | Dynamic client registration is not supported in ArcadeHaven. All clients are pre-registered in Keycloak by administrators. No unauthenticated registration endpoint is exposed. |
| V10.4.8 | 2     |  Planned | Refresh tokens will have an absolute expiration of 7 days, configured in Keycloak. Sliding expiration is disabled to prevent indefinite session extension. |
| V10.4.9 | 2     |  Planned | Users can revoke sessions and refresh tokens via the Keycloak account management console and via the ArcadeHaven logout endpoint (`POST /api/auth/logout`), which calls Keycloak's token revocation endpoint. |
| V10.4.10 | 2     |  Planned | The Spring Boot backend (acting as a confidential client when needed) authenticates to Keycloak using a `client_secret`. All backend-to-Keycloak calls (token introspection, revocation) use confidential client credentials. |
| V10.4.11 | 2     |  Planned | Keycloak client configurations will only include the required scopes (`openid`, `profile`, `email`). No unnecessary scopes are assigned to ArcadeHaven clients. |
| V10.4.12 | 2     | N/A    | Only `response_mode=query` (default for code flow) is permitted. No other response modes are configured or accepted for ArcadeHaven clients in Keycloak. |
| V10.4.13 | 3     | N/A    | Pushed Authorization Requests (PAR) are not implemented in Phase 1. This may be revisited in Phase 2 for higher-security flows (e.g., admin operations). |
| V10.4.14 | 3     | N/A    | Sender-constrained access tokens (mTLS or DPoP) are out of scope for Phase 1. Standard bearer tokens are used, protected by HTTPS and short expiry. |
| V10.4.15 | 3     | N/A    | ArcadeHaven does not use `authorization_details` (Rich Authorization Requests). Not applicable in the current scope. |
| V10.4.16 | 3     | N/A    | Strong client authentication via mTLS or `private_key_jwt` is not implemented in Phase 1. Confidential clients use `client_secret_basic`. This will be evaluated for Phase 2. |

---

## V10.5 — OIDC Client

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.5.1 | 2 |  Planned | The frontend will generate a `nonce` value on every authentication request and validate it against the `nonce` claim in the returned ID Token, protecting against ID Token replay attacks. |
| V10.5.2 | 2 |  Planned | Users are uniquely identified by the `sub` claim in the ID Token, which maps to the Keycloak user UUID. This value is non-reassignable within the Keycloak identity provider. |
| V10.5.3 | 2 |  Planned | The Spring Boot backend and frontend validate the `iss` claim against a pre-configured Keycloak issuer URL. Any token with a different issuer is rejected, preventing authorization server impersonation. |
| V10.5.4 | 2 |  Planned | The `aud` claim in the ID Token is validated against the registered `client_id` of the ArcadeHaven frontend client, ensuring tokens issued for other clients are rejected. |
| V10.5.5 | 2 | N/A  | OIDC back-channel logout is not implemented in Phase 1. Session termination relies on refresh token revocation and short-lived access tokens. Back-channel logout may be considered in Phase 2. |

---

## V10.6 — OpenID Provider

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.6.1 | 2 |  Planned | Keycloak (acting as the OpenID Provider) is configured to only permit `code` as the response type for ArcadeHaven clients. Implicit flow (`token`, `id_token token`) is explicitly disabled. |
| V10.6.2 | 2 |  Planned | Keycloak validates the `id_token_hint` parameter on logout requests and requires explicit user confirmation before terminating sessions. Forced logout DoS is mitigated by Keycloak's built-in logout flow validation. |

---

## V10.7 — Consent Management

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V10.7.1 | 2 |  Planned | Keycloak is configured to prompt users for consent on every authorization request where the client identity cannot be fully assured. For well-known ArcadeHaven clients, consent may be pre-approved at registration, but this is explicitly configured per client. |
| V10.7.2 | 2 |  Planned | Keycloak's consent screen presents clear information about the requested scopes, the identity of the authorizing application (ArcadeHaven), and the session lifetime. Users see what they are authorizing before approving. |
| V10.7.3 | 2 |  Planned | Users can review and revoke active sessions and granted consents via the Keycloak account management console (`/realms/arcadehaven/account`). The ArcadeHaven logout endpoint also triggers full session revocation via Keycloak. |

---

## Summary

| Section | Total |  Planned A | N/A    |
|---|---|---|--------|
| V10.1 Generic OAuth and OIDC Security | 2 | 2 | 0      | 
| V10.2 OAuth Client | 3 | 2 | 1      |
| V10.3 OAuth Resource Server | 5 | 3 | 2      | 
| V10.4 OAuth Authorization Server | 16 | 9 | 7      |
| V10.5 OIDC Client | 5 | 4 | 1      | 
| V10.6 OpenID Provider | 2 | 2 | 0      | 
| V10.7 Consent Management | 3 | 3 | 0      | 
| **Total** | **36** | **25** | **11** | 
