# P1 – Analysis and Design

This document presents the work developed during the **first phase**, including the respective contributors.

This phase corresponds to the first two stages of the **Secure Software Development Life Cycle (SSDLC)**:
- **Analysis**
- **Design**

The **Analysis** stage focuses on identifying the need for security measures within the system, gathering functional and non-functional requirements, and performing risk assessment.

The **Design** stage is responsible for defining the system architecture and planning appropriate security mechanisms to mitigate the risks identified during the analysis phase.

Together, these stages ensure that security is considered from the early stages of development, enabling a more robust and secure system.

## Folder Guide

- Deliverables: Final delivery artifacts used for submission.
- Documentation: Main Phase 1 knowledge base.
- Overview: Entry point for reading the complete documentation.
- Architecture: System architecture, domain model, and data flow artifacts.
- Requirements: Functional and non-functional requirements baseline.
- Mitigations: Security controls mapped to risks and threats.
- SSDLC: Process and risk methodology (including DREAD).
- ThreatModeling: Threat inventory, risk analysis, abuse cases, and attack trees.
- SecurityTesting: Security planning, workflow, ASVS mapping, and traceability.
- ASVS: Chapter-by-chapter security verification documentation.

## Architecture Views

- [Logical View](../Documentation/Architecture/Diagrams/Views/logical-view.puml)
- [Physical Deployment View (Docker)](../Documentation/Architecture/Diagrams/Views/physical-deployment-docker.puml)

## Project Tree

- [desofs2026-wed_nap_2](../)
  - [README.md](../README.md)
  - [Deliverables](./)
    - [Part_1.md](./Part_1.md)
    - [Phase1](./Phase1/)
    - [ASVS_5.0_Tracker.xlsx](./ASVS_5.0_Tracker.xlsx)
  - [Documentation](../Documentation/)
    - [Overview](../Documentation/Overview/)
      - [README.md](../Documentation/Overview/README.md)
    - [Architecture](../Documentation/Architecture/)
      - [analysis.md](../Documentation/Architecture/architecture.md)
      - [Dataflow](../Documentation/Architecture/Dataflow/)
        - [dataflow.md](../Documentation/Architecture/Dataflow/dataflow.md)
        - [arcadehaven-dfd.json](../Documentation/Architecture/Dataflow/arcadehaven-dfd.json)
      - [Diagrams](../Documentation/Architecture/Diagrams/)
        - [DomainModel](../Documentation/Architecture/Diagrams/DomainModel/)
          - [domain_model.puml](../Documentation/Architecture/Diagrams/DomainModel/domain_model.puml)
          - [ArcadeHaven_Domain_Model.svg](../Documentation/Architecture/Diagrams/DomainModel/ArcadeHaven_Domain_Model.svg)
        - [Views](../Documentation/Architecture/Diagrams/Views/)
          - [README.md](../Documentation/Architecture/Diagrams/Views/README.md)
          - [logical-view.puml](../Documentation/Architecture/Diagrams/Views/logical-view.puml)
          - [physical-deployment-docker.puml](../Documentation/Architecture/Diagrams/Views/physical-deployment-docker.puml)
    - [Requirements](../Documentation/Requirements/)
      - [requirements.md](../Documentation/Requirements/requirements.md)
    - [Mitigations](../Documentation/Mitigations/)
      - [Mitigations.md](../Documentation/Mitigations/Mitigations.md)
    - [SSDLC](../Documentation/SSDLC/)
      - [SSDLC.md](../Documentation/SSDLC/SSDLC.md)
      - [DREAD.md](../Documentation/SSDLC/DREAD.md)
    - [ThreatModeling](../Documentation/ThreatModeling/)
      - [ThreatIdentificationAndAnalysis](../Documentation/ThreatModeling/ThreatIdentificationAndAnalysis/)
        - [threatIdentificationAndAnalysis.md](../Documentation/ThreatModeling/ThreatIdentificationAndAnalysis/threatIdentificationAndAnalysis.md)
      - [RiskAssessment](../Documentation/ThreatModeling/RiskAssessment/)
        - [riskAssessment.md](../Documentation/ThreatModeling/RiskAssessment/riskAssessment.md)
      - [AbuseCases](../Documentation/ThreatModeling/AbuseCases/)
          - [AbuseCases.md](../Documentation/ThreatModeling/AbuseCases/AbuseCases.md)
      - [AttackTrees](../Documentation/ThreatModeling/AttackTrees/)
        - [AttackTrees.md](../Documentation/ThreatModeling/AttackTrees/AttackTrees.md)
        - [TH-01-Account-Takeover-Attack-Tree.puml](../Documentation/ThreatModeling/AttackTrees/Account-Takeover-Attack-Tree.puml)
        - [TH-04-Input-Injection-Attack-Tree.puml](../Documentation/ThreatModeling/AttackTrees/Input-Injection-Attack-Tree.puml)
        - [TH-08-Order-Tampering-Attack-Tree.puml](../Documentation/ThreatModeling/AttackTrees/Order-Tampering-Attack-Tree.puml)
    - [SecurityTesting](../Documentation/SecurityTesting/)
      - [securityTesting.md](../Documentation/SecurityTesting/securityTesting.md)
      - [ThreatModelReviewWorkflow.md](../Documentation/SecurityTesting/ThreatModelReviewWorkflow.md)
      - [TraceabilityMatrix.md](../Documentation/SecurityTesting/TraceabilityMatrix.md)
      - [ASVS](../Documentation/SecurityTesting/ASVS/)
        - [V1_EncodingSanitization](../Documentation/SecurityTesting/ASVS/V1_EncodingSanitization/)
        - [V2_ValidationBusinessLogic](../Documentation/SecurityTesting/ASVS/V2_ValidationBusinessLogic/)
        - [V3_WebFrontendSecurity](../Documentation/SecurityTesting/ASVS/V3_WebFrontendSecurity/)
        - [V4_API_WebServiceSecurity](../Documentation/SecurityTesting/ASVS/V4_API_WebServiceSecurity/)
        - [V5_FileHandling](../Documentation/SecurityTesting/ASVS/V5_FileHandling/)
        - [V6_Authentication](../Documentation/SecurityTesting/ASVS/V6_Authentication/)
        - [V7_SessionManagement](../Documentation/SecurityTesting/ASVS/V7_SessionManagement/)
        - [V8_Authorization](../Documentation/SecurityTesting/ASVS/V8_Authorization/)
        - [V9_Self-contained Tokens](../Documentation/SecurityTesting/ASVS/V9_Self-contained%20Tokens/)
        - [V10_OAuthOIDC](../Documentation/SecurityTesting/ASVS/V10_OAuthOIDC/)
        - [V11_Cryptography](../Documentation/SecurityTesting/ASVS/V11_Cryptography/)
        - [V12_SecureCommunication](../Documentation/SecurityTesting/ASVS/V12_SecureCommunication/)
        - [V13_Configuration](../Documentation/SecurityTesting/ASVS/V13_Configuration/)
        - [V14_DataProtection](../Documentation/SecurityTesting/ASVS/V14_DataProtection/)
        - [V15_Secure_Coding_and_Archite](../Documentation/SecurityTesting/ASVS/V15_Secure_Coding_and_Archite/)
        - [V16_Loggin_Error_Handling](../Documentation/SecurityTesting/ASVS/V16_Loggin_Error_Handling/)
        - [V17_Communications](../Documentation/SecurityTesting/ASVS/V17_Communications/)

