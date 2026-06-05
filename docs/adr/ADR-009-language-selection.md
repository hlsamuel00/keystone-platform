# ADR-009: Language Selection

## Status

Accepted

## Date Accepted

2026-06-04

## Context

Keystone Platform consists of multiple services with distinct responsibilities, risk profiles, and operational requirements. The platform includes security-critical services responsible for cryptographic asset ownership and execution, as well as metadata-driven services focused primarily on policy management and service relationships.

A common architectural recommendation for distributed systems is to standardize on a single language across all services. While this simplifies operational tooling, hiring, and development workflows, it can also force services with very different requirements into the same technical constraints.

The platform's MVP phase is primarily focused on validating the cryptographic architecture, trust model, service boundaries, and lifecycle management concepts documented throughout the design. During this phase, the project already involves learning and implementing platform security concepts, cryptographic workflows, and distributed service interactions. Introducing additional language complexity into the most security-sensitive services would increase cognitive load and potentially distract from the primary architectural goals.

The language selection decision therefore required balancing correctness, development velocity, maintainability, learning objectives, and long-term career alignment.

## Alternatives Considered

### Option A: Go Across All Services

Implement all platform services in Go from the beginning.

#### Pros

* Consistent language across the platform
* Strong alignment with modern platform engineering ecosystems
* Excellent operational characteristics
* Simplified hiring and onboarding model
* Direct experience with industry-standard infrastructure tooling patterns

#### Cons

* Requires simultaneous learning of platform security concepts and a less familiar language
* Increases cognitive load during MVP development
* Higher risk of implementation mistakes in security-critical services
* Slower initial delivery while building domain and language proficiency simultaneously

### Option B: Scala Across All Services

Implement all platform services in Scala.

#### Pros

* Consistent language across the platform
* Existing familiarity with JVM cryptography libraries
* Strong type system for modeling security-critical domains
* Reduced cognitive overhead during MVP development

#### Cons

* Slower development velocity for metadata-oriented services
* Additional complexity where advanced type modeling provides limited value
* Less alignment with common platform security infrastructure ecosystems
* Missed opportunity to gain experience operating a polyglot platform

### Option C: Polyglot Architecture (Selected)

Use Scala for KMS and CSP, Python for SR, and migrate KMS to Go after MVP completion.

#### Pros

* Matches language strengths to service requirements
* Leverages existing JVM cryptography familiarity for security-critical services
* Reduces cognitive load during MVP development
* Accelerates development of metadata-driven services
* Reflects real-world polyglot platform environments
* Creates a structured path toward Go proficiency
* Preserves focus on architectural correctness during MVP development

#### Cons

* Introduces multi-language operational complexity
* Requires maintaining expertise across multiple ecosystems
* Increases build and tooling diversity
* Introduces planned migration work for KMS

## Decision

Keystone Platform SHALL adopt a polyglot architecture.

The following language selections are approved:

| Service | Language                   |
| ------- | -------------------------- |
| KMS     | Scala (MVP), Go (Post-MVP) |
| CSP     | Scala                      |
| SR      | Python                     |

Language selection SHALL be based on service requirements, risk profile, and platform objectives rather than strict language uniformity.

The planned migration of KMS from Scala to Go SHALL be treated as an intentional post-MVP architecture initiative rather than a corrective rewrite.

## Rationale

The primary objective of the MVP is validating the platform architecture and cryptographic model. Language learning should not compete with security-domain learning during this phase.

KMS and CSP represent the most security-sensitive components in the platform. Existing familiarity with JVM cryptography libraries reduces implementation risk and allows development effort to focus on cryptographic correctness, lifecycle management, trust boundaries, and platform architecture rather than language acquisition.

Scala provides additional technical advantages for security-critical services. The platform's key lifecycle model naturally maps to Scala's type system through sealed traits, case classes, and pattern matching. This enables illegal lifecycle transitions to become difficult or impossible to represent within the implementation, aligning the language choice directly with the architectural goals of the platform.

Service Registry has fundamentally different requirements. It is primarily a metadata and policy management service rather than a cryptographic service. Python's simplicity, mature ecosystem, and rapid development model allow faster iteration while introducing deliberate polyglot experience consistent with real-world platform engineering environments.

The planned migration of KMS to Go reflects a separate objective. Go has become a dominant language within platform security infrastructure and cloud-native systems. Rather than introducing additional complexity during MVP development, the migration is intentionally deferred until the platform architecture has been validated. A completed and correct MVP implementation provides greater value than an incomplete implementation built while simultaneously learning both the platform domain and a new language.

This decision intentionally prioritizes architectural correctness and domain understanding before platform-language specialization.

## Consequences

### Positive

* Reduces implementation risk in security-critical services
* Allows focus on cryptographic correctness during MVP development
* Leverages strengths of each language where appropriate
* Accelerates development of metadata-driven services
* Introduces realistic polyglot platform experience
* Creates a deliberate path toward Go proficiency
* Aligns language selection with service-specific requirements

### Negative

* Introduces multiple language ecosystems
* Increases tooling and build complexity
* Requires maintaining proficiency across languages
* Creates future migration work for KMS
* Reduces consistency compared to a single-language platform

### Risks

#### Polyglot Complexity

Multiple languages may increase operational and maintenance overhead.

Mitigations:

* Monorepo structure
* Consistent documentation standards
* Shared architectural conventions
* Standardized CI/CD controls

#### Delayed Go Migration

The planned migration may be postponed or deprioritized.

Mitigations:

* Explicit documentation of migration intent
* Dedicated post-MVP roadmap planning
* Architectural separation of concerns

#### Knowledge Fragmentation

Different language ecosystems may create inconsistent implementation patterns.

Mitigations:

* ADR-driven development
* Shared design standards
* Common review controls
* Architecture documentation as source of truth

## Related Decisions

* ADR-004: Separate KMS and CSP Responsibilities
* ADR-010: Storage Engine Selection
* ADR-011: CI/CD Pipeline and Review Controls
* ADR-013: Monorepo Repository Structure
