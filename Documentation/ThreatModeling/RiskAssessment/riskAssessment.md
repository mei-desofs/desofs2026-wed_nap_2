# Risk Assessment

**Project:** ArcadeHaven — DESOFS 2026  
**Methodology:** OWASP Risk Rating Methodology  

---

## 1. Introduction

This document presents the risk assessment for the ArcadeHaven platform. While the Threat Identification document classifies threats by type (STRIDE), this document **prioritises them** by calculating a risk score for each threat using a well-defined methodology, so that the development team can direct security effort where it matters most.

Risk assessment answers three questions that STRIDE alone does not:
- **How likely is this threat to be exploited?**
- **What is the business impact if it is?**
- **Which threats should be fixed first?**

---

## 2. Risk Assessment Methodology

### 2.1 OWASP Risk Rating Methodology

This assessment uses the **OWASP Risk Rating Methodology**, which calculates risk as:

```
Risk = Likelihood × Impact
```

Each dimension is scored on a scale of **1 to 9** by combining multiple sub-factors, and the final risk level is mapped to: **Critical / High / Medium / Low / Note**.

---

### 2.2 Likelihood Factors

Likelihood is the probability that a threat will be successfully exploited. It is composed of two groups:

#### Threat Agent Factors
Assess how capable and motivated the attacker is.

| Factor | Description | Score Range |
|--------|-------------|-------------|
| **Skill Level** | How technically skilled is the threat agent? (1=no skills, 9=penetration tester) | 1–9 |
| **Motive** | How motivated is the agent to find and exploit this vulnerability? (1=low, 9=high reward) | 1–9 |
| **Opportunity** | What resources does the attacker need? (1=full access required, 9=no access required) | 1–9 |
| **Size** | How large is the group of threat agents? (1=developers, 9=anonymous internet users) | 1–9 |

#### Vulnerability Factors
Assess how easy the vulnerability is to discover and exploit.

| Factor | Description | Score Range |
|--------|-------------|-------------|
| **Ease of Discovery** | How easy is it to discover the vulnerability? (1=practically impossible, 9=automated tools) | 1–9 |
| **Ease of Exploit** | How easy is it to exploit the vulnerability? (1=theoretical, 9=automated tools available) | 1–9 |
| **Awareness** | How well known is this vulnerability type? (1=unknown, 9=publicly known) | 1–9 |
| **Intrusion Detection** | How likely is exploitation to be detected? (1=always detected, 9=not logged) | 1–9 |

**Likelihood Score** = Average of all 8 factors above (1–9 scale)

---

### 2.3 Impact Factors

Impact is the business damage caused if the threat is realised. It is composed of two groups:

#### Technical Impact Factors

| Factor | Description | Score Range |
|--------|-------------|-------------|
| **Loss of Confidentiality** | How much data is disclosed, and how sensitive? (1=minimal non-sensitive, 9=all data, highly sensitive) | 1–9 |
| **Loss of Integrity** | How much data can be corrupted, and how damaging? (1=slightly corrupt non-sensitive, 9=all data completely corrupt) | 1–9 |
| **Loss of Availability** | How much service is lost, and for how long? (1=minimal secondary services, 9=all services fully interrupted) | 1–9 |
| **Loss of Accountability** | Are actions traceable to individuals? (1=fully traceable, 9=completely anonymous) | 1–9 |

#### Business Impact Factors

| Factor | Description | Score Range |
|--------|-------------|-------------|
| **Financial Damage** | How much financial damage results? (1=less than cost to fix, 9=bankruptcy) | 1–9 |
| **Reputation Damage** | How much reputational damage results? (1=minimal, 9=brand destroyed) | 1–9 |
| **Non-Compliance** | How much regulatory/legal exposure results? (1=minor violation, 9=high-profile violation, criminal liability) | 1–9 |
| **Privacy Violation** | How much user privacy is affected? (1=one user minimally, 9=all users severely) | 1–9 |

**Impact Score** = Average of all 8 factors above (1–9 scale)

---

### 2.4 Risk Level Matrix

| | **Low Impact (1–3)** | **Medium Impact (4–6)** | **High Impact (7–9)** |
|---|---|---|---|
| **High Likelihood (7–9)** | Medium | High | **Critical** |
| **Medium Likelihood (4–6)** | Low | Medium | High |
| **Low Likelihood (1–3)** | Note | Low | Medium |

---

### 2.5 Risk Level Definitions

| Risk Level | Description | Response |
|------------|-------------|----------|
| **Critical** | Immediate threat to the business; exploitation is likely and impact is catastrophic | Fix immediately before deployment |
| **High** | Significant risk; exploitation is feasible and impact is severe | Fix in current sprint |
| **Medium** | Moderate risk; exploitation requires some conditions; impact is limited in scope | Fix in next release |
| **Low** | Low probability or limited impact; exploitable but with significant barriers | Fix as time allows |
| **Note** | Informational; negligible risk under current conditions | Monitor; document |

