# ArcadeHaven Documentation — Phase 1

This document serves as the main index for Phase 1 of the **ArcadeHaven** project — a secure digital game store platform built with Java Spring Boot. Here you will find references to all required artefacts, organized by topic, as well as the evaluation criteria.

## Document Structure

```
Documentation/
│
├── Overview/
│   └── README.md
│
├── Requirements/
│   └── requirements.md
│
├── Architecture/
│   ├── Diagrams/
│   │   └── DomainModel/
│   ├── Dataflow/
│   │   ├── Level-0/
│   │   ├── Level-1/
│   │   └── Level-2/
│   └── dataflow.md
│
├── ThreatModeling/
│   ├── AbuseCases/
│   ├── RiskAssessment/
│   └── ThreatIdentificationAndAnalysis/
│
├── Mitigations/
│   └── Mitigations.md
│
├── SecurityTesting/
│   ├── ASVS/
|   ├── MasterObjectivePlan.md
|   ├── SecurityTesting.md
│   ├── ThreatModelReviewWorkflow.md
│   └── TraceabilityMatrix-V2.md
│
└── SSDLC/
    ├── SSDLC.md
    └── DREAD.md
```

- [Architecture](../Architecture/architecture.md): System overview, architecture, and domain model of the ArcadeHaven platform, covering the four core DDD aggregates: **User**, **Game**, **Order**, and **Library**.
- [Architecture Views](../Architecture/Diagrams/Views/README.md): Logical and physical architecture views, including Docker deployment view for API and database.
- [Logical View Diagram](../Architecture/Diagrams/Views/logical-view.puml): Minimal logical responsibilities view used as stable architecture contract.
- [Physical Deployment Diagram (Docker)](../Architecture/Diagrams/Views/physical-deployment-docker.puml): Physical deployment view with API and database in separate Docker containers.
- [Dataflow](../Architecture/Dataflow/dataflow.md): Documentation of data flows across the platform, including DFDs (levels 0, 1, and higher if needed), system components, trust boundaries, and external entities such as the RAWG.io API.
- [Threat Identification and Analysis](../ThreatModeling/ThreatIdentificationAndAnalysis/threatIdentificationAndAnalysis.md): Identification and analysis of threats specific to ArcadeHaven, including STRIDE application per DFD element, attack vectors, threat agents, and abuse cases (e.g. unauthorized access to a user's Library, activation key theft).
- [Attack Trees](../ThreatModeling/AttackTrees/AttackTrees.md): Goal-oriented decomposition of high-priority threats (TH-01, TH-04, TH-08) with OR/AND attack paths mapped to controls, tests, and requirements.
- [Risk Assessment](../ThreatModeling/RiskAssessment/riskAssessment.md): Complete risk assessment using DREAD methodology — initial scoring for TH-01 to TH-09, risk treatment plan, residual risk analysis, and release acceptance rules.
- [DREAD Analysis](../SSDLC/DREAD.md): STRIDE category-level DREAD summary plus individual per-threat DREAD scoring for all 76 threats identified by OWASP Threat Dragon, each anchored to CWE, OWASP Top 10 2021, and CVSS 3.1 base score ranges with per-dimension justification.
- [Mitigations](../Mitigations/Mitigations.md): Proposed mitigations for identified threats, focusing on high-priority risks such as broken access control, insecure file handling, and credential exposure.
- [Requirements](../Requirements/requirements.md): Justified security requirements for ArcadeHaven, covering authentication and authorization (Admin, Publisher, Buyer roles), data security, secure communication, input validation, third-party components (RAWG.io), logging, and monitoring.
- [Security Testing](../SecurityTesting/SecurityTesting.md): Security testing methodology for ArcadeHaven, including abuse cases, threat model review process, and ASVS assessment focused on the platform architecture.

[//]: # (- [Security Traceability Matrix v2]&#40;../SecurityTesting/TraceabilityMatrix-V2.md&#41;: Requirement-to-threat-to-test mapping with owner and sprint planning.)



## Abuse-Case Diagrams

### Diagrams

- [Authorization Abuse Case](AbuseCases/authorization-abuse-case.png)
- [Download Invoice Abuse Case](AbuseCases/download-invoice-abuse-case.png)
- [Game Management Abuse Case](AbuseCases/game-management-abuse-case.png)
- [Game Submission Abuse Case](AbuseCases/game-submission-abuse-case.png)
- [Role Management Abuse Case](AbuseCases/role-management-abuse-case.png)

### Source Files (PlantUML)

- [Authorization Abuse Case](AbuseCases/authorization-abuse-case.puml)
- [Download Invoice Abuse Case](AbuseCases/download-invoice-abuse-case.puml)
- [Game Management Abuse Case](AbuseCases/game-management-abuse-case.puml)
- [Game Submission Abuse Case](AbuseCases/game-submission-abuse-case.puml)
- [Role Management Abuse Case](AbuseCases/role-management-abuse-case.puml)

### Project File

- [Visual Paradigm Project](AbuseCases/DESOFS%20-%20Abuse%20Cases.vpp)


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