# Expanded Abuse Cases (v1)

## 1. Purpose

This document defines the v1 abuse-case set for ArcadeHaven and keeps it aligned with the PlantUML diagrams in this folder.

## 2. Diagram Set

- Authentication and authorization: [authorization-abuse-case.puml](authorization-abuse-case.puml)
- Game management: [game-management-abuse-case.puml](game-management-abuse-case.puml)
- Orders and library: [orders-library-abuse-case.puml](orders-library-abuse-case.puml)
- File operations: [file-operations-abuse-case.puml](file-operations-abuse-case.puml)

## 3. Abuse Cases Catalog

| Abuse Case ID | Name | Domain | Mitigations | Threatens | Diagram |
| --- | --- | --- | --- | --- | --- |
| AC-01 | Brute Force Login | Authentication | Lock Account After 5 Tries | Login | [authorization-abuse-case.puml](authorization-abuse-case.puml) |
| AC-02 | JWT Token Replay | Authentication | Token Validation and Expiry | Access Protected Endpoint | [authorization-abuse-case.puml](authorization-abuse-case.puml) |
| AC-03 | Username Enumeration | Authentication | Generic Login Error Message, Input Sanitization | Login | [authorization-abuse-case.puml](authorization-abuse-case.puml) |
| AC-04 | Password Reset Token Guessing | Authentication | MFA | Login | [authorization-abuse-case.puml](authorization-abuse-case.puml) |
| AC-05 | Privilege Escalation | Authorization | RBAC Validation | Change User Role, Manage User Activation | [authorization-abuse-case.puml](authorization-abuse-case.puml) |
| AC-06 | Forced Browse Publisher Routes | Game Management | RBAC Validation | Submit Game, Update Game | [game-management-abuse-case.puml](game-management-abuse-case.puml) |
| AC-07 | Forced Browse Admin Approval Routes | Game Management | RBAC Validation | Approve or Reject Game | [game-management-abuse-case.puml](game-management-abuse-case.puml) |
| AC-08 | Stored XSS in Game Description | Game Management | Input Sanitization | Submit Game, Update Game | [game-management-abuse-case.puml](game-management-abuse-case.puml) |
| AC-09 | RAWG Payload Pollution | Game Management | External Payload Validation | Submit Game | [game-management-abuse-case.puml](game-management-abuse-case.puml) |
| AC-10 | Order Tampering | Orders and Library | Server-side Validation | Create Order | [orders-library-abuse-case.puml](orders-library-abuse-case.puml) |
| AC-11 | Duplicate Order Race | Orders and Library | Idempotency and Concurrency Control | Create Order | [orders-library-abuse-case.puml](orders-library-abuse-case.puml) |
| AC-12 | Unauthorized Order Cancellation | Orders and Library | Authorization Check | Cancel Pending Order | [orders-library-abuse-case.puml](orders-library-abuse-case.puml) |
| AC-13 | Activation Key Disclosure | Orders and Library | Authorization Check | View Activation Keys | [orders-library-abuse-case.puml](orders-library-abuse-case.puml) |
| AC-14 | Unauthorized Library Revocation | Orders and Library | Authorization Check | Manage Library Entries | [orders-library-abuse-case.puml](orders-library-abuse-case.puml) |
| AC-15 | Malicious Upload Bypass | File Operations | File Name Sanitization, MIME Type Verification | Upload Game Image | [file-operations-abuse-case.puml](file-operations-abuse-case.puml) |
| AC-16 | Invoice Path Traversal | File Operations | Authorization Check, Path Normalization, Secure File Storage | Download Invoice | [file-operations-abuse-case.puml](file-operations-abuse-case.puml) |
| AC-17 | Unsafe Startup Path Abuse | File Operations | Path Normalization | Download Invoice | [file-operations-abuse-case.puml](file-operations-abuse-case.puml) |
| AC-18 | Sensitive Log Exposure | File Operations | Log Redaction | Download Invoice | [file-operations-abuse-case.puml](file-operations-abuse-case.puml) |

## 4. Validation Rules

1. Every abuse case must have at least one mitigation in its diagram.
2. Every abuse case must threaten at least one concrete use case.
3. Diagram labels and catalog names must remain identical.
4. Any change in a v1 diagram must update this catalog in the same commit.
