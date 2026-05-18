# Deliverable — Phase 2: Sprint 1

This document presents the work developed during the **first sprint** for the **second part** of the **ArcadeHaven** project, 
including the respective contributors.

This spring focused on the automation of security practices in the software development lifecycle (SDLC), including 
development and implementation of a DevSecOps pipeline, secure development practices, and continuous testing.

Additionally, it also comprehended the development and testing of enough system functionalities to develop tests, implement 
static code analysis, artifact scanning and other relevant security practices.

As such, during this sprint, the team carried out the following activities:
- Development and testing of the ArcadeHaven API;
- Configuration of a remote database, authentication services and file storage for the application;
- Automation of application builds, tests, code analysis, artifact scanning and security testing;
- Implementation of a DevSecOps pipeline with integration of DAST, SCA and DAST pipelines;
- Automation of Github version releases;
- Code reviews and secure coding validation.

---

### Project Structure

```
├── .github/
├── Api/
│   ├── logs/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/isep/desosfs/arcadehaven/
│   │   │   │   ├── Config/
│   │   │   │   ├── Controller/
│   │   │   │   ├── Domain/
│   │   │   │   ├── Dto/
│   │   │   │   ├── Exception/
│   │   │   │   ├── Repository/
│   │   │   │   ├── Security/
│   │   │   │   ├── Service/
│   │   │   │   ├── Validation/
│   │   │   │   └── ArcadehavenApplication.java
│   │   │   └── resources/
│   │   └── tests/
│   │       ├── java/isep/desofs/arcadehaven/
│   │       │   ├── Config/
│   │       │   ├── Controller/
│   │       │   ├── Domain/
│   │       │   ├── Dto/
│   │       │   ├── Exception/
│   │       │   ├── Security/
│   │       │   ├── Service/
│   │       │   ├── Smoke/
│   │       │   └── ArcadehavenApplicationTests.java
│   │       └── resources/
│   ├── docker-compose.yml
│   ├── Dockerfile
│   └── pom.xml
├── Deliverables/
│   ├── Phase1/
│   │   └── Part_1.md
│   ├── Phase2/
│   │   └── Sprint1/
│   │       └── Part2_Sprint_1.md
│   └── ASVS_5.0_Tracker.xlsx
├── Documentation/
│   ├── Analysis/
│   ├── Architecture/
│   ├── Mitigations/
│   ├── Overview/
│   │   └── README.md
│   ├── Requirements/
│   ├── SecurityTesting/
│   ├── SSDLC/
│   └── Project.pdf
├── keycloak/
│   └── realm-export.json
├── ArcadeHaven.postman_collection.json
├── CHANGELOG.md
├── HOW_TO_RUN.md
└── README.md
```

---

### Documentation Index

#### Application Documentation

For previous delivery documentation, please refer to:
- [Report - Part 1](../../../Deliverables/Phase1/Part_1.md).

Additionally, for a overview of all documentation prepared for the **ArcadeHaven** project refer to:
- [Overview](../../../Documentation/Overview/overview.md)

---

#### Root Configuration & Tools

| Artefact | Description |
|---|---|
| [README](../../../README.md) | Main project overview |
| [CHANGELOG](../../../CHANGELOG.md) | Project change history |
| [HOW TO RUN](../../../HOW_TO_RUN.md) | Setup and execution instructions |

---

#### Backend implementation

| Artefact                                                                        | Description                                                                                                                |
|---------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| [API source code](../../../Api/src/main/java/isep/desosfs/arcadehaven/)         | Main backend implementation containing controllers, services, repositories, security, validation, DTOs, and domain entities |
| [Configuration](../../../Api/src/main/java/isep/desosfs/arcadehaven/Config/)    | API Configuration                                                                                                          |
| [Controllers](../../../Api/src/main/java/isep/desosfs/arcadehaven/Controller/)  | REST API endpoints and request handling                                                                                    |
| [Domain](../../../Api/src/main/java/isep/desosfs/arcadehaven/Domain/)           | Core domain entities and business objects                                                                                  |
| [DTO](../../../Api/src/main/java/isep/desosfs/arcadehaven/Dto/)                 | Data Transfer Objects for API communication                                                                                |
| [Exceptions](../../../Api/src/main/java/isep/desosfs/arcadehaven/Exception/)    | Exception handling and error responses                                                                                     |
| [Repositories](../../../Api/src/main/java/isep/desosfs/arcadehaven/Repository/) | Persistence layer and database interaction                                                                                 |
| [Security](../../../Api/src/main/java/isep/desosfs/arcadehaven/Security/)       | Keycloak configuration, security configuration, audits and event handler                                                   |
| [Services](../../../Api/src/main/java/isep/desosfs/arcadehaven/Service/)        | Business logic layer and application services                                                                              |
| [Validation](../../../Api/src/main/java/isep/desosfs/arcadehaven/Validation/)   | Custom validation rules and input validation                                                                               |

