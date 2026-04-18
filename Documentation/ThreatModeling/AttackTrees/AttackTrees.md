# Attack Trees for ArcadeHaven

## 1. Purpose

This document defines why Attack Trees are being introduced in ArcadeHaven and clarifies the threat scope selected for the first iteration.

The objective is to decompose attacker goals into realistic AND/OR paths that can be traced to existing project artifacts:

- Threats (TH-XX)
- Risks (R-XX)
- Abuse cases (AC-XX)
- Mitigation controls (AC/IV/FO/LM/SC/DC)
- Planned security tests (ST-XXX)
- Functional and non-functional requirements (RF-XX, RNF-XX)

This is a depth artifact for threat analysis, not a replacement for the threat register.

## 2. Why Attack Trees in this project

ArcadeHaven already has:

- STRIDE-based threat inventory.
- DREAD-based prioritization.
- Abuse case catalog and diagrams.
- Mitigation catalog and traceability matrix.

Attack Trees are added to make exploitation paths explicit:

- Show alternative attacker routes with OR branches.
- Show mandatory multi-step chains with AND branches.
- Expose preconditions, trust-boundary crossings, and weak links.
- Improve mitigation and testing prioritization for high-impact threats.

## 3. Method summary

For each selected threat:

1. Define the attacker goal (tree root).
2. Decompose major attack strategies (OR).
3. Decompose required chained actions (AND where needed).
4. Link each leaf to existing AC/control/ST/RNF references.
5. Mark residual-risk notes where controls are partial.

## 4. Threats selected for first Attack Tree iteration

The first iteration focuses on TH-01, TH-04, and TH-08 because they represent the strongest security and business impact in the current risk posture.

### 4.1 TH-01

- ID: TH-01
- Current description: Brute-force against login endpoint.
- STRIDE class: Spoofing, Denial of Service.
- Related abuse case: AC-01.
- Related requirements: RF-02, RNF-09.
- Initial DREAD score: 8.6 (Critical).

Why this threat is prioritized:

- It is the only Critical threat in the current register.
- It directly targets account takeover and service abuse potential.
- It is a high-leverage entry point for follow-on attacks.

### 4.2 TH-04

- ID: TH-04
- Current description: SQL injection in search/filter endpoints.
- STRIDE class: Tampering, Information Disclosure.
- Related abuse case: AC-04.
- Related requirements: RF-11, RNF-06.
- Initial DREAD score: 8.4 (High).

Why this threat is prioritized:

- It can compromise confidentiality and integrity of core business data.
- It may affect multiple modules through common input paths.
- It links strongly to external data/input handling concerns.

### 4.3 TH-08

- ID: TH-08
- Current description: Order tampering and ownership bypass.
- STRIDE class: Tampering.
- Related abuse case: AC-08.
- Related requirements: RF-14, RF-20.
- Initial DREAD score: 7.0 (High).

Why this threat is prioritized:

- It targets transaction integrity and business trust.
- It can generate financial and reputational impact.
- It is central to order/invoice/library process correctness.

## 5. Attack Tree UML Artefacts

- [TH-01 Attack Tree (PlantUML)](TH-01-Account-Takeover-Attack-Tree.puml)
- [TH-04 Attack Tree (PlantUML)](TH-04-Input-Injection-Attack-Tree.puml)
- [TH-08 Attack Tree (PlantUML)](TH-08-Order-Tampering-Attack-Tree.puml)

## 6. Relationship to existing project artifacts

Attack Tree nodes will map to existing documentation as follows:

- Threat inventory: ../ThreatIdentificationAndAnalysis/threatIdentificationAndAnalysis.md
- Risk scores: ../RiskAssessment/riskAssessment.md
- Mitigation controls: ../../Mitigations/Mitigations.md
- Test links: ../../SecurityTesting/SecurityTesting.md
- End-to-end traceability: ../../SecurityTesting/TraceabilityMatrix-V2.md

No replacement of existing IDs is required.

## 7. Deliverable boundaries for the first iteration

Included:

- Three threat-focused trees: TH-01, TH-04, TH-08.
- PlantUML sources plus PNG/SVG render outputs.
- Per-tree traceability table from node to controls and tests.

Excluded:

- Full expansion of all TH-XX entries in this iteration.
- New independent taxonomy beyond lightweight tree-node labels.
- Re-definition of STRIDE, DREAD, or abuse-case catalogs already approved.

## 8. Acceptance checks before expanding to more trees

A tree is considered ready when:

1. Root goal is clear and threat-specific.
2. OR/AND decomposition is logically consistent.
3. Leaves map to at least one existing control and one planned test.
4. Diagram and narrative are consistent with current threat and risk docs.

## 9. References

Internal references:

- ../ThreatIdentificationAndAnalysis/threatIdentificationAndAnalysis.md
- ../RiskAssessment/riskAssessment.md
- ../../Mitigations/Mitigations.md
- ../../SecurityTesting/SecurityTesting.md
- ../../SecurityTesting/TraceabilityMatrix-V2.md

External references used for methodology framing:

- OWASP Threat Modeling Process: https://owasp.org/www-community/Threat_Modeling_Process
- Bruce Schneier, Attack Trees (1999): https://www.schneier.com/academic/archives/1999/12/attack_trees.html
- MITRE CWE View 1008 (Architectural Concepts): https://cwe.mitre.org/data/definitions/1008.html