---

## 3. Risk Register

The following register documents the **top-priority threats** selected from the full threat catalogue. Threats were selected for this register based on their Critical or High STRIDE severity rating combined with their business context within ArcadeHaven.

Each row provides the complete OWASP scoring justification.

---

### 3.1 Critical Risks

---

#### RISK-01 — Activation Keys Stored in Plaintext (Library Datastore)

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1C-S-07, L1C-F-12 |
| **STRIDE Category** | Information Disclosure |
| **Element** | Library Datastore / Read/Write Library Data Flow |
| **Threat** | Activation keys stored without encryption; read flow returns plaintext keys in DB response |

**Likelihood Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Skill Level | 5 | SQL injection or insider threat requires moderate skill |
| Motive | 9 | Activation keys have direct monetary value on resale markets |
| Opportunity | 6 | DB accessible to application; insider has direct access |
| Size | 7 | Includes insiders, external attackers with DB access, SQL injection exploiters |
| Ease of Discovery | 7 | DB schema inspection reveals plaintext activation_key column immediately |
| Ease of Exploit | 6 | SQL injection or direct DB query retrieves keys in one step |
| Awareness | 8 | Plaintext credential storage is a well-known vulnerability class |
| Intrusion Detection | 7 | Bulk key read may not trigger anomaly detection if logging is insufficient |
| **Likelihood Score** | **6.9** | **→ High Likelihood** |

**Impact Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Loss of Confidentiality | 9 | All activation keys exposed; each has real monetary value |
| Loss of Integrity | 5 | Keys could be burned (marked used) before legitimate owner activates |
| Loss of Availability | 3 | Service remains available; only key confidentiality is affected |
| Loss of Accountability | 6 | Bulk key reads may not be attributable without detailed query logging |
| Financial Damage | 9 | Every exposed key represents a purchased game that can be stolen |
| Reputation Damage | 8 | Buyers who paid for games lose their keys; major trust damage |
| Non-Compliance | 6 | Potential PCI-DSS implications if payment and key data are linked |
| Privacy Violation | 4 | Activation keys not PII but linked to specific user purchases |
| **Impact Score** | **6.3** | **→ High Impact** |

**Risk Level: CRITICAL** (High Likelihood × High Impact)

**Justification:** Activation keys are the primary deliverable of every purchase on ArcadeHaven. Storing them in plaintext means a single SQL injection or insider access event exposes the entire key inventory. The combination of high attacker motivation (keys have direct resale value), easy discoverability, and catastrophic financial impact to both users and the platform makes this the highest-priority finding in the assessment.

**Required Mitigations:**
1. Encrypt all activation keys at the application layer (AES-256-GCM) before writing to the DB — the column stores only ciphertext
2. Encryption key managed via environment secrets (Docker secrets or HashiCorp Vault) — never in source code
3. Decryption occurs only in the delivery endpoint after ownership verification
4. Activation keys never appear in any log output, HTTP response cache, or error message

---

#### RISK-02 — Order Record Retroactive Modification (Order Datastore)

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1C-F-17, L1C-S-09 |
| **STRIDE Category** | Tampering |
| **Element** | Order Datastore / Read/Write Order Data Flow |
| **Threat** | Completed order records can be updated, changing financial amounts post-purchase |

**Likelihood Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Skill Level | 6 | Requires DB access or SQL injection skill |
| Motive | 8 | Financial fraud with direct monetary gain; cover-up of fraudulent orders |
| Opportunity | 5 | Requires DB access (insider) or SQL injection vulnerability in application |
| Size | 4 | Primarily insider threat or sophisticated external attacker |
| Ease of Discovery | 5 | Requires understanding the DB schema; moderate effort |
| Ease of Exploit | 5 | Standard UPDATE statement once DB access is obtained |
| Awareness | 7 | Financial data tampering is a known and sought-after attack vector |
| Intrusion Detection | 6 | UPDATE on completed order may not trigger specific alerts without pgaudit |
| **Likelihood Score** | **5.8** | **→ Medium-High Likelihood** |

**Impact Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Loss of Confidentiality | 3 | Tampering does not primarily affect confidentiality |
| Loss of Integrity | 9 | Financial records falsified; invoice PDF and DB disagree; audit trail destroyed |
| Loss of Availability | 2 | Service remains available |
| Loss of Accountability | 8 | Modified records obscure what was actually charged; audit becomes impossible |
| Financial Damage | 9 | Direct financial fraud; potential tax evasion or accounting fraud liability |
| Reputation Damage | 8 | Platform financial records shown to be unreliable; catastrophic for a payment platform |
| Non-Compliance | 9 | Violation of financial record-keeping regulations; accounting fraud |
| Privacy Violation | 4 | Order modification could hide illegal purchases |
| **Impact Score** | **6.5** | **→ High Impact** |

