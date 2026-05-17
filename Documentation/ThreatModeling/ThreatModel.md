[← Back to index page](../Overview/overview.md)

# ArcadeHaven — System Overview
### Threat Modeling Phase 1 | DESOFS 2025/26

---

## 1. Threat Model Information

| Field | Value |
|---|---|
| **Application Name** | ArcadeHaven |
| **Description** | Platform for digital sale and distribution of video games, supporting game catalog management, purchase flows, invoice generation, and personal game libraries. |
| **Document Owner** | desofs2026-wed_nap_2 |
| **Reviewer** | Nuno Pereira (NAP), Paulo Baltarejo Sousa (PBS) |

---

## 2. Application Description

ArcadeHaven is a web-based digital video game store built with a **Java Spring Boot** back-end, secured with **Spring Security + JWT + Keyclock**, backed by a **PostgreSQL** relational database, and containerised with **Docker**. The platform supports three distinct user roles — **Admin**, **Publisher**, and **Buyer** — and is structured around four DDD aggregates: **User**, **Game**, **Order**, and **Library**.

The core business flow is:

1. A **Publisher** submits a game (status: `PENDING`).
2. An **Admin** reviews and approves or rejects it (status: `ACTIVE` or `REJECTED`).
3. A **Buyer** creates an order containing one or more active games.
4. Upon order completion, the system generates a **PDF invoice**, produces an **activation key** (UUID-based), and adds the game to the buyer's personal **Library**.

The system also integrates with the external **RAWG.io REST API** to enrich game metadata.

---

## 3. External Dependencies

| Description |
|---|
| The Spring Boot application runs inside a **Docker container**, orchestrated via **Docker Compose**. All services (API, DB, Keycloak) are containerised. |
| The database is **PostgreSQL**, running in a dedicated Docker container. Schema migrations are managed by **Flyway**. The API container connects to the DB over an internal Docker network. |
| The connection between the Spring Boot API and the PostgreSQL database is over a **private Docker internal network**, not exposed externally. |
| The system uses **Keycloak** as an external Identity and Access Management (IAM) service, running in its own container, responsible for authentication and authorization via **OAuth2** and **OpenID Connect**. |
| **Keycloak** issues **JWT tokens** that are consumed by the frontend and validated by the backend using **Spring Security**. |
| All client-to-server communications must use **HTTPS exclusively**, including secure communication with the Keycloak server. |
| The system uses the external **RAWG.io REST API** for game data enrichment of our database. |
| **Sensitive configuration** (DB credentials, API keys, Keycloak client secrets) must be injected via environment variables and never hard-coded. |
| The **GitHub Actions** CI/CD pipeline runs verifications on every push. Static analysis is a mandatory gate. |
---

## 4. Entry Points

| Name | Description                                                                                                                                                                                      | Trust Level |
|---|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---|
| HTTPS API Port | All REST API endpoints are exposed over HTTPS. This is the single external entry point for all actors.                                                                                           | Anonymous User, Authenticated Buyer, Authenticated Publisher, Authenticated Admin |
| Registration Endpoint (`POST /auth/register`) | Allows a guest to create a new account by providing username, email, and password. No authentication required. Email and username must be unique (RF-01).                                        | Anonymous User |
| Login Endpoint (`POST /auth/login`) | Accepts credentials and returns a JWT token. Subject to rate limiting to prevent brute force.                                                                                                    | Anonymous User, User with Invalid Credentials |
| Authenticated API Endpoints | All endpoints beyond registration and login require a valid JWT bearer token. Role-based access control governs which operations each role can perform.                                          | Authenticated Buyer, Publisher, Admin |
| Game Image Upload (`POST /games/{id}/images`) | Publishers can upload game images (JPEG/PNG). Restricted by file type and size (max 25MB).                                                                                                       | Authenticated Publisher |
| Invoice Download (`GET /orders/{id}/invoice`) | Buyers can download the PDF invoice for a completed order. Access restricted to the order owner.                                                                                                 | Authenticated Buyer |
| RAWG API (External Outbound) | The server makes outbound HTTP requests to `api.rawg.io` to fetch game metadata. This is an **exit/entry point at the server-external boundary**  data returned from RAWG flows into the system. | System (no auth on RAWG side) |

---

## 5. Exit Points

