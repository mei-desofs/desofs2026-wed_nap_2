# V15 — Secure Coding Practices — ASVS 5.0 Assessment

## 1. Overview

This document assesses all V15 ASVS 5.0 requirements against ArcadeHaven — a Java/Spring Boot REST API for digital game sales. ArcadeHaven has three user roles (administrator, publisher, buyer), uses JWT Bearer authentication, BCrypt password hashing, Maven for dependency management, Docker for containerisation, and integrates with the RAWG API for game metadata enrichment.

V15 covers four areas:
- **V15.1** — Secure Coding and Architecture Documentation: what must be documented about third-party components and risky functionality
- **V15.2** — Security Architecture and Dependencies: runtime enforcement of what was documented in V15.1
- **V15.3** — Defensive Coding: defensive patterns in Java/Spring such as DTO isolation, mass assignment protection, and input handling
- **V15.4** — Safe Concurrency: thread-safety and race condition prevention (all Level 3)

All Level 3 requirements (V15.1.4, V15.1.5, V15.2.4, V15.2.5, V15.4.1–V15.4.4) are outside the mandatory ASVS Level 2 scope for ArcadeHaven and are documented for reference only.

**Critical note on V15.3.6:** Prototype pollution is a JavaScript/Node.js vulnerability class. ArcadeHaven is a Java/Spring Boot backend with no JavaScript runtime. V15.3.6 is Not Applicable to this project.

---

## 2. V15.1 — Secure Coding and Architecture Documentation

### V15.1.1 — Remediation Time Frames (Level 1)

**Requirement:** Verify that application documentation defines risk-based remediation time frames for 3rd party component versions with vulnerabilities and for updating libraries in general.

**ArcadeHaven assessment:** ArcadeHaven's dependencies are managed through Maven (pom.xml). A risk-based remediation policy is being defined:
- Critical vulnerabilities (CVSS >= 9.0): remediation within 30 days
- High vulnerabilities (CVSS 7.0–8.9): remediation within 90 days
- Medium vulnerabilities (CVSS 4.0–6.9): remediation within 180 days
- Low/informational: tracked and addressed in the next scheduled dependency update cycle

This policy is consistent with standard vulnerability management practice for Java REST API applications and will be enforced through OWASP Dependency Check integration.

**Status:** In Progress

---

### V15.1.2 — SBOM Inventory (Level 2)

**Requirement:** Verify that an inventory catalog (SBOM) is maintained of all third-party libraries in use, including verifying that components come from pre-defined, trusted, and continually maintained repositories.

**ArcadeHaven assessment:** The pom.xml file provides a complete list of all direct third-party dependencies. All dependencies are sourced from Maven Central, a pre-defined, trusted, and continually maintained repository. A formal SBOM in CycloneDX or SPDX format is being generated via the `cyclonedx-maven-plugin`, which produces a machine-readable inventory including transitive dependencies.

**Status:** In Progress

---

### V15.1.3 — Resource-Demanding Functionality Documentation (Level 2)

**Requirement:** Verify that the application documentation identifies functionality which is time-consuming or resource-demanding, and includes how to prevent a loss of availability.

**ArcadeHaven assessment:** The following resource-demanding operations are identified in ArcadeHaven's architecture:

| Operation | Resource concern | Documented defense |
|-----------|-----------------|-------------------|
| File upload (publishers) | Disk I/O, memory buffering | FO-001: max file size limit; FO-002: allowed MIME types; rate limiting (AC-006) |
| PDF invoice generation (iText) | CPU, memory | Triggered only on confirmed order completion (RF-16, RF-26); bounded input from database |
| RAWG API call | Network I/O, external dependency | RNF-16: fallback to locally stored metadata; configurable request timeout |
| Activation key generation | CSPRNG entropy | Single call per order line; SecureRandom is non-blocking on modern JVMs |

Rate limiting (RNF-09, AC-006) applies across all endpoints, limiting repeated invocations of resource-intensive operations.

**Status:** In Progress

---

### V15.1.4 — Risky Third-Party Components (Level 3)

**Requirement:** Verify that application documentation highlights third-party libraries which are considered to be "risky components".

**Status:** Not Started — Level 3 requirement, outside the mandatory Level 2 scope for ArcadeHaven.

---

