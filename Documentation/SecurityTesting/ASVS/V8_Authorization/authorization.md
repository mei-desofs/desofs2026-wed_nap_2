# Authorization — ASVS Security Requirements (V8.1 – V8.4)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design
> **Note:** ArcadeHaven implements role-based authorization. Access control is enforced at the backend service layer and is based on user roles.

---

## V8.1 — Authorization Document 

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V8.1.1 | 1 | ⚠️ Planned | Authorization must define access based on roles |
| V8.1.2 | 2 | ⚠️ Planned | Field-level access must be restricted depending on user roles |
| V8.1.3 | 3 | 🔵 N/A | Authorization decision are not based on contextual attributes |
| V8.1.4 | 3 | 🔵 N/A | No adaptative or risk-based authorization are enforced |

---

## V8.2 — General Authorization Design

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V8.2.1 | 1 | ⚠️ Planned | Functional-level access is restricted using Spring Security annotations based on roles |
| V8.2.2 | 1 | ⚠️ Planned | Data-level access must enforce ownerships checks |
| V8.2.3 | 2 | ⚠️ Planned | Field-level restrictions must be enforced in DTOs and API responses to prevent exposure of sensitive attributes |
| V8.2.4 | 3 | 🔵 N/A | No contextual or adaptative authorization controls are enforced |

---

## V8.3 — Operation Level Authorization

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V8.3.1 | 1 | ⚠️ Planned | Authorization is enforced at backend service layer |
| V8.3.2 | 3 | ⚠️ Planned | Changes in user roles or permissions are not reflected in JWT tokens |
| V8.3.3 | 3 | 🔵 N/A | ArcadeHaven system is monolithic there is no inter-service communication |

---

## V8.4 — Other Authorization Consideration

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V8.4.1 | 2 | 🔵 N/A | ArcadeHaven is not a multi-tenant system so all users belong to the same application context |
| V8.4.2 | 3 | ⚠️ Planned | Administrative operations are restricted to Admin role |

---

## Summary

| Section | Total | ⚠️ Planned | 🔵 N/A |
|---|---|---|---|
| V8.1 Authorization Document | 4 | 2 | 2 |
| V8.2 General Authorization Design | 4 | 3 | 1 |
| V8.3 Operation Level Authorization | 3 | 2 | 1 |
| V8.4 Other Authorization Consideration | 2 | 1 | 1 |
| **Total** | **13** | **8** | **5** |