| Name | Description                                                                                                                                                                                                    |
|---|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| API JSON Responses | All REST endpoints return JSON responses. Sensitive data (passwords, JWT internals) must never appear in responses. Error messages must be generic and not disclose internal details (stack traces, SQL errors). |
| PDF Invoice Files | Generated PDF invoices are stored on the server filesystem and served to buyers on request.                                                                                                                    |
| Activation Key Files | Generated activation keys (UUID) are stored as files on the server filesystem and included in order completion responses.                                                                                      |
| Game Image Files | Uploaded images are stored on the server and served back for the game catalogue.                                                                                                                               |
| Application Logs | The system logs critical security events: login attempts, role changes, order creation. Logs must not contain sensitive data (passwords, full activation keys).                                        |
| RAWG Outbound Requests | HTTP requests made to the RAWG.io API. Query parameters must not include sensitive user data.                                                                                                                  |

---

## 6. Assets

| Name | Description | Trust Level Required |
|---|---|---|
| User Credentials | Username, email, and BCrypt-hashed passwords stored in the `users` table. Compromise enables account takeover or enumeration. | Admin, DB Administrator |
| JWT Signing Secret | The secret used to sign and verify JWT tokens. Compromise allows forging of arbitrary tokens for any user or role. | System (env variable only) |
| Activation Keys | UUID-based keys generated per purchased game, stored as files. Keys grant access to a game title. | Authenticated Buyer (owner), Admin |
| PDF Invoices | Financial documents containing order details and buyer information. Stored on server filesystem. | Authenticated Buyer (owner), Admin |
| Game Data | Titles, descriptions, prices, categories, and images submitted by publishers and approved by admins. Tampering could damage platform reputation or defraud buyers. | Authenticated Publisher (own games), Admin |
| Order & Payment Records | Order history, items, amounts, and status stored in the `orders` and `order_items` tables. | Authenticated Buyer (own orders), Admin |
| Library Entries | Records of which buyers own which games, used for access control to activation keys and downloads. | Authenticated Buyer (own library), Admin |
| Game Image Files | Uploaded game images on the server filesystem. Malicious uploads could compromise the server or serve malware. | Authenticated Publisher (own games), Admin |
| Uploaded File Storage | Server filesystem directories for images, invoices, and activation keys. Path traversal or misconfiguration could expose all files. | System (internal) |
| RAWG API Data | External game metadata. Tampered or malicious RAWG responses could introduce corrupt data. | System (external, untrusted) |
| Application Logs | Security audit trail. Tampering with logs could hide attack evidence. | Admin, System |
| Database | PostgreSQL instance containing all persistent data. Direct access bypasses all application-level security controls. | DB Administrator |

---

## 7. Trust Levels

| Name | Description |
|---|---|
| Anonymous User | A user who has reached the API but has not authenticated. Can only access registration and login. |
| User with Valid Credentials (JWT) | A user who has successfully authenticated and holds a valid JWT. Base level for all authenticated operations. |
| User with Invalid Credentials | A user attempting to authenticate with invalid credentials. Must be subject to rate limiting. |
| Authenticated Buyer | A `BUYER`-role user. Can browse games, create orders, view their library and invoices. |
| Authenticated Publisher | A `PUBLISHER`-role user. Can submit and manage their own games, upload images, view sales metrics. |
| Authenticated Administrator | An `ADMIN`-role user. Full system management: user activation/deactivation, role changes, game approval/rejection, library entry suspension. |
| System (Internal Process) | Automated system actions: order completion, library population, PDF/key generation, Flyway migrations, RAWG API calls. |
| RAWG.io API | External third-party API. Data returned is **untrusted** and must be validated before use. |
| Database Administrator | Direct access to PostgreSQL. Bypasses all application-level controls. Highest risk if compromised. |
 | CI/CD Pipeline | GitHub Actions runner. Executes SAST (SonarQube) and automated tests. Has access to build secrets. |

---

## 8. Technology Stack Summary

| Component | Technology                                | Security Relevance |
|---|-------------------------------------------|---|
| Back-end | Java 21 + Spring Boot                     | Application logic, REST API surface |
| Security | Spring Security + JWT (BCrypt) + Keycloak | Authentication, authorisation, password hashing |
| Database | PostgreSQL + JPA/Hibernate                | Persistent data store; SQL injection risk mitigated by ORM |
| Migrations | Flyway                                    | Schema integrity; migration scripts must be protected |
| File System | Local server storage                      | Image uploads, PDF invoices, activation key files — path traversal risk |
| External API | RAWG.io REST API                          | Untrusted external data source |
| Containerisation | Docker + Docker Compose                   | Deployment isolation; misconfigured networks = lateral movement risk |
| CI/CD | GitHub Actions                            | SAST gate; pipeline secret exposure risk |
| SAST | SonarQube                                 | Static code analysis on every push |
| Testing | JUnit + Mockito (≥80% coverage)           | Regression and unit testing |