### V15.1.5 — Dangerous Functionality Areas (Level 3)

**Requirement:** Verify that application documentation highlights parts of the application where "dangerous functionality" is being used.

**ArcadeHaven assessment:** Although a Level 3 requirement, ArcadeHaven's V15.1.3 analysis already identifies and describes the dangerous functionality areas:
- **File upload endpoint** — accepts multipart files from publishers; MIME type spoofing and path traversal risk (FO-001, FO-002)
- **iText PDF invoice generation** — server-side file generation from order data; unbounded input or malformed data could affect resource consumption (RF-16, RF-26)
- **RAWG API HTTP client** — outbound HTTP call to an external service; SSRF-adjacent risk if the target URL were user-controlled (RF-12, RNF-16)
- **Activation key generation** — CSPRNG entropy dependency; weak generation would produce guessable keys (RNF-08, SC-003)

A dedicated "Dangerous Functionality" section is being added to the security architecture documentation to formally satisfy V15.1.5 framing, referencing these areas with explicit mitigations.

**Status:** In Progress

---

## 3. V15.2 — Security Architecture and Dependencies

### V15.2.1 — No Breached Remediation Time Frames (Level 1)

**Requirement:** Verify that the application only contains components which have not breached the documented update and remediation time frames.

**ArcadeHaven assessment:** Dependencies are version-pinned in pom.xml. OWASP Dependency Check is being integrated into the CI/CD pipeline to scan all direct and transitive dependencies against the NVD (National Vulnerability Database) on each build. Any component with a CVE exceeding the documented remediation timeframe (V15.1.1) blocks the build. No known high or critical CVEs have been identified in the current dependency set.

**Status:** In Progress

---

### V15.2.2 — Defenses Against Availability Loss (Level 2)

**Requirement:** Verify that the application has implemented defenses against loss of availability due to functionality which is time-consuming or resource-demanding.

**ArcadeHaven assessment:** Defenses are implemented at multiple levels:
- **File uploads:** maximum file size enforced at the Spring multipart layer (FO-001); MIME type validation rejects unexpected content (FO-002)
- **PDF generation:** iText processes structured data from the database, not user-controlled raw input; generation is triggered only after order confirmation
- **RAWG API calls:** configurable HTTP client timeout; fallback to locally stored game metadata if the external API is unavailable (RNF-16)
- **Rate limiting:** AC-006 and RNF-09 apply rate limits to all security-sensitive and high-volume endpoints, preventing repeated triggering of resource-intensive operations

**Status:** In Progress

---

### V15.2.3 — Production Environment Minimal Functionality (Level 2)

**Requirement:** Verify that the production environment only includes functionality that is required for the application to function, and does not expose extraneous functionality such as test code, sample snippets, and development functionality.

**ArcadeHaven assessment:** ArcadeHaven is containerised with Docker (RNF-21, RNF-22). Spring Boot application profiles separate development and production configurations:
- Swagger/OpenAPI UI endpoints are disabled in the production profile
- The H2 in-memory database console is not available in production (production uses a dedicated database server)
- Spring Boot Actuator management endpoints, if used, are restricted to localhost or removed from the production build
- Test utilities, fixtures, and seeding scripts are not included in the production Docker image

**Status:** In Progress

---

### V15.2.4 — No Dependency Confusion (Level 3)

**Requirement:** Verify that third-party components and all of their transitive dependencies are included from the expected repository, and that there is no risk of a dependency confusion attack.

**Status:** Not Started — Level 3 requirement, outside the mandatory Level 2 scope for ArcadeHaven.

---

### V15.2.5 — Additional Protections Around Dangerous Functionality (Level 3)

**Requirement:** Verify that the application implements additional protections around parts documented as containing "dangerous functionality" or using third-party libraries considered "risky components". This could include techniques such as sandboxing, encapsulation, containerization or network level isolation.

**ArcadeHaven assessment:** Although a Level 3 requirement, Docker containerisation — the deployment model already in place (RNF-21, RNF-22) — directly satisfies the requirement's explicit examples of "containerization" and "network-level isolation":
- The entire application, including the file upload handler, iText PDF generator, and RAWG API client, runs inside a Docker container with no direct host filesystem or network access
- The database is isolated in a separate container; the application container connects only to the database container over the Docker internal network
- No process running in the application container can reach the Docker host or other network services without explicit port mappings

