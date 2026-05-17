[← Back to index page](../Overview/overview.md)

# Threat Model Review Workflow

## 1. Purpose

This document defines how threat modeling is reviewed throughout the development lifecycle and updated when the system changes.

## 2. Trigger Conditions

A review is mandatory when at least one condition is verified:

- New API endpoint is added;
- Existing endpoint behavior or authorization rule changes;
- Dataflow changes afects trust boundaries;
- New feature integratin within the system;
- New user-triggered file operation is introduced;
- New external integration is added.

## 3. Workflow Steps

1. Change detection and review request;
2. Scope definition of impacted components and assets;
3. STRIDE reassessment for impacted scope;
4. Mitigation update and control mapping;
5. Update security tests and its mapping traceability matrix;
6. Review decision and action tracking.

## 4. Quality Checklist

- Scope includes all changed trust boundaries;
- No new endpoint or feature remains without threat classification;
- All Critical and High risks have treatment and planned tests;
- Risk acceptance is explicit and documented.
