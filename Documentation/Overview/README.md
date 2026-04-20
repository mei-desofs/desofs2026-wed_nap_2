# ArcadeHaven — Main Documentation

> Secure digital game store platform built with **Java Spring Boot**, following Domain-Driven Design (DDD) principles and a Secure Software Development Lifecycle (SSDLC).

---

## Platform overview

ArcadeHaven is a digital game store with three user roles — **Admin**, **Publisher**, and **Buyer** — built around four core DDD aggregates: **User**, **Game**, **Order**, and **Library**. Security is embedded at every stage of development, from architecture through threat modeling, risk assessment, and ASVS-aligned testing.

---

## Repository Documentation structure

```
../
├── Overview/
│   └── README.md                          
├── Requirements/
│   └── requirements.md
├── Architecture/
│   ├── architecture.md
│   ├── Diagrams/
│   │   ├── DomainModel/                   ← SVG + PlantUML domain model
│   │   └── Views/                         ← Logical, physical, development views (L1–L3)
│   └── Dataflow/
│       ├── dataflow.md
│       ├── Level-0/                       ← Context DFD
│       ├── Level-1/                       ← System, Auth API, Database, RAWG API
│       └── Level-2/                       ← Game management detail
├── ThreatModeling/
    ├── ThreatModel.md
│   ├── AbuseCases/                        ← Abuse case diagrams (PNG + PlantUML)
│   ├── AttackTrees/                       ← Attack trees
│   ├── RiskAssessment/
│   │   └── riskAssessment.md
│   └── ThreatIdentificationAndAnalysis/
│       └── threatIdentificationAndAnalysis.md
├── Mitigations/
│   └── mitigations.md
├── SSDLC/
│   ├── SSDLC.md
│   └── DREAD.md
└── SecurityTesting/
    ├── securityTesting.md
    ├── TraceabilityMatrix.md
    ├── ThreatModelReviewWorkflow.md
    └── ASVS/                              ← V1–V17 individual assessments
```

---

## Documentation index

### Architecture

| Artefact | Description |
|---|---|
| [Architecture](../Architecture/architecture.md) | System overview, component responsibilities, and DDD aggregate design |
| [Architecture views](../Architecture/Diagrams/Views/Views.md) | Logical, physical (Docker), and development views at levels 1–3 |
| [Domain model](../Architecture/Diagrams/DomainModel/) | SVG domain model and PlantUML source |
| [Dataflow](../Architecture/Dataflow/dataflow.md) | DFDs at levels 0, 1, and 2; trust boundaries; RAWG.io external entity |

### Threat modeling

| Artefact | Description                                                                                                                                                                                                                                                                  |
|---|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Threat Model](../ThreatModeling/ThreatModel.md) | High-level threat modeling document describing the system architecture, actors, assets, trust boundaries, entry/exit points, and external dependencies. Establishes the foundation for security analysis using STRIDE and defines the overall attack surface of ArcadeHaven. |
| [Threat identification & analysis](../ThreatModeling/ThreatIdentificationAndAnalysis/threatIdentificationAndAnalysis.md) | STRIDE per DFD element, attack vectors, threat agents, and abuse cases                                                                                                                                                                                                       |
| [Attack trees](../ThreatModeling/AttackTrees/AttackTrees.md) | OR/AND decomposition for Account takeover, Input injection, Order tampering|
| [Risk assessment](../ThreatModeling/RiskAssessment/riskAssessment.md) | Treatment plan, residual risk, release acceptance rules                                                                                                                                                                                                                      |
| [DREAD analysis](../SSDLC/DREAD.md) | Per-threat DREAD for all threats; anchored to CWE, OWASP Top 10 2021, CVSS 3.1                                                                                                                                                                                               |

### Security & requirements

| Artefact | Description |
|---|---|
| [Requirements](../Requirements/requirements.md) | Security requirements for authentication, authorization, data protection, input validation, logging, and third-party components |
| [Mitigations](../Mitigations/mitigations.md) | Controls for broken access control, insecure file handling, and credential exposure |
| [SSDLC](../SSDLC/SSDLC.md) | Secure development lifecycle process overview |

### Security testing

| Artefact | Description |
|---|---|
| [Security testing methodology](../SecurityTesting/securityTesting.md) | Test approach, abuse case integration, and ASVS assessment scope |
| [Traceability matrix](../SecurityTesting/TraceabilityMatrix.md) | Requirement → threat → test mapping |
| [Threat model review workflow](../SecurityTesting/ThreatModelReviewWorkflow.md) | Review process and sign-off criteria |
| [ASVS 5.0 assessments](../SecurityTesting/ASVS/) | Individual assessments for V1–V17 (encoding, auth, session, API, crypto, logging, and more) |

---

## Abuse case diagrams

| Diagram | PNG | PlantUML source |
|---|---|---|
| Authorization | [PNG](../ThreatModeling/AbuseCases/authorization-abuse-case.png) | [PUML](../ThreatModeling/AbuseCases/authorization-abuse-case.puml) |
| Download invoice | [PNG](../ThreatModeling/AbuseCases/download-invoice-abuse-case.png) | [PUML](../ThreatModeling/AbuseCases/download-invoice-abuse-case.puml) |
| Game management | [PNG](../ThreatModeling/AbuseCases/game-management-abuse-case.png) | [PUML](../ThreatModeling/AbuseCases/game-management-abuse-case.puml) |
| Game submission | [PNG](../ThreatModeling/AbuseCases/game-submission-abuse-case.png) | [PUML](../ThreatModeling/AbuseCases/game-submission-abuse-case.puml) |
| Role management | [PNG](../ThreatModeling/AbuseCases/role-management-abuse-case.png) | [PUML](../ThreatModeling/AbuseCases/role-management-abuse-case.puml) |

[Visual Paradigm project file](../ThreatModeling/AbuseCases/DESOFS%20-%20Abuse%20Cases.vpp)

---

# Phase 1

## Deliverables

| File                                                              | Description |
|-------------------------------------------------------------------|---|
| [ASVS_5.0_Tracker.xlsx](../../Deliverables/ASVS_5.0_Tracker.xlsx) | Full ASVS 5.0 requirement tracker with test status |
| [Part_1.md](../../Deliverables/Part_1.md)                         | Phase 1 consolidated report |

---

## Evaluation criteria

| Criteria | Weight | What counts as excellent                                                                                                      |
|---|---|-------------------------------------------------------------------------------------------------------------------------------|
| Requirements | 20% | Justified security requirements covering all relevant topics, grounded in best practices, identified threats, and regulations |
| Threat identification & analysis | 20% | STRIDE applied per DFD element; detailed attack vectors, threat agents, and abuse cases                                       |
| Dataflow | 15% | DFDs with well-identified components, flows, trust boundaries, and external entities                                          |
| Security testing | 10% | Defined methodology, ASVS assessment, traceability between requirements and tests                                             |
| Mitigations | 10% | Specific, clear, and feasible controls focused on high-priority risks                                                         |
| Risk assessment | 10% | Employ a well-defined risk assessment methodology toprioritise risks and justifies decisions                                  |
| Analysis | 10% | Complete system overview with all major components described                                                                  |
| Organization & language | 5% | Easy navigation, all artefacts linked, no major language errors                                                               |
