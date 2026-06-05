# ADR-003: Platform-Managed Service Identity Keys

## Status

Accepted

## Date Accepted

2026-06-04

## Context

Machine identity is a foundational requirement for Keystone Platform. Services must be able to prove who they are, establish trust with platform components, obtain certificates, request tokens, and securely communicate with other services. As the platform's trust model evolved, Keystone Platform required a clear answer to a foundational question: who owns Service Identity Keys?

The most common approach in distributed systems is service-generated identity. Under this model, a service generates and manages its own identity keypair and presents the public portion to a certificate authority for validation and certificate issuance. This model provides strong local ownership and isolation, but introduces challenges around recovery, lifecycle management, auditing, and consistency across the platform.

During the design process, Keystone Platform placed significant emphasis on centralized governance and recoverability. Earlier architectural decisions had already established KMS as the authoritative owner of cryptographic assets. Introducing a separate ownership model for Service Identity Keys would create an exception to that principle and require a distinct lifecycle, recovery model, and governance process.

The architecture therefore required a solution that maintained machine identity as a first-class security capability while preserving consistency with the broader platform philosophy of centralized cryptographic ownership and lifecycle management.

## Alternatives Considered

### Option A: Service-Generated Identity Keys

Each service generates and manages its own identity keypair.

#### Pros

* Strong service-level ownership
* Reduced dependency on centralized identity key generation
* Common industry pattern
* Clear isolation between services

#### Cons

* Difficult recovery workflows
* Fragmented lifecycle management
* Inconsistent auditing capabilities
* Complicates certificate rotation processes
* Creates a separate governance model from other key types

### Option B: STS-Owned Identity Keys

The Security Token Service generates and owns Service Identity Keys.

#### Pros

* Identity concerns remain within the identity domain
* Simplifies certificate issuance workflows

#### Cons

* Introduces cryptographic key ownership outside KMS
* Creates overlapping responsibilities between STS and KMS
* Violates centralized key ownership principles
* Requires duplicate lifecycle management capabilities

### Option C: Platform-Managed Identity Keys

KMS generates and owns Service Identity Keys while STS issues certificates and tokens.

#### Pros

* Consistent ownership model
* Simplified recovery workflows
* Centralized auditing and governance
* Unified lifecycle management
* Aligns with existing KMS responsibilities

#### Cons

* Increases sensitivity of KMS
* Requires secure delivery of operational key material
* Services cannot independently establish or recover their own identities
* Identity provisioning becomes dependent on KMS availability and governance controls

## Decision

Service Identity Keys SHALL be generated and owned by KMS.

During onboarding, KMS SHALL generate a Service Identity Keypair and maintain the authoritative copy of that key material.

STS SHALL use the generated public key to issue certificates and perform identity-related functions, including certificate lifecycle management and token issuance.

Services SHALL receive an operational copy of their identity credentials for runtime authentication and authorization activities.

KMS SHALL remain the authoritative source of truth for Service Identity Keys and SHALL manage their lifecycle, recovery, rotation, and retirement.

## Rationale

Keystone Platform prioritizes consistency in cryptographic governance. Service Identity Keys are cryptographic assets and therefore fall within the ownership boundaries established by ADR-001. Creating a separate ownership model for identity keys would introduce unnecessary complexity and undermine the platform's centralized governance strategy.

Centralized ownership significantly improves recovery capabilities. If a service is destroyed, redeployed, or otherwise loses access to its operational credentials, the authoritative identity can be restored through KMS without requiring the creation of a completely new identity. This preserves continuity and simplifies operational recovery workflows.

Separating identity ownership from identity operations also creates clearer service boundaries. KMS owns key material, while STS consumes identity information to issue certificates, validate trust relationships, and issue authorization tokens. This aligns with the broader architectural principle of separating ownership from execution.

The increased sensitivity of KMS under this model is accepted because the platform had already committed to centralized cryptographic ownership and introduced specific controls — split root-key trust, policy enforcement, and lifecycle governance — to protect against that concentration.

## Consequences

### Positive

* Establishes a consistent ownership model across all key types
* Simplifies identity recovery workflows
* Centralizes auditing and governance
* Supports unified lifecycle management
* Creates clear separation between identity ownership and identity operations
* Reduces duplication of key management responsibilities

### Negative

* Increases sensitivity of KMS
* Requires secure distribution of operational identity credentials
* Reduces direct service ownership of identity assets
* Introduces dependency on KMS for identity provisioning

### Risks

#### KMS Compromise

Compromise of KMS may affect Service Identity Keys in addition to other cryptographic assets.

Mitigations:

* ADR-002 Split Root Key Trust
* Policy-based authorization controls
* Comprehensive audit logging
* Lifecycle governance and monitoring

#### Credential Distribution Risk

Operational credentials must be delivered securely to services during onboarding and recovery workflows.

Mitigations:

* Controlled onboarding process
* Authorized credential distribution mechanisms
* Future enhancements to identity provisioning workflows

#### Identity Lifecycle Complexity

Identity recovery, rotation, and certificate management become dependent on coordination between KMS and STS.

Mitigations:

* Clear service boundaries
* Defined ownership responsibilities
* Centralized lifecycle management processes

## Related Decisions

* ADR-001: KMS Owns All Cryptographic Keys
* ADR-002: Split Root Key Trust
* ADR-004: Separate KMS and CSP Responsibilities
* ADR-006: Service Registry as Policy Authority and STS as Enforcement Point
