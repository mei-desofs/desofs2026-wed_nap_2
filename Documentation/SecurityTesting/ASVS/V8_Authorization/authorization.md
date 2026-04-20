# Authorization — ASVS Security Requirements (V8.1 – V8.4)
## ArcadeHaven | Phase 1


> **Version:** ASVS 5.0
> **Status:** Phase 1 — Analysis & Design
> **Note:** ArcadeHaven implements role-based authorization. Access control is enforced at the backend service layer and is based on user roles.

---

## V8.1 — Authorization Document 

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V8.1.1 | 1 |  Planned | Authorization will be based on role-based access control, defining permissions for roles |
| V8.1.2 | 2 |  Planned | Field-level access control will be enforced at the API response layer using DTO projections and service-level filtering to prevent exposure of sensitive attributes |
| V8.1.3 | 3 |  N/A | Authorization decision are not based on contextual attributes |
| V8.1.4 | 3 |  N/A | No adaptative or risk-based authorization are enforced |

---

## V8.2 — General Authorization Design

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V8.2.1 | 1 |  Planned | Functional authorization will be enforced using Spring Security method-level security based on user roles |
| V8.2.2 | 1 |  Planned | Data-level authorization will enforce ownership checks |
| V8.2.3 | 2 |  Planned | Sensitive fields will be excluded from API responses using DTO mapping and controlled serialization to prevent data leakage |
| V8.2.4 | 3 |  N/A | No contextual or adaptative authorization controls are not implemented |

---

## V8.3 — Operation Level Authorization

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V8.3.1 | 1 |  Planned | Authorization will be enforced at the service layer using Spring Security annotations and centralized security checks |
| V8.3.2 | 3 |  Planned | Role changes will be reflected through token re-issuance on next authentication or refresh flow |
| V8.3.3 | 3 |  N/A | ArcadeHaven system is monolithic; no inter-service authorization propagation is required |

---

## V8.4 — Other Authorization Consideration

| Req ID | Level | Status | Observations |
|---|---|---|---|
| V8.4.1 | 2 |  N/A | ArcadeHaven is not a multi-tenant system so all users belong to the same application context |
| V8.4.2 | 3 |  Planned | Administrative operations are restricted to users with the Admin role and enforced at the service layer |

---

## Summary

| Section | Total |  Planned |  N/A |
|---|---|---|---|
| V8.1 Authorization Document | 4 | 2 | 2 |
| V8.2 General Authorization Design | 4 | 3 | 1 |
| V8.3 Operation Level Authorization | 3 | 2 | 1 |
| V8.4 Other Authorization Consideration | 2 | 1 | 1 |
| **Total** | **13** | **8** | **5** |