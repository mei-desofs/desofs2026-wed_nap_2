# Expanded Abuse Cases

## 1. Objective

This document expands abuse-case coverage to all major ArcadeHaven domains and links each abuse case to threats, risks, controls, and planned tests.

## 1.1 Diagram Set

- Auth and authorization diagram: [auth-abuse-case.puml](auth-abuse-case.puml)
- Game management diagram: [game-abuse-case.puml](game-abuse-case.puml)
- Orders and library diagram: [orders-library-abuse-case.puml](orders-library-abuse-case.puml)
- File operations diagram: [file-operations-abuse-case.puml](file-operations-abuse-case.puml)
- Legacy seed diagram: [authorization-abuse-case.puml](../v2/authorization-abuse-case.puml)

## 2. Abuse Cases Catalog

| Abuse Case ID | Abuse Case | Domain | Primary Threat IDs | Primary Risk IDs | Related Requirements | Planned Controls | Planned Tests |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AC-01 | Brute-force login attempts | Authentication | TH-01 | R-01 | RF-02, RNF-09 | AC-006, LM-001 | ST-010 |
| AC-02 | JWT token theft and replay | Authentication | TH-02 | R-02 | RNF-02, RNF-03, RNF-05 | AC-002, AC-003, AC-005 | ST-002, ST-003, ST-005 |
| AC-03 | Privilege escalation to admin capabilities | Authorization | TH-03, TH-09 | R-03, R-09 | RF-03, RF-04, RF-05, RNF-04, RNF-07 | AC-004, LM-002 | ST-004, ST-008 |
| AC-04 | SQL injection in search and filter parameters | Input Validation | TH-04 | R-04 | RF-11, RNF-06 | IV-001, IV-002 | ST-006 |
| AC-05 | Malicious upload with fake MIME/extension | File Operations | TH-05 | R-05 | RF-29, RF-30, RNF-10 | FO-001, FO-002, FO-005 | ST-011, ST-013, ST-014 |
| AC-06 | Path traversal on invoice download | File Operations | TH-06 | R-06 | RF-19, RF-27 | FO-003, FO-004 | ST-012 |
| AC-07 | Activation key disclosure to unauthorized user | Library/Security | TH-07 | R-07 | RF-23, RF-28, RNF-08 | FO-004, SC-003 | ST-009, ST-012 |
| AC-08 | Order tampering by parameter manipulation | Orders | TH-08 | R-08 | RF-14, RF-20 | IV-001, AC-004 | ST-015 |
| AC-09 | Username enumeration during login errors | Authentication | TH-01, TH-09 | R-01, R-09 | RF-02, RNF-07, RNF-09 | AC-006, LM-001 | ST-010, ST-008 |
| AC-10 | Password reset abuse and token guessing | Authentication | TH-01, TH-02 | R-01, R-02 | RF-02, RNF-02 | AC-002, AC-006 | ST-002, ST-010 |
| AC-11 | Forced browsing to publisher-only routes | Authorization | TH-03 | R-03 | RF-07, RF-08, RNF-04 | AC-004 | ST-004 |
| AC-12 | Forced browsing to admin game approval endpoints | Authorization | TH-03 | R-03 | RF-09, RNF-04 | AC-004 | ST-004 |
| AC-13 | Mass assignment in profile updates | Input Validation | TH-04, TH-08 | R-04, R-08 | RF-06, RNF-06 | IV-001, AC-004 | ST-006, ST-015 |
| AC-14 | Stored XSS via game description fields | Input Validation | TH-04 | R-04 | RF-07, RF-08, RNF-06 | IV-003 | ST-007 |
| AC-15 | Duplicate order race condition abuse | Orders | TH-08 | R-08 | RF-14, RF-15 | IV-001 | ST-015 |
| AC-16 | Unauthorized order cancellation | Orders | TH-08, TH-03 | R-08, R-03 | RF-20, RNF-04 | AC-004 | ST-015 |
| AC-17 | Unauthorized library entry revocation | Library | TH-03 | R-03 | RF-25, RNF-04 | AC-004, LM-002 | ST-004, ST-008 |
| AC-18 | Exposure of sensitive logs with tokens/passwords | Logging/Secrets | TH-07, TH-09 | R-07, R-09 | RNF-07, RNF-23 | SC-002, LM-003 | ST-008 |
| AC-19 | Abuse of RAWG integration for payload pollution | External Integration | TH-04 | R-04 | RF-12, RNF-06, RNF-16 | IV-001, DC-001 | ST-006 |
| AC-20 | Abuse of startup directory creation with unsafe paths | File Operations | TH-06, TH-05 | R-06, R-05 | RF-26, RF-27 | FO-003, FO-005 | ST-012 |

## 3. Coverage Summary

- Total abuse cases: 20.
- Authentication and authorization: 8.
- Input and data integrity: 5.
- Order and library flows: 4.
- File and storage operations: 3.

Coverage is represented across the domain-specific PlantUML diagrams listed in Section 1.1.

## 4. Governance Rules

1. Every new endpoint must be evaluated against this catalog.
2. Every new high-risk abuse case must be linked to at least one threat, one risk, and one planned test.
3. Critical or High abuse cases cannot remain without mapped controls before release.
4. Any abuse-case update must be reflected in the relevant PlantUML diagram and in the traceability matrix.
