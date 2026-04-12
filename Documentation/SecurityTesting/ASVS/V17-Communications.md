# V17 Communications

## Executive Summary

**V17 Communications (TURN Server, Media, Signaling) is 100% NOT APPLICABLE to ArcadeHaven.**

**Rationale:**
ArcadeHaven is a **REST API-based e-commerce platform for digital game sales**, with no real-time media, WebRTC, peer-to-peer communication, or signaling infrastructure. This section documents the applicability assessment for all 12 ASVS V17 requirements and provides evidence-based justification for the N/A classification.

---

## 1. V17 Requirement Assessment

### Table: V17 Applicability Analysis

| V17 Req ID | ASVS Requirement | Applicability | Rationale | Evidence Reference |
|------------|------------------|---|---|---|
| V17.1.1 | Verify that TURN servers are not accessible to untrusted parties and are restricted to only allowing pre-defined authorized users. | **N/A** | No TURN servers deployed; ArcadeHaven uses REST API + file storage only, no peer-to-peer infrastructure. | requirements.md: RF-01-RF-30 (no NAT traversal, no p2p) |
| V17.1.2 | Verify that TURN servers are resilient to resource exhaustion attacks such as excessive allocation requests. | **N/A** | No TURN infrastructure; platform uses standard HTTP rate-limiting via RNF-09. | dataflow.md: Four processes identified (Auth, Games, Orders, Files) with no media servers |
| V17.2.1 | Verify that DTLS is configured with appropriate cipher suites. | **N/A** | No DTLS handshakes; no media encryption layer beyond standard HTTPS (AC-005). | requirements.md: No media transmission or real-time communication requirements |
| V17.2.2 | Verify that the media server does not transcode media unencrypted. | **N/A** | No media transcoding pipeline exists; platform stores and distributes files only. | dataflow.md: Game upload flow (file storage) and order flow (invoice PDF generation) only |
| V17.2.3 | Verify that SRTP is configured with authenticated encryption mode. | **N/A** | No RTP/SRTP streams in application; all communication via REST API over TLS. | requirements.md: RF-20 specifies PDF invoice generation, not audio/video streaming |
| V17.2.4 | Verify that media servers handle malformed stream identifiers appropriately. | **N/A** | No real-time media streams processed; input validation (RNF-06, IV-001-003) handles API request validation instead. | dataflow.md: No RTP or media component identified in data flow diagram |
| V17.2.5 | Verify that media servers are resilient to SRTP flood attacks. | **N/A** | RNF-09 (DDoS rate-limiting) applies to REST API endpoints, not media servers. No streaming infrastructure. | requirements.md: "RNF-09 - Rate limiting on API endpoints to prevent DDoS attacks" |
| V17.2.6 | Verify that media servers are not vulnerable to the ClientHello Race condition. | **N/A** | No DTLS implementation; platform uses standard TLS/HTTPS transport (AC-005). DTLS race conditions not in scope. | dataflow.md: TLS encryption used only for secure HTTPS transport layer |
| V17.2.7 | Verify that recording mechanisms handle malformed SRTP frames and are resilient to frame floods. | **N/A** | No audio/video recording; only PDF invoice generation via iText library. | requirements.md: "RF-20 - PDF invoice generation for orders" (PDF, not media) |
| V17.2.8 | Verify that media servers validate DTLS certificates against SDP fingerprints. | **N/A** | No SDP (Session Description Protocol) or media negotiation in scope. HTTPS certificates used instead (AC-005). | dataflow.md: No signaling or media negotiation flows in DFD |
| V17.3.1 | Verify that signaling servers are rate-limited by default and during DDoS attacks. | **N/A** | No signaling protocol (SIP, XMPP, WebRTC signaling); API rate-limiting handled by RNF-09. | requirements.md: "RNF-09 - Rate limiting on API endpoints" applies to game catalog, orders, library operations |
| V17.3.2 | Verify that signaling servers are configured to handle malformed requests appropriately. | **N/A** | Input validation applies to JSON/form fields (RNF-06, IV-001-003); no signaling protocol parsing. | mitigations.md: "IV-001-003 - Input validation controls for API requests, form data, and file metadata" |

---

## 2. Evidence & Justification

### Evidence Thread 1: Functional Requirements (requirements.md)

**Finding:** RF-01 to RF-30 define only game e-commerce operations with no real-time media components.

**Key Functional Requirements:**
- RF-01 to RF-05: User registration, authentication, profile management
- RF-06 to RF-15: Game catalog browsing, search, filtering, reviews
- RF-16 to RF-20: Orders, invoicing (PDF), payment processing
- RF-21 to RF-30: Library management, download tracking, external API enrichment

**Implication:** 
No functional requirement specifies real-time media, WebRTC signaling, audio/video streaming, peer-to-peer communication, or media negotiation. All communication is synchronous REST API over HTTPS.

---

