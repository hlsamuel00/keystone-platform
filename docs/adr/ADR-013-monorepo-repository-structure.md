# ADR-013: Monorepo Repository Structure

## Status

Accepted

## Date Accepted

2026-06-05

## Context

Keystone Platform consists of multiple services, multiple programming languages, platform-wide architecture documentation, Architecture Decision Records (ADRs), CI/CD workflows, and review controls. Before implementation began, the platform required a repository structure capable of supporting both the technical architecture and the project's educational and portfolio objectives.

Repository structure decisions become increasingly difficult to change as implementation progresses. CI/CD pipelines, branch protection rules, review workflows, deployment processes, documentation references, and contributor expectations all become coupled to repository organization. Selecting a repository structure before implementation avoids disruptive restructuring later in the project lifecycle.

The platform also serves as a learning and portfolio project. A cold reviewer should be able to clone a single repository and immediately understand the platform's architecture, decisions, implementation status, and operational model without navigating multiple repositories.

This decision is also inherently self-referential. The ADR is committed within the repository structure it defines, and the review controls established by ADR-011 and ADR-012 become effective through the organizational model described here. As a result, the repository structure serves as both implementation scaffolding and governance infrastructure.

## Alternatives Considered

### Option A: Multi-Repository Architecture

Each service resides in an independent repository with separate pipelines, versioning, and deployment workflows.

#### Pros

* Strong service isolation
* Independent release cycles
* Mirrors common enterprise deployment models
* Clear repository ownership boundaries

#### Cons

* Increased operational overhead
* Duplicated CI/CD configuration
* Distributed documentation
* More difficult architectural discovery for new contributors
* Solves a coordination problem that does not exist for a solo-maintainer project

### Option B: Flat Monorepo

Maintain a single repository with service directories at the root and minimal organizational structure.

#### Pros

* Simple layout
* Single repository experience
* Reduced administrative overhead

#### Cons

* Weak ownership signaling
* Limited service-level documentation structure
* Reduced scalability as services mature
* Less clear separation of responsibilities

### Option C: Structured Monorepo with Per-Service Ownership (Selected)

Maintain a single repository while organizing services into independently owned service directories with dedicated documentation, build configuration, tests, and operational artifacts.

#### Pros

* Single source of truth for architecture and implementation
* Clear service ownership boundaries
* Consistent CI/CD and review controls
* Improved discoverability for reviewers and contributors
* Supports polyglot development
* Enables future service extraction if required

#### Cons

* Shared repository lifecycle
* Larger repository footprint over time
* Additional structure compared to a flat monorepo
* Future extraction may require migration effort

## Decision

Keystone Platform SHALL utilize a structured monorepo architecture.

The repository SHALL contain:

* Platform architecture documentation
* Architecture Decision Records
* CI/CD workflow definitions
* Service implementations
* Service-specific documentation
* Service-specific operational runbooks

Each service SHALL own its complete implementation footprint, including:

* Source code
* Tests
* Build configuration
* Docker configuration
* Service documentation
* Operational runbooks

The approved structure is:

```text
platform/
├── docs/
│   ├── design/
│   └── adr/
├── services/
│   ├── kms/
│   ├── csp/
│   └── sr/
├── .github/
│   └── workflows/
└── README.md
```

A future `lib/` directory MAY be introduced if shared operational concerns emerge across services.

The `lib/` directory SHALL be limited to infrastructure and operational concerns.

The following SHALL NOT be placed within `lib/`:

* Cryptographic operations
* Key lifecycle logic
* Secret management logic
* Authorization logic
* Service-owned domain logic

Future extraction of services into independent repositories MAY be evaluated if platform requirements justify additional operational independence.

## Rationale

The primary value of the repository is not merely storing source code but preserving the relationship between architecture, decisions, implementation, and governance.

A monorepo allows the platform architecture document, ADR log, CI/CD configuration, review controls, and service implementations to exist as a cohesive unit. This significantly improves discoverability for both contributors and reviewers while reducing administrative complexity.

Multi-repository architectures provide meaningful benefits when multiple teams require independent deployment and ownership boundaries. Keystone Platform does not currently face that coordination challenge. Introducing those operational costs during MVP development would provide limited architectural value.

The structured monorepo approach preserves ownership boundaries while maintaining repository cohesion. Each service owns its implementation, documentation, tests, and operational guidance, reinforcing the platform-wide emphasis on ownership and accountability.

The decision to defer creation of a shared `lib/` directory reflects a deliberate effort to avoid premature abstraction. The platform currently contains insufficient duplication to justify a shared library layer. Introducing one before a demonstrated need emerges would increase complexity without corresponding value.

Should a shared library eventually become necessary, it will be restricted to operational and infrastructure concerns. This guardrail prevents erosion of service ownership boundaries and preserves the responsibilities established in ADR-004.

The structure further supports future evolution. Because services remain independently organized within the repository, future extraction into standalone repositories remains possible without requiring substantial architectural redesign.

## Consequences

### Positive

* Single source of truth for architecture and implementation
* Improved discoverability for reviewers and contributors
* Consistent CI/CD and review controls
* Strong service ownership boundaries
* Supports polyglot development
* Simplifies project onboarding
* Enables future repository extraction if needed

### Negative

* Shared repository lifecycle
* Larger repository size over time
* Less deployment independence than a multi-repository model
* Future service extraction requires migration effort

### Risks

#### Premature Shared Abstractions

A shared library may become a dumping ground for service-specific logic.

Mitigations:

* Explicit `lib/` guardrails
* ADR-driven architecture reviews
* Service ownership enforcement

#### Monorepo Growth

Repository complexity may increase as services mature.

Mitigations:

* Consistent directory standards
* Service-level documentation
* Service runbooks
* Clear ownership boundaries

#### Future Operational Requirements

Platform growth may eventually justify repository separation.

Mitigations:

* Service isolation by design
* Independent build configurations
* Independent service documentation
* Future extraction reviews as requirements evolve

## Related Decisions

* ADR-001: KMS Owns All Cryptographic Keys
* ADR-004: Separate KMS and CSP Responsibilities
* ADR-009: Language Selection
* ADR-011: CI/CD Pipeline and Review Controls
* ADR-012: AI-Assisted Code Review
