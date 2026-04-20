# P1 – Analysis and Design

This document presents the work developed during the **first phase**, including the respective contributors.

This phase corresponds to the first two stages of the **Secure Software Development Life Cycle (SSDLC)**:

- **Analysis**
- **Design**

The **Analysis** stage focuses on identifying the need for security measures within the system, gathering functional and non-functional requirements, and performing risk assessment.

The **Design** stage is responsible for defining the system architecture and planning appropriate security mechanisms to mitigate the risks identified during the analysis phase.

Together, these stages ensure that security is considered from the early stages of development, enabling a more robust and secure system.

---

## Folder Guide

- **Deliverables**: Final submission artifacts.
- **Documentation**: Main Phase 1 knowledge base.
- **Overview**: Entry point for the full documentation.
- **Architecture**: System architecture, domain model, and data flow artifacts.
- **Requirements**: Functional and non-functional requirements baseline.
- **Mitigations**: Security controls mapped to threats and risks.
- **SSDLC**: Development lifecycle and risk methodology (including DREAD).
- **ThreatModeling**: Threat identification, risk analysis, abuse cases, and attack trees.
- **SecurityTesting**: Security validation strategy, ASVS mapping, and traceability.

---

## Architecture Views

> The following diagrams represent different architectural perspectives of the system.

### Level 1 Views
- [Logical View](../Documentation/Architecture/Diagrams/Views/Level1/LogicView.png)
- [Development View](../Documentation/Architecture/Diagrams/Views/Level1/DevelopmentView.png)
- [Physical View](../Documentation/Architecture/Diagrams/Views/Level1/PhysicalView.png)

### Level 2 Views
- [Logical View](../Documentation/Architecture/Diagrams/Views/Level2/LogicView.png)
- [Development View](../Documentation/Architecture/Diagrams/Views/Level2/DevelopmentView.png)
- [Physical View](../Documentation/Architecture/Diagrams/Views/Level2/Physical.png)

### Level 3 Views
- [Logical View](../Documentation/Architecture/Diagrams/Views/Level3/LogicView.png)

### Other Diagrams
- [Use Case Diagram](../Documentation/Architecture/Diagrams/Views/UseCase/UseCase.png)
- [Domain Model (SVG)](../Documentation/Architecture/Diagrams/DomainModel/ArcadeHaven_Domain_Model.svg)
- [Domain Model (PlantUML)](../Documentation/Architecture/Diagrams/DomainModel/domain_model.puml)

---

## Project Structure

- [Root](../)
  - [README.md](../README.md)

### Deliverables
- [Part_1.md](./Part_1.md)
- [ASVS_5.0_Tracker.xlsx](./ASVS_5.0_Tracker.xlsx)

---

### Documentation

#### Overview
- [README.md](../Documentation/Overview/README.md)

#### Architecture
- [architecture.md](../Documentation/Architecture/architecture.md)

##### Dataflow
- [dataflow.md](../Documentation/Architecture/Dataflow/dataflow.md)
- [DFD JSON](../Documentation/Architecture/Dataflow/arcadehaven-dfd.json)
- [DFD PDF](../Documentation/Architecture/Dataflow/arcadehaven-dfd.pdf)

###### DFD Levels
- [Level 0](../Documentation/Architecture/Dataflow/Level-0/DFD_Level0_Context.png)
- [Level 1 - ArcadeHaven System](../Documentation/Architecture/Dataflow/Level-1/DFD_Level1_ArcadeHavenSystem.png)
- [Level 1 - Auth API](../Documentation/Architecture/Dataflow/Level-1/DFD_Level1_AuthAPI.png)
- [Level 1 - Database](../Documentation/Architecture/Dataflow/Level-1/DFD_Level1_Database.png)
- [Level 1 - RAWG API](../Documentation/Architecture/Dataflow/Level-1/DFD_Level1_RAWGAPI.png)
- [Level 2 - Game Management](../Documentation/Architecture/Dataflow/Level-2/DFD_Level2_GameManagement.png)

---

#### Requirements
- [requirements.md](../Documentation/Requirements/requirements.md)

---

#### Mitigations
- [mitigations.md](../Documentation/Mitigations/mitigations.md)

---

#### SSDLC
- [SSDLC.md](../Documentation/SSDLC/SSDLC.md)
- [DREAD.md](../Documentation/SSDLC/DREAD.md)

