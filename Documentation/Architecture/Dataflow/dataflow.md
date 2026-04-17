# ArcadeHaven Dataflow and Trust Boundaries

## 1. Objective

This document defines the initial dataflow model used to support threat identification and security testing design.

## 2. Dataflow Level 1

### 2.1 External Entities

- Guest user.
- Authenticated buyer.
- Authenticated publisher.
- Authenticated administrator.
- RAWG external API.

### 2.2 Core Processes

- Authentication and token issuance.
- User and role management.
- Game management and search.
- Order and payment workflow.
- Invoice and activation-key generation.
- Library management.
- File upload and file download.

### 2.3 Data Stores

- Relational database for users, roles, games, orders, library, reviews.
- File storage for invoices, activation keys, and uploaded images.
- Security logs and audit events.

### 2.4 Trust Boundaries

- TB-01 Public network boundary between clients and API.
- TB-02 Authentication boundary between guest and authenticated users.
- TB-03 Authorization boundary between buyer, publisher, and admin routes.
- TB-04 Persistence boundary between API and database.
- TB-05 File-system boundary between API and server storage.
- TB-06 External integration boundary between API and RAWG API.

## 3. Dataflow Level 2

## 3.1 Auth Flow

1. User submits credentials.
2. API validates credentials against database.
3. API issues JWT token with expiration.
4. Protected routes validate token and role claims.

Threat focus:

- TH-01 Brute-force login.
- TH-02 Token theft/replay.
- TH-03 Privilege escalation.

## 3.2 Game Upload Flow

1. Publisher submits game metadata and image file.
2. API validates role and input fields.
3. API validates file MIME, extension, and size.
4. API stores file and metadata.

Threat focus:

- TH-04 Injection through metadata fields.
- TH-05 Malicious upload.
- TH-09 Missing audit evidence for privileged changes.

## 3.3 Order and Invoice Flow

1. Buyer creates order with selected games.
2. API validates ownership rules and duplicate purchase constraints.
3. API persists order state.
4. API generates invoice and activation keys in file storage.
5. Buyer downloads invoice through protected endpoint.

Threat focus:

- TH-06 Path traversal in file access.
- TH-07 Activation key disclosure.
- TH-08 Order tampering.

## 3.4 External API Enrichment Flow

1. Backend sends request to RAWG API.
2. Backend receives and normalizes external game data.
3. Backend persists selected fields in local domain model.

Threat focus:

- Data integrity and trust validation on third-party responses.
- Availability fallback for external dependency failures.

## 4. Dataflow to Threat Mapping

| Dataflow Segment | Trust Boundary | Main Threat IDs | Main Planned Tests |
| --- | --- | --- | --- |
| Login and token issuance | TB-01, TB-02 | TH-01, TH-02 | ST-002, ST-003, ST-010 |
| Role-sensitive routes | TB-03 | TH-03, TH-09 | ST-004, ST-008 |
| Search and form inputs | TB-01 | TH-04 | ST-006, ST-007 |
| Upload and file persistence | TB-05 | TH-05 | ST-011, ST-013, ST-014 |
| Invoice download | TB-05 | TH-06 | ST-012 |
| Activation key lifecycle | TB-04, TB-05 | TH-07 | ST-009 |
| Order lifecycle | TB-04 | TH-08 | ST-015 |

## 5. Review Notes

- This dataflow baseline must be reviewed whenever a new endpoint or file operation is added.
- Diagram versions can be added later as PlantUML and linked to this document.