Additional hardening (Linux seccomp profiles, AppArmor policies, read-only container filesystem) is being evaluated as part of the Docker deployment hardening specification.

**Status:** In Progress

---

## 4. V15.3 — Defensive Coding

### V15.3.1 — Return Only Required Fields (Level 1)

**Requirement:** Verify that the application only returns the required subset of fields from a data object.

**ArcadeHaven assessment:** ArcadeHaven uses dedicated response DTO classes for all API responses. Entity classes (User, Game, Order, ActivationKey) are never directly serialised into API responses. The response DTOs include only the fields appropriate for the requesting role:
- `UserResponseDTO`: includes userId, username, email, role — never includes passwordHash
- `GameResponseDTO`: includes gameId, title, description, price, imageUrl — never includes internal metadata
- `OrderResponseDTO`: includes orderId, status, totalAmount, orderDate — activation keys included only in the library endpoint for authenticated buyers
- Activation keys are returned only once upon purchase and never again exposed in list operations

**Status:** In Progress

---

### V15.3.2 — No Unintended Redirect Following (Level 2)

**Requirement:** Verify that where the application backend makes calls to external URLs, it is configured to not follow redirects unless it is intended functionality.

**ArcadeHaven assessment:** The only outbound HTTP call in ArcadeHaven is to the RAWG API for game metadata enrichment (RF-12). The HTTP client (RestTemplate or WebClient) is explicitly configured to disable automatic redirect-following. An HTTP redirect from the RAWG API would be anomalous and could indicate a DNS hijack or response manipulation; the application will fail the request rather than follow the redirect.

**Status:** In Progress

---

### V15.3.3 — Mass Assignment Protection (Level 2)

**Requirement:** Verify that the application has countermeasures to protect against mass assignment attacks by limiting allowed fields per controller and action.

**ArcadeHaven assessment:** Spring Boot uses dedicated request DTO classes for each endpoint operation. Jackson deserialisation operates on DTO fields only — any extra fields in the request body are ignored by default. JPA entities are never directly bound to request bodies. Fields that must not be modifiable by users — such as role, userId, createdAt, isApproved, activationKeyValue — are absent from request DTOs. Role assignment is performed only through the administrator role-management endpoint (RF-07) with explicit authorization checks (AC-004).

**Status:** In Progress

---

### V15.3.4 — Correct IP Address Forwarding (Level 2)

**Requirement:** Verify that all proxying and middleware components transfer the user's original IP address correctly using trusted data fields that cannot be manipulated by the end user, and that the application uses this value for logging and security decisions such as rate limiting.

**ArcadeHaven assessment:** The Docker deployment configuration is defining the reverse proxy layer to forward the original client IP via the `X-Forwarded-For` header. The application reads the IP exclusively from this trusted header (set only by the proxy, not by the end user) for rate limiting (AC-006) and security event logging (LM-001). The proxy configuration must strip any client-supplied `X-Forwarded-For` headers before appending its own, preventing IP spoofing at the application boundary.

**Status:** In Progress

---

### V15.3.5 — Strict Type Checking (Level 2)

**Requirement:** Verify that the application explicitly ensures that variables are of the correct type and performs strict equality and comparator operations, to avoid type juggling or type confusion vulnerabilities.

**ArcadeHaven assessment:** Java is a statically and strongly typed language with no implicit type coercion. All API inputs are declared as typed DTO fields annotated with Bean Validation constraints (`@NotNull`, `@Positive`, `@Email`, `@Size`). Jackson's deserialiser rejects type mismatches — a string submitted where a Long is expected returns a 400 Bad Request before reaching business logic. There is no type juggling equivalent in Java; comparisons are performed using `.equals()` for objects and `==` for primitives, both of which are type-safe.

**Status:** In Progress

---

### V15.3.6 — Prototype Pollution Prevention (Level 2)

**Requirement:** Verify that JavaScript code is written in a way that prevents prototype pollution (e.g., using Set() or Map() instead of object literals).

**Applicability:** Not Applicable. Prototype pollution is a vulnerability class specific to JavaScript/Node.js environments, where user-controlled input can modify the Object prototype and affect unrelated code paths. ArcadeHaven is a Java/Spring Boot backend with no JavaScript runtime component. This vulnerability class is architecturally inapplicable.

