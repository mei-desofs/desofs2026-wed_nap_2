# Session Management — ASVS Security Requirements (V7.1 – V7.6)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design
> **Note:** ArcadeHaven uses authentication based on JWT via Spring Security. There are no traditional server-side sessions; instead, authentication and authorization are handled through signed tokens sent in each request.

---

## V7.1 — Session Management Document 

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V7.1.1 | 2 | ⚠️ Planned | JWT expiration time must be documented including justification based on security |
| V7.1.2 | 2 | 🔵 N/A | ArcadeHAven does not track server-side sessions |
| V7.1.3 | 2 | 🔵 N/A | No federated identity is used in ArcadeHaven |

---

## V7.2 — Fundamental Session Management Security

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V7.2.1 | 1 | ⚠️ Planned | JWT validation is performed in the backend using Spring Security |
| V7.2.2 | 1 | ⚠️ Planned | ArcadeHaven uses dynamically generated JWT tokens |
| V7.2.3 | 1 | 🔵 N/A | Reference tokens are not used |
| V7.2.4 | 1 | ⚠️ Planned | A new JWT is issued on authentication, re-authentication must be ensured when old tokens are no longer usable when required |

---

## V7.3 — Session Timeout

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V7.3.1 | 2 | ⚠️ Planned | JWT expiration enforces inactivity timeout indirectly so token lifetime must be defined and documented |
| V7.3.2 | 2 | ⚠️ Planned | Absolute session lifetime is enforced via JWT expiration |

---

## V7.4 — Session Termination

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V7.4.1 | 1 | ⚠️ Planned | JWT-based systems cannot invalidate tokens by default so mitigation strategies should be considered |
| V7.4.2 | 1 | ⚠️ Planned | When a user is disabled adittional mechanisms must invalidate tokens |
| V7.4.3 | 2 | ⚠️ Planned | Token invalidation after password change should requires additional mechanisms |
| V7.4.4 | 2 | 🔵 N/A | ArcadeHaven is backend-only, logout visibility is not enforced |
| V7.4.5 | 2 | ⚠️ Planned | Admin-driven session would require token revocation mechanisms |

---

## V7.5 — Defenses Against Session Abuse

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V7.5.1 | 2 | ⚠️ Planned | Sensitive operations should require re-authentication |
| V7.5.2 | 2 | 🔵 N/A | ArcadeHaven does not track active sessions centrally |
| V7.5.3 | 3 | ⚠️ Planned | High risk operations should require additional verification |

---

## V7.6 — Federated Re-authentication

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V7.6.1 | 2 | 🔵 N/A | No federated authentication is used in ArcadeHaven |
| V7.6.1 | 2 | ⚠️ Planned | Session creation occurs oonly after explicit user authentication |

---

## Summary

| Section | Total | ⚠️ Planned | 🔵 N/A |
|---|---|---|---|
| V7.1 Session Management Document | 3 | 1 | 2 |
| V7.2 Fundamental Session Management Security | 4 | 3 | 1 |
| V7.3 Session Timeout | 2 | 2 | 0 |
| V7.4 Session Termination | 5 | 4 | 1 |
| V7.5 Defenses Against Session Abuse | 3 | 2 | 1 |
| V7.6 Federated Re-authentication | 2 | 1 | 1 |
| **Total** | **19** | **13** | **6** |