---

#### Threat Modeling

- [Threat Model Overview](../Documentation/ThreatModeling/ThreatModel.md)

##### Threat Identification & Analysis
- [threatIdentificationAndAnalysis.md](../Documentation/ThreatModeling/ThreatIdentificationAndAnalysis/threatIdentificationAndAnalysis.md)

##### Risk Assessment
- [riskAssessment.md](../Documentation/ThreatModeling/RiskAssessment/riskAssessment.md)

##### Abuse Cases
- [AbuseCases.md](../Documentation/ThreatModeling/AbuseCases/AbuseCases.md)

##### Attack Trees
- [AttackTrees.md](../Documentation/ThreatModeling/AttackTrees/AttackTrees.md)
- [Account Takeover](../Documentation/ThreatModeling/AttackTrees/Account-Takeover-Attack-Tree.puml)
- [Input Injection](../Documentation/ThreatModeling/AttackTrees/Input-Injection-Attack-Tree.puml)
- [Order Tampering](../Documentation/ThreatModeling/AttackTrees/Order-Tampering-Attack-Tree.puml)

---

#### Security Testing

- [securityTesting.md](../Documentation/SecurityTesting/securityTesting.md)
- [Threat Model Review Workflow](../Documentation/SecurityTesting/ThreatModelReviewWorkflow.md)
- [Traceability Matrix](../Documentation/SecurityTesting/TraceabilityMatrix.md)

##### ASVS Controls

- [V1 – Encoding & Sanitization](../Documentation/SecurityTesting/ASVS/V1_EncodingSanitization/encodingSanatization.md)
- [V2 – Validation & Business Logic](../Documentation/SecurityTesting/ASVS/V2_ValidationBusinessLogic/validationBusinessLogic.md)
- [V3 – Web Frontend Security](../Documentation/SecurityTesting/ASVS/V3_WebFrontendSecurity/webFrontendSecurity.md)
- [V4 – API & Web Services](../Documentation/SecurityTesting/ASVS/V4_API_WebServiceSecurity/apiWebservice.md)
- [V5 – File Handling](../Documentation/SecurityTesting/ASVS/V5_FileHandling/fileHandling.md)
- [V6 – Authentication](../Documentation/SecurityTesting/ASVS/V6_Authentication/authentication.md)
- [V7 – Session Management](../Documentation/SecurityTesting/ASVS/V7_SessionManagement/sessionManagement.md)
- [V8 – Authorization](../Documentation/SecurityTesting/ASVS/V8_Authorization/authorization.md)
- [V9 – Self-contained Tokens](../Documentation/SecurityTesting/ASVS/V9_Self-containedTokens/self_containedTokens.md)
- [V10 – OAuth & OIDC](../Documentation/SecurityTesting/ASVS/V10_OAuthOIDC/oauthOidc.md)
- [V11 – Cryptography](../Documentation/SecurityTesting/ASVS/V11_Cryptography/V11-Cryptography.md)
- [V12 – Secure Communication](../Documentation/SecurityTesting/ASVS/V12_SecureCommunication/secureCommunication.md)
- [V13 – Configuration](../Documentation/SecurityTesting/ASVS/V13_Configuration/configuration.md)
- [V14 – Data Protection](../Documentation/SecurityTesting/ASVS/V14_DataProtection/dataProtection.md)
- [V15 – Secure Coding & Architecture](../Documentation/SecurityTesting/ASVS/V15_Secure_Coding_and_Archite/V15-SecureCoding.md)
- [V16 – Security Logging & Error Handling](../Documentation/SecurityTesting/ASVS/V16_Security_Loggin_Error_Handling/V16-Security-Logging-Error-Handling.md)
- [V17 – Communications](../Documentation/SecurityTesting/ASVS/V17_Communications/V17-Communications.md)

---

## Work Distribution

During this phase, the workload was distributed among the group members as follows:

