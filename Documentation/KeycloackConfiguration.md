# Keycloak Security Documentation

## Overview

This document describes the Keycloak identity and access management (IAM) configuration for the **ArcadeHaven** platform. It covers realm settings, client definitions, authentication flows, token policies, and Spring Boot integration.

---

## 1. Realm Configuration (`realm-export.json`)

**Realm:** `arcadehaven`

### 1.1 General Security Settings

| Setting | Value | Notes |
|---|---|---|
| `registrationAllowed` | `false` | Self-registration is disabled; users are provisioned via Admin API only |
| `loginWithEmailAllowed` | `true` | Users may authenticate with email or username |
| `duplicateEmailsAllowed` | `false` | Enforces unique email per account |
| `resetPasswordAllowed` | `true` | Password reset flow is enabled |
| `bruteForceProtected` | `true` | Brute-force lockout is active |
| `permanentLockout` | `false` | Accounts unlock automatically after the wait window |

### 1.2 Brute-Force Security Settings

| Parameter | Value | Meaning |
|---|---|---|
| `failureFactor` | `5` | Account is locked after 5 consecutive failures |
| `maxFailureWaitSeconds` | `900` | Maximum lockout wait time: 15 minutes |
| `waitIncrementSeconds` | `60` | Each subsequent failure adds 60 s to the wait |
| `minimumQuickLoginWaitSeconds` | `60` | Minimum wait when rapid logins are detected |
| `quickLoginCheckMilliSeconds` | `1000` | Rapid-login threshold: < 1 s between attempts |
| `maxDeltaTimeSeconds` | `43200` | Failure counter resets after 12 hours of no failures |

---

## 2. Token Lifetime

| Token / Session | Lifetime | Rationale |
|---|---|---|
| Access token (`accessTokenLifespan`) | **300 s (5 min)** | Short-lived to limit the exposure window of stolen tokens |
| SSO session idle (`ssoSessionIdleTimeout`) | **1800 s (30 min)** | Session expires after 30 min of inactivity |
| SSO session max (`ssoSessionMaxLifespan`) | **604 800 s (7 days)** | Hard upper bound for any SSO session |
| Auth code (`accessCodeLifespan`) | **60 s** | One-time authorization codes expire quickly |
| Auth code login (`accessCodeLifespanLogin`) | **1800 s** | Time allowed to complete the login interaction |
| User-action code (`accessCodeLifespanUserAction`) | **300 s** | Email verification / password-reset links expire in 5 min |
| `revokeRefreshToken` | `true` | Refresh tokens are single-use (rotation enforced) |
| `refreshTokenMaxReuse` | `0` | No refresh-token reuse is permitted |
| Offline sessions | Disabled | No long-lived offline tokens |

---

## 3. Roles

Three realm-level roles are defined:

| Role | Purpose |
|---|---|
| `ADMIN` | Platform administrators (subject to MFA) |
| `PUBLISHER` | Game publishers |
| `BUYER` | End users |

---

## 4. Clients

### 4.1 `arcadehaven-public` - Browser / API Client

| Property | Value |
|---|---|
| Type | Public (no client secret) |
| Flows | Standard (OIDC code flow) + Direct Access Grants |
| PKCE | `S256` enforced via `pkce.code.challenge.method` |
| Redirect URIs | `localhost:3000/*`, `localhost:5173/*` |
| Audience mapper | Injects `arcadehaven-api` into access tokens |
| Implicit flow | Disabled |
| Service accounts | Disabled |

**PKCE:** Public client enforces `S256` PKCE for all authorization-code flows, preventing authorization-code interception attacks.

### 4.2 `arcadehaven-backend` - Service Account Client

| Property | Value |
|---|---|
| Type | Confidential (client secret) |
| Grant type | `client_credentials` only |
| Standard / Implicit / Direct flows | All disabled |
| Service accounts | Enabled |
| Purpose | Keycloak Admin API (user CRUD, role assignment, session revocation) |

The service account is granted `realm-admin` under `realm-management`, scoping its privileges to the `arcadehaven` realm only.

---

## 6. MFA / OTP Policy

| Parameter | Value |
|---|---|
| Algorithm | `HmacSHA1` (TOTP, RFC 6238) |
| Digits | 6 |
| Period | 30 seconds |
| Look-ahead window | 1 (allows ±30 s clock skew) |
| Code reuse | Disabled |
| Supported apps | FreeOTP, Google Authenticator |

---

## 7. Seed Users

Keycloak configuration defines the following users for development and testing purposes only.

| Username | Role | Email |
|---|---|---|
| `admin` | `ADMIN` | admin@arcadehaven.com |
| `publisher1` | `PUBLISHER` | publisher1@arcadehaven.com |
| `buyer1` | `BUYER` | buyer1@arcadehaven.com |