**Risk Level: CRITICAL** (Medium-High Likelihood × High Impact)

**Justification:** An e-commerce platform that allows its financial records to be modified post-completion has no trustworthy audit trail. This threat exposes ArcadeHaven to financial fraud, tax compliance violations, and potential criminal liability. The business impact score is the highest in this register because it affects the integrity of every completed transaction on the platform.

**Required Mitigations:**
1. Orders table uses insert-only semantics after COMPLETED status — no UPDATE permitted
2. DB trigger raises exception on any attempt to UPDATE an order WHERE status = 'COMPLETED'
3. Invoice PDF SHA-256 hash stored alongside the order record and verified on every retrieval
4. Immutable order_events table records every state transition; financial data retained for 7 years

---

#### RISK-03 — Stored XSS via Unsanitised RAWG Metadata (Game Datastore / All Buyers)

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1D-F-09, L1A-P-16 |
| **STRIDE Category** | Information Disclosure / Tampering |
| **Element** | Game Metadata Handler / Game Datastore |
| **Threat** | RAWG description fields stored without HTML sanitisation; XSS payload executed in all buyers' browsers |

**Likelihood Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Skill Level | 5 | Stored XSS via a third-party API requires moderate understanding |
| Motive | 7 | Session token theft provides account access; scalable attack across entire user base |
| Opportunity | 7 | RAWG API is always queried during game creation; no authentication required to serve the exploit |
| Size | 6 | Supply-chain attacker or anyone who can influence RAWG data |
| Ease of Discovery | 6 | Reviewing the game detail page source reveals whether sanitisation is in place |
| Ease of Exploit | 6 | If RAWG data is unsanitised, the XSS executes on every page load automatically |
| Awareness | 9 | Stored XSS is one of the most well-known vulnerability classes (OWASP Top 10) |
| Intrusion Detection | 7 | XSS execution in user browsers is not visible in server logs |
| **Likelihood Score** | **6.6** | **→ High Likelihood** |

**Impact Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Loss of Confidentiality | 9 | Session tokens of all buyers viewing the game page are stolen |
| Loss of Integrity | 6 | Attacker with stolen session tokens can perform actions on behalf of all affected users |
| Loss of Availability | 3 | Service availability not primarily affected |
| Loss of Accountability | 7 | Actions performed with stolen tokens are attributed to the victim |
| Financial Damage | 8 | Mass account takeover enables fraudulent purchases; activation key theft |
| Reputation Damage | 9 | Platform shown to execute malicious scripts in users' browsers; catastrophic trust damage |
| Non-Compliance | 7 | GDPR breach if personal data accessed via stolen sessions |
| Privacy Violation | 8 | All buyers who view the affected game page are compromised |
| **Impact Score** | **7.1** | **→ High Impact** |

**Risk Level: CRITICAL** (High Likelihood × High Impact)

