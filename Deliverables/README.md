# ArcadeHaven Documentation — Phase 1

This document serves as the main index for Phase 1 of the **ArcadeHaven** project — a secure digital game store platform built with Java Spring Boot. Here you will find references to all required artefacts, organized by topic, as well as the evaluation criteria.

## Document Structure

- [Analysis](Analysis/analysis.md): System overview, architecture, and domain model of the ArcadeHaven platform, covering the four core DDD aggregates: **User**, **Game**, **Order**, and **Library**.
- [Dataflow](Dataflow/dataflow.md): Documentation of data flows across the platform, including DFDs (levels 0, 1, and higher if needed), system components, trust boundaries, and external entities such as the RAWG.io API.
- [Threat Identification and Analysis](ThreatIdentificationAndAnalysis/threatIdentificationAndAnalysis.md): Identification and analysis of threats specific to ArcadeHaven, including STRIDE application per DFD element, attack vectors, threat agents, and abuse cases (e.g. unauthorized access to a user's Library, activation key theft).
- [Risk Assessment](RiskAssessment/riskAssessment.md): Risk assessment methodology and prioritization of identified threats, considering the sensitivity of user data, payment flows, and digital license management.
- [Mitigations](Mitigations/mitigations.md): Proposed mitigations for identified threats, focusing on high-priority risks such as broken access control, insecure file handling, and credential exposure.
- [Requirements](Requirements/requirements.md): Justified security requirements for ArcadeHaven, covering authentication and authorization (Admin, Publisher, Buyer roles), data security, secure communication, input validation, third-party components (RAWG.io), logging, and monitoring.
- [Security Testing](SecurityTesting/securityTesting.md): Security testing methodology for ArcadeHaven, including abuse cases, threat model review process, and ASVS assessment focused on the platform architecture.

## Evaluation Criteria

| Criteria | Weight | Excellent (100%) |
|---|---|---|
| Organization and Language | 5% | Well-organized document and repository, easy navigation, all components linked to this main document, no major language errors. |
| Analysis | 10% | Complete and well-documented system overview, architecture, and domain model; all major components described. |
| Dataflow | 15% | Data flows documented in detail; components, flows, trust boundaries, and external entities well identified; DFDs included. |
| Threat Identification and Analysis | 20% | Identification of relevant threats, proper STRIDE application per DFD element, detailed attack vectors and threat agents with abuse cases. |
| Risk Assessment | 10% | Well-defined and justified risk assessment methodology for prioritization. |
| Mitigations | 10% | Specific, clear, and feasible mitigations for identified threats, focusing on high-priority ones. |
| Requirements | 20% | Justified security requirements, covering all relevant topics and based on best practices, identified threats, and regulations. |
| Security Testing | 10% | Defined testing methodology, reference to abuse cases, review process, ASVS assessment, and traceability between requirements and tests. |

---

Each section above is documented in its respective file and directory. Use this README as the starting point to navigate all Phase 1 documentation for the ArcadeHaven project.