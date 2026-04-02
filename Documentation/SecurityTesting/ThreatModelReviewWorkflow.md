# Threat Model Review Workflow

## 1. Purpose

This workflow defines how threat modeling is reviewed and updated when the system changes.

## 2. Trigger Conditions

A review is mandatory when at least one condition is true:

- New API endpoint is added.
- Existing endpoint behavior or authorization rule changes.
- Dataflow changes across trust boundaries.
- New user-triggered file operation is introduced.
- New external integration is added.

## 3. Roles

- Threat Model Owner: updates threat register and mappings.
- Security Reviewer: validates STRIDE and risk scoring quality.
- Technical Owner: confirms feasibility and implementation details.
- Approver: confirms acceptance of risk and planned actions.

## 4. Workflow Steps

1. Change detection and review request.
2. Scope definition of impacted components and assets.
3. STRIDE reassessment for impacted scope.
4. Risk score update in risk register.
5. Mitigation update and control mapping.
6. Security test mapping update in traceability matrix.
7. Review decision and action tracking.

## 5. SLA and Output

- Review SLA: 3 business days after request.
- Outputs:
  - Updated threat entries.
  - Updated risk and mitigation mappings.
  - Updated planned tests and traceability links.
  - Open actions with owner and due date.

## 6. Quality Checklist

- Scope includes all changed trust boundaries.
- No new endpoint remains without threat classification.
- All Critical and High risks have treatment and planned tests.
- Risk acceptance is explicit and owner-approved.