**Justification:** This threat has the widest blast radius in the entire system. A single compromised RAWG response (whether via supply-chain attack, DNS poisoning, or RAWG's own platform being exploited) could compromise every buyer who visits a game detail page. The exploit is persistent (stored), requires no user interaction beyond viewing a page, and is invisible to server-side monitoring. OWASP Top 10 A03:2021 (Injection) classifies this as a primary risk.

**Required Mitigations:**
1. All RAWG string fields HTML-sanitised using OWASP Java HTML Sanitizer with a strict allowlist (no script, no style, no iframe, no event attributes) before any storage operation
2. Output encoding applied at frontend render layer as a second line of defence
3. Content-Security-Policy header with script-src 'self' deployed on all game pages
4. RAWG data treated as untrusted external input regardless of source

---

#### RISK-04 — Full User PII Returned to Non-Owner Callers (User Datastore)

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1C-F-29, L1C-F-30 |
| **STRIDE Category** | Information Disclosure |
| **Element** | User Information Datastore / Read/Write User Data Flow |
| **Threat** | GET /users/{id} returns full PII to any authenticated caller without ownership check; PII also logged |

**Likelihood Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Skill Level | 2 | Requires only a valid JWT and knowledge of integer user IDs |
| Motive | 6 | PII has value for social engineering, spam, and targeted attacks |
| Opportunity | 8 | Any registered Buyer can call the endpoint with any user ID |
| Size | 8 | Any registered user of the platform; very large threat agent population |
| Ease of Discovery | 8 | Sequential ID enumeration is a trivial automated attack |
| Ease of Exploit | 9 | A for loop iterating user IDs with a valid Bearer token is sufficient |
| Awareness | 9 | IDOR (Insecure Direct Object Reference) is OWASP Top 10 A01:2021 |
| Intrusion Detection | 6 | Sequential ID requests may not trigger anomaly detection without rate-limiting logs |
| **Likelihood Score** | **7.0** | **→ High Likelihood** |

**Impact Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Loss of Confidentiality | 9 | All registered users' full PII exposed (name, email, address, date of birth) |
| Loss of Integrity | 2 | Read-only; integrity not directly affected |
| Loss of Availability | 1 | Service availability not affected |
| Loss of Accountability | 5 | Data reads are logged but volume of exfiltration may not trigger alerts |
| Financial Damage | 6 | Regulatory fines (GDPR) and breach notification costs |
| Reputation Damage | 9 | Mass PII breach destroys user trust; platform abandonment |
| Non-Compliance | 9 | GDPR Article 5 violation; mandatory breach notification; potential criminal liability |
| Privacy Violation | 9 | Every registered user's personal data exposed without consent |
| **Impact Score** | **6.3** | **→ High Impact** |

**Risk Level: CRITICAL** (High Likelihood × High Impact)

**Justification:** IDOR on user profile data is one of the easiest attacks to execute (a simple loop with a Bearer token) and has the most direct regulatory consequence. Under GDPR, a mass PII breach requires notification to the supervisory authority within 72 hours and to affected individuals without undue delay. The financial penalties (up to 4% of global annual turnover under GDPR Article 83) and reputational damage make this a Critical risk that must be resolved before the platform goes live.

**Required Mitigations:**
1. Every user profile read enforces: JWT sub must equal target user_id, OR caller holds ADMIN role
2. Dedicated minimal response DTO (display name, email only) for Buyer-facing responses
3. Automated IDOR test with two different user JWTs is mandatory in CI pipeline
4. IP-level rate limiting on user profile endpoints to slow enumeration attacks

---

#### RISK-05 — JWT Algorithm Confusion Attack — alg:none (Token Validation)

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1B-P-19 |
| **STRIDE Category** | Spoofing |
| **Element** | Token Validation Process (Auth API) |
| **Threat** | JWT with alg: none header accepted; signature verification skipped entirely |

**Likelihood Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Skill Level | 6 | Requires understanding of JWT structure and algorithm confusion attacks |
| Motive | 9 | Full authentication bypass; can impersonate any user including ADMIN |
| Opportunity | 8 | Only requires the ability to send HTTP requests to the API |
| Size | 5 | Skilled attacker; moderately sized threat agent population |
| Ease of Discovery | 7 | Automated JWT testing tools (jwt_tool) check this by default |
| Ease of Exploit | 8 | Base64-decode header, change to alg:none, re-encode, remove signature |
| Awareness | 9 | CVE-documented attack; featured in every JWT security checklist |
| Intrusion Detection | 7 | Token with alg:none may not trigger specific alerts if logging is insufficient |
| **Likelihood Score** | **7.4** | **→ High Likelihood** |

**Impact Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Loss of Confidentiality | 9 | Attacker can impersonate ADMIN and read all data |
| Loss of Integrity | 9 | Attacker can modify any record in any module as ADMIN |
| Loss of Availability | 7 | Attacker can delete games, users, orders |
| Loss of Accountability | 9 | Actions attributed to impersonated user; attacker fully anonymous |
| Financial Damage | 9 | Full platform compromise; financial records tampered; all activation keys accessible |
| Reputation Damage | 9 | Complete authentication bypass destroys all user trust |
| Non-Compliance | 9 | All GDPR, PCI-DSS, and financial regulations violated simultaneously |
| Privacy Violation | 9 | All user data accessible |
| **Impact Score** | **8.8** | **→ High Impact** |

**Risk Level: CRITICAL** (High Likelihood × High Impact)

**Justification:** Algorithm confusion attacks represent a complete authentication bypass. An attacker who successfully exploits this vulnerability has effectively bypassed all of ArcadeHaven's security controls simultaneously — they can act as any user including ADMIN with no cryptographic barrier. This is the highest-impact single vulnerability in the entire threat model. Despite being well-documented, it continues to appear in real-world systems using naive JWT validation libraries.

**Required Mitigations:**
1. Explicitly whitelist RS256 as the only accepted algorithm in Spring Security JWT configuration
2. Reject any token with alg:none or any unexpected algorithm before processing
3. Use Nimbus JOSE+JWT library which handles algorithm confusion attacks by design
4. Add automated security test submitting an alg:none token to every authenticated endpoint

---

#### RISK-06 — Role Response Tampered — Privilege Escalation (Auth API Internal Flow)

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1B-F-14 |
| **STRIDE Category** | Tampering |
| **Element** | Role Request/Response Flow (Token Validation ↔ Claim Role) |
| **Threat** | Role Response intercepted and ADMIN injected; attacker escalates to full admin without any Keycloak change |

**Likelihood Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Skill Level | 7 | Requires network interception capability and understanding of auth flow internals |
| Motive | 9 | Full privilege escalation to ADMIN |
| Opportunity | 4 | Requires network access to internal Keycloak communication path |
| Size | 3 | Insider or attacker who has already compromised internal network |
| Ease of Discovery | 4 | Requires mapping internal service communication; not trivially discoverable |
| Ease of Exploit | 5 | Once on the internal network, HTTP response modification is straightforward |
| Awareness | 6 | Role injection via intercepted internal responses is a known architectural weakness |
| Intrusion Detection | 8 | Internal network traffic typically not monitored for response content manipulation |
| **Likelihood Score** | **5.8** | **→ Medium Likelihood** |

**Impact Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Loss of Confidentiality | 9 | ADMIN access to all data |
| Loss of Integrity | 9 | ADMIN can modify any record |
| Loss of Availability | 7 | ADMIN can delete all content |
| Loss of Accountability | 9 | Actions attributed to any impersonated user |
| Financial Damage | 9 | Full platform compromise |
| Reputation Damage | 9 | Auth architecture shown to be fundamentally broken |
| Non-Compliance | 9 | All regulations violated |
| Privacy Violation | 9 | All user data accessible |
| **Impact Score** | **8.8** | **→ High Impact** |

**Risk Level: CRITICAL** (Medium Likelihood × High Impact)

**Justification:** Although the likelihood is medium (requires internal network access), the impact is catastrophic. This risk highlights a critical architectural principle: role resolution must never cross a network boundary. If a custom external role resolver is used, the entire auth architecture is one internal network compromise away from a full privilege escalation. The mitigation is architectural — keep role resolution in-process.

**Required Mitigations:**
1. Role resolution must always be performed in-process from verified JWT claims — never via a network call
2. If an external role resolver is ever introduced, it must use mutual TLS and message signing
3. Spring Security must extract roles from the cryptographically verified JWT payload only
4. Architecture review: document and enforce the in-process constraint

---

#### RISK-07 — Malicious File Upload — Remote Code Execution (Game Images Handler)

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L2-P-14 |
| **STRIDE Category** | Tampering |
| **Element** | Game Images Handler (Level 2 — Game Management) |
| **Threat** | PHP webshell or executable uploaded with spoofed Content-Type; stored and served to buyers; potential RCE |

**Likelihood Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Skill Level | 5 | Content-Type spoofing requires moderate skill; tools readily available |
| Motive | 7 | RCE provides full server access; significant capability gain |
| Opportunity | 7 | Any authenticated Publisher can upload files |
| Size | 5 | Publisher accounts; medium-sized threat agent population |
| Ease of Discovery | 6 | Changing Content-Type header is trivial; testing with Burp Suite is standard practice |
| Ease of Exploit | 6 | Uploading a webshell is well-documented; triggering it requires knowing the path |
| Awareness | 9 | Unrestricted file upload is OWASP Top 10 A04:2021 |
| Intrusion Detection | 5 | File upload may be logged but executable content may not trigger specific alerts |
| **Likelihood Score** | **6.3** | **→ High Likelihood** |

**Impact Scoring**

| Factor | Score | Justification |
|--------|------:|---------------|
| Loss of Confidentiality | 9 | RCE provides access to all data including DB credentials and secrets |
| Loss of Integrity | 9 | Attacker can modify any file on the server |
| Loss of Availability | 8 | Server can be destroyed or ransomed |
| Loss of Accountability | 8 | Actions performed at OS level are difficult to attribute |
| Financial Damage | 9 | Full server compromise; potential ransomware; DB access |
| Reputation Damage | 9 | RCE represents complete security failure |
| Non-Compliance | 9 | All data on the server exposed; all regulations violated |
| Privacy Violation | 9 | All user data on the server accessible |
| **Impact Score** | **8.8** | **→ High Impact** |

**Risk Level: CRITICAL** (High Likelihood × High Impact)

**Justification:** Remote code execution represents the most severe possible outcome of a security vulnerability. A successful webshell upload gives an attacker complete control of the ArcadeHaven server, including access to all database credentials, environment secrets, user data, and activation keys. The attack surface is broad (any Publisher account) and the technique is well-documented and actively exploited.

**Required Mitigations:**
1. Server-side magic-byte validation using Apache Tika (not Content-Type header) for all uploaded files
2. ClamAV antivirus scan on every uploaded file before storage
3. UUID filename generation on save — original filename discarded; never executed
4. Block executable MIME types (application/x-php, application/x-executable, text/x-script.*) at the handler level
5. Files stored outside the web root; not directly executable by the web server

---

### 3.2 High Risks

---

#### RISK-08 — SQL Injection Across All DB Flows

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1C-F-01, L1C-F-11, L1C-F-19, L1C-F-27 |
| **STRIDE Category** | Tampering / Information Disclosure |
| **Element** | All Read/Write Data Flows to DB |

**Likelihood Score:** 6.4 (High) | **Impact Score:** 8.1 (High) | **Risk Level: CRITICAL**

| Factor | L | I |
|--------|:-:|:-:|
| Skill Level | 5 | — |
| Motive | 7 | — |
| Opportunity | 7 | — |
| Size | 7 | — |
| Ease of Discovery | 8 | — |
| Ease of Exploit | 7 | — |
| Awareness | 9 | — |
| Intrusion Detection | 5 | — |
| Loss of Confidentiality | — | 9 |
| Loss of Integrity | — | 9 |
| Loss of Availability | — | 6 |
| Loss of Accountability | — | 7 |
| Financial Damage | — | 8 |
| Reputation Damage | — | 8 |
| Non-Compliance | — | 9 |
| Privacy Violation | — | 9 |

**Risk Level: CRITICAL** — SQL injection affecting any of the four DB flows could result in full database exfiltration or destruction. JPA parameterised queries must be enforced universally with zero exceptions.

---

#### RISK-09 — IDOR on Order History — Cross-User Financial Data

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1C-F-20, L1A-P-24 |
| **STRIDE Category** | Information Disclosure / Elevation of Privilege |
| **Element** | Order Management / Order Datastore |

**Likelihood Score:** 7.3 (High) | **Impact Score:** 7.4 (High) | **Risk Level: CRITICAL**

Combines easy exploit (same as RISK-04 but targeting financial records), high attacker motivation (activation keys + payment data), and Critical impact (financial PII + GDPR breach).

---

#### RISK-10 — Credential Stuffing Against Keycloak

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1B-P-07 |
| **STRIDE Category** | Spoofing |
| **Element** | Credential Validation Process |

**Likelihood Score:** 7.8 (High) | **Impact Score:** 6.1 (High) | **Risk Level: CRITICAL**

Automated tools and large breach databases make credential stuffing highly likely. Account takeover enables further fraud (L1A-P-19, L1A-P-22). MFA is the primary control.

---

#### RISK-11 — Publisher Self-Approval of Games

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1A-P-18, L2-P-06 |
| **STRIDE Category** | Elevation of Privilege |
| **Element** | Game Management Module / Game Data Handler |

**Likelihood Score:** 6.1 (High) | **Impact Score:** 5.8 (Medium) | **Risk Level: High**

Missing role check on approval endpoint is easy to exploit but impact is contained to content policy bypass rather than data exfiltration. Still requires immediate fix because it breaks the core business workflow.

---

#### RISK-12 — RAWG SSRF via Image URL

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1D-F-11, L2-F-04 |
| **STRIDE Category** | Tampering |
| **Element** | Game Metadata Handler / Game Metadata Request Flow |

**Likelihood Score:** 4.5 (Medium) | **Impact Score:** 8.9 (High) | **Risk Level: High**

Requires a spoofed RAWG response (medium likelihood) but if successful, SSRF to the AWS metadata endpoint yields IAM credentials giving full cloud access. The asymmetry between likelihood and impact makes this a High risk requiring architectural controls (URL allowlist + egress proxy).

---

#### RISK-13 — Age Rating Tampered in Game Details Response

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1D-F-18 |
| **STRIDE Category** | Tampering |
| **Element** | Game Details Request/Response Flow |

**Likelihood Score:** 4.2 (Medium) | **Impact Score:** 7.8 (High) | **Risk Level: High**

Requires MitM on RAWG connection (medium likelihood) but impact involves regulatory violations (PEGI/ESRB compliance) and potential legal liability for selling adult content to minors — a High risk regardless of likelihood.

---

#### RISK-14 — Decompression Bomb Causing OOM Crash

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L2-P-17 |
| **STRIDE Category** | Denial of Service |
| **Element** | Game Images Handler |

**Likelihood Score:** 6.0 (High) | **Impact Score:** 6.5 (High) | **Risk Level: High**

Easy to create and upload a decompression bomb image. Impact is full application crash affecting all users. Fix is simple (validate compressed file size and maximum decompressed dimensions before processing).

---

#### RISK-15 — User DB Backup Exposed Unencrypted (GDPR)

| Attribute | Value |
|-----------|-------|
| **Threat ID** | L1C-F-31, L1C-S-15 |
| **STRIDE Category** | Information Disclosure |
| **Element** | User Information Datastore |

**Likelihood Score:** 4.8 (Medium) | **Impact Score:** 8.9 (High) | **Risk Level: High**

Cloud misconfiguration is a common real-world incident. Impact is a GDPR breach requiring 72-hour notification with potential fines up to 4% of global annual turnover. Infrastructure controls (encrypted backups, S3 Block Public Access) must be in place before go-live.

---

### 3.3 Medium Risks

| # | Threat ID | Description | Likelihood | Impact | Risk Level |
|---|-----------|-------------|:----------:|:------:|:----------:|
| RISK-16 | L1B-P-14 | Weak JWT signing key — HS256 with guessable secret | 5.1 | 8.8 | **High** |
| RISK-17 | L1A-P-14 | Malicious game file upload bypassing MIME check | 6.3 | 7.1 | **High** |
| RISK-18 | L1B-F-06 | Token response cached by proxy — cross-user token leakage | 4.0 | 7.5 | **High** |
| RISK-19 | L1D-F-06 | RAWG quota exhaustion via publisher flood | 6.5 | 4.2 | **Medium** |
| RISK-20 | L1C-F-25 | Synchronous PDF generation in order write transaction | 5.8 | 3.9 | **Medium** |
| RISK-21 | L1B-P-20 | Excessive clock skew accepting expired tokens | 4.1 | 5.5 | **Medium** |
| RISK-22 | L1A-P-35 | User enumeration via login timing attack | 6.8 | 3.2 | **Medium** |
| RISK-23 | L2-P-16 | EXIF metadata in uploaded images exposing developer PII | 5.5 | 4.1 | **Medium** |
| RISK-24 | L1C-F-16 | Concurrent activation writes — key burned without use | 4.5 | 5.0 | **Medium** |
| RISK-25 | L1D-F-14 | RAWG tracking pixels stored — GDPR violation | 4.0 | 5.5 | **Medium** |

---

### 3.4 Low Risks

| # | Threat ID | Description | Likelihood | Impact | Risk Level |
|---|-----------|-------------|:----------:|:------:|:----------:|
| RISK-26 | L1C-F-08 | N+1 query on game list | 5.5 | 2.5 | **Low** |
| RISK-27 | L1B-F-19 | Keycloak role definitions accidentally deleted | 2.0 | 5.5 | **Low** |
| RISK-28 | L2-F-07 | Race condition on concurrent image writes | 3.5 | 3.0 | **Low** |
| RISK-29 | L1D-F-23 | Game Details Response cached with insufficient cache key | 3.0 | 2.5 | **Low** |
| RISK-30 | L1C-F-35 | PII history table unbounded growth | 3.5 | 2.0 | **Low** |

---

## 4. Risk Priority Matrix

The following matrix maps all assessed risks by likelihood and impact:

```
         LOW IMPACT    MEDIUM IMPACT    HIGH IMPACT
         (1-3)         (4-6)            (7-9)
         ┌─────────────┬────────────────┬────────────────────┐
HIGH     │             │  RISK-19       │  RISK-01 RISK-02   │
LIKELIH. │             │  RISK-22       │  RISK-03 RISK-04   │
(7-9)    │             │                │  RISK-05 RISK-07   │
         │             │                │  RISK-08 RISK-09   │
         │             │                │  RISK-10 RISK-17   │
         ├─────────────┼────────────────┼────────────────────┤
MEDIUM   │  RISK-26    │  RISK-20       │  RISK-06 RISK-11   │
LIKELIH. │  RISK-27    │  RISK-21       │  RISK-12 RISK-13   │
(4-6)    │  RISK-28    │  RISK-23       │  RISK-14 RISK-15   │
         │  RISK-29    │  RISK-24       │  RISK-16 RISK-18   │
         │  RISK-30    │  RISK-25       │                    │
         ├─────────────┼────────────────┼────────────────────┤
LOW      │             │                │                    │
LIKELIH. │             │                │                    │
(1-3)    │             │                │                    │
         └─────────────┴────────────────┴────────────────────┘
```

---

## 5. Prioritised Treatment Plan

### Phase 1 — Before Deployment (Critical)

These risks must be resolved before ArcadeHaven goes live. Deployment with any of these open represents an unacceptable business risk.

| Priority | Risk ID | Threat | Owner | Effort |
|:--------:|---------|--------|-------|--------|
| 1 | RISK-05 | JWT alg:none attack — complete auth bypass | Security / Backend | Low (config change) |
| 2 | RISK-08 | SQL injection across all DB flows | Backend | Medium (code audit) |
| 3 | RISK-04 | IDOR — full user PII to non-owner callers | Backend | Low (add ownership check) |
| 4 | RISK-09 | IDOR — cross-user order financial data | Backend | Low (add buyer_id scope) |
| 5 | RISK-01 | Activation keys stored in plaintext | Backend | Medium (encryption implementation) |
| 6 | RISK-03 | Stored XSS via RAWG metadata | Backend | Low (add sanitiser dependency) |
| 7 | RISK-07 | Malicious file upload — potential RCE | Backend | Medium (magic-byte + ClamAV) |
| 8 | RISK-02 | Order record retroactive modification | Backend / DBA | Medium (DB trigger + constraints) |
| 9 | RISK-10 | Credential stuffing against Keycloak | DevOps / Security | Low (Keycloak config) |
| 10 | RISK-15 | User DB backup exposed — GDPR breach | DevOps / Infra | Low (S3 policy + encryption) |

### Phase 2 — Current Sprint (High)

These risks significantly increase the platform's security posture and should be addressed in the same sprint as the critical fixes.

| Priority | Risk ID | Threat | Owner | Effort |
|:--------:|---------|--------|-------|--------|
| 11 | RISK-06 | Role Response tampered — privilege escalation | Architecture | Low (enforce in-process) |
| 12 | RISK-16 | Weak JWT signing key — HS256 guessable | DevOps | Low (Keycloak config) |
| 13 | RISK-11 | Publisher self-approval of games | Backend | Low (add @PreAuthorize) |
| 14 | RISK-14 | Decompression bomb — OOM crash | Backend | Low (add size validation) |
| 15 | RISK-12 | RAWG SSRF via image URL | Backend | Medium (URL allowlist + egress proxy) |
| 16 | RISK-13 | Age rating tampered — minor accessing adult content | Backend | Medium (TLS pinning + enum validation) |
| 17 | RISK-17 | Malicious game file upload (MIME spoofing) | Backend | Medium (already in Phase 1 — verify) |
| 18 | RISK-18 | Token response cached by proxy | DevOps | Low (Cache-Control header) |

### Phase 3 — Next Release (Medium)

| Priority | Risk ID | Threat | Effort |
|:--------:|---------|--------|--------|
| 19 | RISK-19 | RAWG quota exhaustion via publisher flood | Low |
| 20 | RISK-20 | Synchronous PDF generation stalling DB transaction | Medium |
| 21 | RISK-21 | Excessive JWT clock skew | Low |
| 22 | RISK-22 | User enumeration via timing attack | Low |
| 23 | RISK-23 | EXIF metadata in uploads | Low |
| 24 | RISK-24 | Concurrent activation key writes | Low |
| 25 | RISK-25 | RAWG tracking pixels — GDPR | Low |

### Phase 4 — Backlog (Low)

| Priority | Risk ID | Threat |
|:--------:|---------|--------|
| 26 | RISK-26 | N+1 query on game list |
| 27 | RISK-27 | Keycloak role definitions accidentally deleted |
| 28 | RISK-28 | Race condition on concurrent image writes |
| 29 | RISK-29 | Game Details Response cache key insufficient |
| 30 | RISK-30 | PII history table unbounded growth |

---

## 6. Risk Assessment Decisions — Justification Summary

### Why OWASP Risk Rating was chosen

The OWASP Risk Rating Methodology was selected over alternatives (DREAD, CVSS) for the following reasons:

| Criterion | DREAD | CVSS | OWASP Risk Rating |
|-----------|:-----:|:----:|:-----------------:|
| Business impact factors | ✗ | Partial | ✓ |
| Considers threat agent | ✗ | ✗ | ✓ |
| Considers detection capability | ✗ | ✗ | ✓ |
| Appropriate for web applications | Partial | ✓ | ✓ |
| Justifies prioritisation decisions | ✗ | Partial | ✓ |
| GDPR / regulatory impact scoring | ✗ | ✗ | ✓ |

DREAD was considered but rejected because it does not include business impact factors (financial damage, non-compliance, privacy violation) which are critical for a platform handling payments and personal data under GDPR. CVSS scores technical severity well but does not model the business context of an e-commerce platform.

### Key Prioritisation Decisions

**RISK-05 (alg:none) is ranked #1** despite not being the highest-impact threat in isolation, because it is the only vulnerability that can bypass the entire authentication and authorisation system in a single step. All other security controls are rendered void if this is exploited.

**RISK-06 (Role Response tampering) is ranked lower than its impact score suggests** because the likelihood requires internal network access — a precondition that places it behind threats that require only a browser and a valid JWT account. However, it is still treated as Phase 2 because the architectural fix (enforcing in-process role resolution) is low-effort and eliminates the risk entirely.

**RISK-15 (unencrypted backup) is in Phase 1** despite being an infrastructure control rather than a code change, because a single misconfiguration incident would constitute an automatic GDPR data breach with mandatory notification obligations — a regulatory risk that cannot be deferred.

**Risks L0-P-01 through L0-P-06** (Level 0 system-context threats) are not separately scored in the risk register because they are fully covered by more specific threats identified at lower DFD levels. The Level 0 analysis provides context for understanding the system boundary, not additional distinct risks.

---

## 7. Residual Risk Acceptance

After implementing all Phase 1 and Phase 2 mitigations, the following residual risks are accepted:

| Risk | Residual Level | Acceptance Justification |
|------|:--------------:|--------------------------|
| RISK-06 (Role Response tampering) | Low | In-process role resolution eliminates the network attack surface; only applicable if architecture changes |
| RISK-12 (RAWG SSRF) | Low | URL allowlist + egress proxy reduces to theoretical with proper implementation |
| RISK-27 (Keycloak config accidentally deleted) | Low | Version-controlled Keycloak config + deployment smoke tests reduce likelihood to near-zero |
| RISK-19 (RAWG quota exhaustion) | Medium | Per-publisher rate limiting reduces but does not eliminate; accepted pending RAWG paid tier upgrade |

---

*End of Risk Assessment Document*