---

#### Tests

| Artefact                                                                            | Description                     |
|-------------------------------------------------------------------------------------|---------------------------------|
| [Config](../../../Api/src/test/java/isep/desosfs/arcadehaven/Config/)               | Configuration tests             |
| [Controller](../../../Api/src/test/java/isep/desosfs/arcadehaven/Controller/)       | Controller tests |
| [Domain](../../../Api/src/test/java/isep/desosfs/arcadehaven/Domain/)           | Domain tests              |
| [Dto](../../../Api/src/test/java/isep/desosfs/arcadehaven/Dto/)               | DTO tests                 |
| [Exception](../../../Api/src/test/java/isep/desosfs/arcadehaven/Exception/)       | Exception handling tests  |
| [Security](../../../Api/src/test/java/isep/desosfs/arcadehaven/Security/)       | Security-related tests |
| [Service](../../../Api/src/test/java/isep/desosfs/arcadehaven/Service/)        | Service layer tests       |
| [Smoke](../../../Api/src/test/java/isep/desosfs/arcadehaven/Smoke/)          | Smoke tests |
| [Validation](../../../Api/src/test/java/isep/desosfs/arcadehaven/Validation/)   | Validators tests                |

---

#### Docker Configuration and Deployment

| Artefact | Description |
|---|--------------------|
| [Docker configuration](../../../Api/docker-compose.yml) | Multi-container docker environment configuration |
| [Dockerfile](../../../Api/Dockerfile) | Application docker image build file              |

---

#### Additional Deliverables

| File | Description |
|---|---|
| [ASVS_5.0_Tracker.xlsx](../../ASVS_5.0_Tracker.xlsx) | OWASP ASVS 5.0 tracking and assessment spreadsheet |

---

## Development and Implemented Features

### Authentication & Session Management
- User (Buyer, Publisher and Admin) registration and login via Keycloak
- User logout with session revocation

---

### User Profile Management
- View user profile
- Update user profile

---

### Game Management
- Get all games
- Get game by ID
- Get filtered games (title, category, price range)
- Get Publisher games
- Create game (Publisher)
- Update game (Publisher)
- Get Game Metrics (Publisher)
- Upload Game Files (Publisher)
- Download Game Files

---

### Orders Management
- Get orders (Buyer)
- Get order by ID (Buyer)
- Create order (Buyer)
- Add game to order (Buyer)
- Remove game from order (Buyer)
- Complete order (Buyer)
- Download order invoice (Buyer)
- Cancel order (Buyer)

---

### Library Management
- Get library (Buyer)
- Import Game Key (Buyer)

---

## Administrator Functionalities
- Get all users
- Get user by ID
- Deactivate/reactivate user
- Change user role
- Get all games
- Approve/reject game
- Remove game 
- Suspend user library
- Resume user library

---


### Pipeline Automated Practices