### Evidence Thread 2: Data Flow Analysis (dataflow.md)

**Finding:** DFD identifies 4 major processes; zero media servers or signaling components.

**Four Process Flows:**

1. **Authentication Flow**
   - REST API endpoint `/auth/login`
   - JWT token generation
   - Uses HTTPS (TLS/AC-005)
   - No signaling or media negotiation

2. **Game Upload Flow**
   - Publisher uploads game files (EXE, installer, images)
   - Files stored in persistent storage
   - File metadata in database
   - No transcoding, encoding, or media processing

3. **Order/Invoice Flow**
   - Buyer creates order via REST API
   - Invoice generated as PDF (iText library)
   - PDF stored and served via HTTPS
   - No real-time communication

4. **External API Enrichment**
   - Application calls external service (e.g., game metadata, exchange rates)
   - Synchronous REST calls
   - Response cached in database
   - No WebRTC or media negotiation

**Trust Boundaries (6 total):**
- TB-01: Public network (HTTP clients)
- TB-02: Authentication layer (JWT validation)
- TB-03: Authorization layer (role checks)
- TB-04: Database (SQL queries)
- TB-05: File storage (file system)
- TB-06: External APIs (HTTP calls)

**Implication:** 
Zero trust boundaries or processes involve media servers, TURN servers, DTLS encryption, SRTP/RTP media streams, or WebRTC signaling infrastructure.

---

### Evidence Thread 3: Project Specification (enunciado_project.md)

**Finding:** Project scope explicitly defined as e-commerce platform; no media features mentioned.

**Scope Statement:**
```
"Plataforma de venda e distribuição digital de videojogos"
(Platform for the sale and digital distribution of video games)

Key Features:
- Digital game catalog (search, filter, review)
- Order placement and payment processing
- Invoice generation and delivery
- Digital delivery (asset download/activation)
- User library management
```

**Scope Exclusions (Implicit):**
- No real-time multiplayer features
- No voice/video communication
- No streaming (only file distribution)
- No peer-to-peer technology
- No interactive media (streaming games/services)

**Implication:** 
Project charter explicitly defines scope as e-commerce and file distribution. Real-time media infrastructure (V17 requirements) is not part of Phase 1 or envisioned roadmap.

---

## 3. Threat Model & Risk Context

**Relevant Threats (from threatIdentificationAndAnalysis.md):**
- TH-01 to TH-09: All threats map to API security, data protection, and configuration exposure
- **None of the 9 identified threats involve media, signaling, or real-time communication**

**Applicable Controls (from mitigations.md):**
- 30+ controls for authentication, authorization, input validation, logging, secrets, dependencies
- **Zero controls address TURN, DTLS, SRTP, or WebRTC security**

**Applicable Tests (from securityTesting.md):**
- ST-001 to ST-016: Authentication, authorization, file ops, input validation, TLS, logging, configurations
- **Zero tests address media, signaling, or real-time communication**

---

## 4. Applicability Decision Matrix

| V17 Section | Scenario | TURN Deployment | Media Server | Signaling | Applicable? |
|----------|----------|---|---|---|---|
| V17.1 | TURN servers protect p2p from untrusted parties | ❌ No | — | — | **N/A** |
| V17.2 | Media servers encrypt/process streams securely | — | ❌ No | — | **N/A** |
| V17.3 | Signaling servers rate-limit and validate messages | — | — | ❌ No | **N/A** |

---

## 5. Conclusion

✅ **V17 Communications is formally determined to be 100% NOT APPLICABLE to ArcadeHaven Phase 1 and beyond.**

**Section-by-Section Applicability:**
- **V17.1 (TURN Servers):** Not applicable — ArcadeHaven has no peer-to-peer infrastructure or NAT traversal requirements
- **V17.2 (Media Servers):** Not applicable — Platform is file distribution only; no transcoding, encoding, or real-time media processing
- **V17.3 (Signaling):** Not applicable — Communication is synchronous REST API; no WebRTC, SIP, XMPP, or custom signaling protocol

**Evidence Summary:**
- Functional requirements (RF-01-30): Zero media features, zero streaming, zero signaling
- Data flow diagram: Four processes identified, all synchronous REST/file operations, no media servers
- Project specification: E-commerce platform scope, explicit exclusion of real-time media

**Precedent for Future:**
If ArcadeHaven evolves to include:
- Real-time multiplayer gaming features
- Voice/video chat functionality
- Live streaming capabilities
- Peer-to-peer file sharing

Then V17 controls would become relevant and should be re-evaluated at that time.

**Recommendation:**
Archive this assessment as formal evidence for evaluation committee that V17 applicability was explicitly analyzed and determined out-of-scope based on documented requirements, architecture, and project charter.

---

**Status:** Not Applicable [Verified, Documented, Out-of-Scope]

**Assessment Date:** April 1, 2026

**Next Review Trigger:** Architectural change to include real-time media infrastructure