**Status:** Not Applicable

---

### V15.3.7 — HTTP Parameter Pollution Defenses (Level 2)

**Requirement:** Verify that the application has defenses against HTTP parameter pollution attacks, particularly if the application framework makes no distinction about the source of request parameters.

**ArcadeHaven assessment:** Spring MVC processes query parameters, request body, and header fields through separate and explicit binding mechanisms. Query parameters are bound to `@RequestParam` (typed); request body to `@RequestBody` (DTO); path variables to `@PathVariable` (typed). Duplicate query parameters produce a list value, which Spring's typed binding rejects for single-value fields (400 Bad Request). Input validation (IV-001) applies whitelist validation to all parameters before they reach business logic, preventing parameter pollution payloads from influencing query construction or access control decisions.

**Status:** In Progress

---

## 5. V15.4 — Safe Concurrency

All V15.4 requirements are Level 3 and are outside the mandatory ASVS Level 2 scope for ArcadeHaven. They are documented here for completeness.

### V15.4.1 — Thread-Safe Shared Objects (Level 3)

Spring Boot applications are multi-threaded by default (each HTTP request is handled in a separate thread from the container thread pool). Spring beans are singletons by default. Thread-safety in ArcadeHaven depends on:
- Spring components (controllers, services, repositories) being stateless — no instance variables storing request-scoped data
- JPA/Hibernate session management being request-scoped (EntityManager per request)
- No in-memory caches or shared mutable objects outside of the framework

A formal thread-safety audit of all Spring beans and shared state has not been performed. **Status: Not Started**

### V15.4.2 — Atomic TOCTOU Prevention (Level 3)

The most relevant TOCTOU scenario in ArcadeHaven is activation key assignment: a key must be checked as unassigned before being assigned to a buyer. This should be implemented as a single atomic database operation (SELECT FOR UPDATE or a conditional UPDATE) rather than a separate check followed by an update. **Status: Not Started**

### V15.4.3 — Consistent Lock Usage (Level 3)

No custom locking logic is implemented in ArcadeHaven at the application layer. Database-level locking is delegated to JPA/Hibernate pessimistic or optimistic locking. A formal review of all lock usage patterns has not been performed. **Status: Not Started**

### V15.4.4 — Thread Starvation Prevention (Level 3)

ArcadeHaven relies on the Spring Boot embedded server (Tomcat/Jetty) thread pool for request handling. Thread pool sizing, queue depth, and timeout configuration are infrastructure concerns. No custom thread allocation policy has been designed for ArcadeHaven. **Status: Not Started**

---

## 6. Traceability Threads

### Thread A — Dependency Management

V15.1.1 (remediation policy) → V15.2.1 (no breached timeframes) → pom.xml + OWASP Dependency Check CI integration

### Thread B — Resource-Demanding Functionality

V15.1.3 (documentation) → V15.2.2 (runtime defenses) → FO-001 (upload limits) + AC-006 (rate limiting) + RNF-16 (RAWG fallback)

### Thread C — Minimal Production Footprint

V15.2.3 (production configuration) → Docker Spring profiles → disabled Swagger/Actuator in production (RNF-21/22)

### Thread D — Mass Assignment Protection

V15.3.3 → DTO pattern in Spring Boot → no JPA entity binding to request bodies

### Thread E — Data Minimisation in Responses

V15.3.1 → Response DTOs → password hashes and activation keys excluded from all non-purchase responses

---

## 7. Identified Gaps

| Gap | Requirement | Impact | Action required |
|-----|-------------|--------|----------------|
| No formal SBOM generated | V15.1.2 | Incomplete dependency traceability | Add cyclonedx-maven-plugin to build |
| OWASP Dependency Check not in CI | V15.2.1 | Vulnerabilities may go undetected | Configure OWASP DC as a CI build step |
| Reverse proxy IP forwarding not yet configured | V15.3.4 | Rate limiting and logging may use proxy IP, not client IP | Define Nginx/proxy configuration for X-Forwarded-For |
| TOCTOU risk in activation key assignment | V15.4.2 | Concurrent purchases could result in double-assignment | Implement SELECT FOR UPDATE or conditional UPDATE |