| Practice                             | Tooling                         | When Applied |
|--------------------------------------|---------------------------------|---|
| Static analysis (SAST)               | CodeQL                          | Every push/PR to `main` |
| Code quality / code smells           | SpotBugs + Checkstyle           | Every push/PR to `main` |
| Secret / credential scanning         | Gitleaks                        | Every push/PR to `main` |
| Application Jar build                | Maven Wrapper Plugin            | Every push/PR to `main` |
| Automated unit and integration tests | JUnit 5 (unit + integration)    | Every push/PR to `main` |
| Generate tests coverage report       | JaCoCo Maven Plugin             | Every push/PR to `main` |
| SBOM generation                      | CycloneDX Maven Plugin          | Every push/PR to `main` |
| Application Docker image build       | Dockerfile + Docker Buildx      | Every push/PR to `main` |
| Dependency vulnerability scan (SCA)  | OWASP Dependency Check          | Every push/PR to `main` |
| Container vulnerability scan         | Docker Scout + Trivy            | Every push/PR to `main` |
| Dynamic scan (DAST)                  | OWASP ZAP Baseline              | Every push/PR to `main` |
| Security tests                       | JUnit 5 + custom test utilities | Every push/PR to `main` |
| PR Labeler                           | GitHub Action                   | Every PR to `main` |
| Github version releases              | Release Please GitHub Action    | Every push to `main` |
| Github Docker Publish                | GitHub Action + Docker Buildx   | Every push to `main` |

Each practice implementation can be further explored in:
- [Pipelines](../../../.github/workflows)

---

### Code Reviews

Before merging to `main`, code changes submitted through Pull Requests are to be reviewed by at least one other team member. 

All PRs were reviewed by a second team member before merge. Reviews validated implementation correctness, pipeline green status, and absence of introduced security regressions. PR and respective reviews can be explored in the [GitHub Pull Requests history](https://github.com/mei-desofs/desofs2026-wed_nap_2/pulls).

---

## ASVS

The ASVS 5.0 assessment is maintained in [ASVS_5.0_Tracker.xlsx](../../ASVS_5.0_Tracker.xlsx).

---

## Work Distribution

During this phase, the workload was distributed among the group members as follows:

- 1221137 - Diogo Pereira
    - [Development of the API](../../../Api/src/main/java/isep/desosfs/arcadehaven/)
    - [Keycloak Configuration](../../../Api/src/main/java/isep/desosfs/arcadehaven/Config/KeycloakAdminConfig.java)
    - [Database Configuration](../../../Api/src/main/resources/application.properties)
    - [Pipelines](../../../.github/workflows) 
      - [PR Labeler](../../../.github/workflows/pr-labeler.yml) 
      - [Docker Publisher](../../../.github/workflows/docker-publish.yml)
    - [ASVS Implementations](../../ASVS_5.0_Tracker.xlsx)
    - [Unit Tests](../../../Api/src/test/java/isep/desosfs/arcadehaven/)
    - [Dockerfile](../../../Api/Dockerfile)
    - [Docker Configuration](../../../Api/docker-compose.yml)


- 1250505 - Diogo Sousa
    - [Pipelines](../../../.github/workflows)
    - [GiHub Security Configuration](https://github.com/mei-desofs/desofs2026-wed_nap_2/security)
    - [GitHub Issues](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues)
    - [GitHub Secrets and Variables](https://github.com/mei-desofs/desofs2026-wed_nap_2/settings/secrets/actions)
    - [Docker Configuration](../../../Api/docker-compose.yml)
    - [Remote File Storage Configuration](../../../Api/src/main/java/isep/desosfs/arcadehaven/Service/SftpStorageService.java)
    - [Application Improvements](../../../Api/src/main/java/isep/desosfs/arcadehaven/)
      - [StringTo Converters](../../../Api/src/main/java/isep/desosfs/arcadehaven/Config)
      - [NoHtml Validator](../../../Api/src/main/java/isep/desosfs/arcadehaven/Validation/NoHtmlValidator.java)
    - [Improve Security Testing Docs](../../../Documentation/SecurityTesting/securityTesting.md)
    - [Security Tests](../../../Api/src/test/java/isep/desosfs/arcadehaven/Security/SecurityIntegrationTests.java)
    - [Smoke Tests](../../../Api/src/test/java/isep/desosfs/arcadehaven/Smoke/SmokeTests.java)
    - [ASVS Review and Small Improvements](../../ASVS_5.0_Tracker.xlsx)


- 1250491 - Acácio Coutinho
    - [Phase 1 Docs Improvements](../../../Deliverables/Phase1/Part_1.md)
    - [Report Documentation](Part_2_Sprint_1.md)
    - [Unit Tests](../../../Api/src/test/java/isep/desosfs/arcadehaven/)
    - [Integration Tests](../../../Api/src/test/java/isep/desosfs/arcadehaven/)