- 1221137 - Diogo Pereira
  - [Domain Model](../Documentation/Architecture/Diagrams/DomainModel/ArcadeHaven_Domain_Model.svg)
  - [ASVS](../Documentation/SecurityTesting/ASVS)
    - [Authentication](../Documentation/SecurityTesting/ASVS/V6_Authentication/authentication.md)
    - [Web Frontend Security](../Documentation/SecurityTesting/ASVS/V3_WebFrontendSecurity/webFrontendSecurity.md)
    - [API and Web Service](../Documentation/SecurityTesting/ASVS/V4_API_WebServiceSecurity/apiWebservice.md)
    - [Self-contained Tokens](../Documentation/SecurityTesting/ASVS/V9_Self-containedTokens/self_containedTokens.md)
    - [OAuth and OIDC](../Documentation/SecurityTesting/ASVS/V10_OAuthOIDC/oauthOidc.md)
    - [Secure Communication](../Documentation/SecurityTesting/ASVS/V12_SecureCommunication/secureCommunication.md)
    - [Data Protection](../Documentation/SecurityTesting/ASVS/V14_DataProtection/dataProtection.md)
    - [File Handling](../Documentation/SecurityTesting/ASVS/V5_FileHandling/fileHandling.md)
    - [WebRTC]()
  - [STRIDE](../Documentation/Architecture/Dataflow/arcadehaven-dfd.pdf)
  - [Mitigations](../Documentation/Mitigations/mitigations.md)
  - [Risk Assessment](../Documentation/ThreatModeling/RiskAssessment/riskAssessment.md)
  - [Threat Identification and Analysis](../Documentation/ThreatModeling/ThreatIdentificationAndAnalysis/threatIdentificationAndAnalysis.md)
  - [Threat Model](../Documentation/ThreatModeling/ThreatModel.md)


- 1250505 - Diogo Sousa
  - [Requirements](../Documentation/Requirements/requirements.md)
  - [Architecture Views](../Documentation/Architecture/Diagrams/Views/Views.md)
  - [Abuse Cases](../Documentation/ThreatModeling/AbuseCases/AbuseCases.md)
  - [Dataflow Diagrams](../Documentation/Architecture/Dataflow/dataflow.md)
  - [Mitigations](../Documentation/Mitigations/mitigations.md)
  - [Security Testing](../Documentation/SecurityTesting/)
    - [Security Testing Plan](../Documentation/SecurityTesting/securityTesting.md)
    - [Traceability Matrix](../Documentation/SecurityTesting/TraceabilityMatrix.md)

 

- 1250491 - Acácio Coutinho
  - [DREAD Analysis](../Documentation/SSDLC/DREAD.md)
  - [ASVS](../Documentation/SecurityTesting/ASVS)
    - [Encoding and Sanitization](../Documentation/SecurityTesting/ASVS/V1_EncodingAndSanitization/encondingAndSanitization.md)
    - [Validation and Business Logic](../Documentation/SecurityTesting/ASVS/V2_ValidationAndBusinessLogic/validationAndBusinessLogic.md)
    - [Session Management](../Documentation/SecurityTesting/ASVS/V7_SessionManagement/sessionManagement.md)
    - [Authorization](../Documentation/SecurityTesting/ASVS/V8_Authorization/authorization.md)
    - [Configuration](../Documentation/SecurityTesting/ASVS/V13_Configuration/configuration.md)


- 1250516 - Gabriel Proença
  - [Attack Trees](../Documentation/ThreatModeling/AttackTrees/AttackTrees.md)
    - [Account Takeover](../Documentation/ThreatModeling/AttackTrees/Account-Takeover-Attack-Tree.puml)
    - [Input Injection](../Documentation/ThreatModeling/AttackTrees/Input-Injection-Attack-Tree.puml)
    - [Order Tampering](../Documentation/ThreatModeling/AttackTrees/Order-Tampering-Attack-Tree.puml)
  - [DREAD Analysis](../Documentation/SSDLC/DREAD.md)
  - [ASVS](../Documentation/SecurityTesting/ASVS)
    - [Cryptography](../Documentation/SecurityTesting/ASVS/V11_Cryptography/V11-Cryptography.md)
    - [Secure Coding and Architecture](../Documentation/SecurityTesting/ASVS/V15_Secure_Coding_and_Archite/V15-SecureCoding.md)
    - [Security Logging and Error Handling](../Documentation/SecurityTesting/ASVS/V16_Security_Loggin_Error_Handling/V16-Security-Logging-Error-Handling.md)
  - [Threat Model Review Workflow](../Documentation/SecurityTesting/ThreatModelReviewWorkflow.md)