## Work Distribution

During this phase, the workload was distributed among the group members as follows:

- 1221137 - Diogo Pereira
  - [Domain Model](../Documentation/Architecture/Diagrams/DomainModel/ArcadeHaven_Domain_Model.svg)
  - [ASVS](../Documentation/SecurityTesting/ASVS)
    - [Authentication](../Documentation/SecurityTesting/ASVS/V6_Authentication/authentication.md)
    - [Web Frontend Security](../Documentation/SecurityTesting/ASVS/V3_WebFrontendSecurity/webFrontendSecurity.md)
    - [API and Web Service](../Documentation/SecurityTesting/ASVS/V4_API_WebServiceSecurity/apiWebservice.md)
    - [Self-contained Tokens](../Documentation/SecurityTesting/ASVS/V9_Self-contained%20Tokens/self_containedTokens.md)
    - [OAuth and OIDC](../Documentation/SecurityTesting/ASVS/V10_OAuthOIDC/oauthOidc.md)
    - [Secure Communication](../Documentation/SecurityTesting/ASVS/V12_SecureCommunication/secureCommunication.md)
    - [Data Protection](../Documentation/SecurityTesting/ASVS/V14_DataProtection/dataProtection.md)
    - [File Handling](../Documentation/SecurityTesting/ASVS/V5_FileHandling/fileHandling.md)
    - [WebRTC]()
  - [STRIDE](../Documentation/Architecture/Dataflow/arcadehaven-dfd.pdf)
  - [Mitigations](../Documentation/Mitigations/mitigations.md)
  - [Risk Assessment](../Documentation/ThreatModeling/RiskAssessment/riskAssessment.md)
  - [Threat Identification and Analysis](../Documentation/ThreatModeling/ThreatIdentificationAndAnalysis/threatIdentificationAndAnalysis.md)
 

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
    - [Security Logging and Error Handling](../Documentation/SecurityTesting/ASVS/V16_Loggin_Error_Handling/V16-Logging-Error-Handling.md)
    - [Communications](../Documentation/SecurityTesting/ASVS/V17_Communications/V17-Communications.md)
  - [Security Testing](../Documentation/SecurityTesting/)
    - [Threat Model Review Workflow](../Documentation/SecurityTesting/ThreatModelReviewWorkflow.md)