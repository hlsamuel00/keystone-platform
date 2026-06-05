# ADR-002: Split Root Key Trust

## Status

Accepted

## Date Accepted

2026-06-04

## Context

ADR-001 established KMS as the authoritative owner of all cryptographic keys within Keystone Platform. While centralized ownership provides significant benefits in terms of governance, lifecycle management, auditing, and recovery, it also introduces a new concern: concentration of trust. If KMS owns all cryptographic assets, the protection of the platform's root key becomes one of the most critical security considerations in the entire architecture.

Early discussions focused on the implications of a root key compromise. A single root key stored within a single trust boundary creates a high-value target and increases blast radius. An attacker who gains access to the root key could potentially compromise large portions of the platform's cryptographic hierarchy.

The architecture therefore required a mechanism to reduce trust concentration while preserving the operational simplicity and governance benefits of centralized key ownership. The solution needed to meaningfully reduce blast radius without introducing unnecessary infrastructure or creating an entirely new trust domain that would itself require protection and management.

## Alternatives Considered

### Option A: Single Root Key

Store the complete root key within a single KMS trust boundary.

#### Pros

* Simple implementation
* Minimal operational complexity
* Straightforward recovery process

#### Cons

* Single point of compromise
* High blast radius
* Concentrates trust within a single component
* Root key compromise may impact the entire platform

### Option B: Dedicated Root Trust Service

Create a separate service responsible for protecting and managing the platform root key.

#### Pros

* Establishes a separate trust boundary
* Reduces concentration of trust within KMS

#### Cons

* Introduces an additional platform dependency
* Increases operational complexity
* Creates another critical service requiring protection
* Ultimately relocates the trust problem rather than eliminating it

### Option C: Split Root Key Trust

Split the platform root key into two independent shares stored in separate KMS trust domains.

#### Pros

* Reduces blast radius
* Requires compromise of multiple trust domains
* Preserves centralized ownership model
* Avoids introducing additional platform services

#### Cons

* Additional implementation complexity
* Requires key reconstruction workflows
* Introduces dependencies between KMS components during sensitive operations

## Decision

The platform root key SHALL be split into two independent shares.

Root Key Share A SHALL be stored within the Operations Engine trust boundary and persisted within the Operations Engine database.

Root Key Share B SHALL be stored within the Vault trust boundary and persisted within the Vault database.

Neither share SHALL be sufficient to reconstruct the root key independently.

The complete root key SHALL only be reconstructed in volatile memory for approved cryptographic operations requiring root-key access.

Following completion of the operation, the reconstructed root key SHALL be removed from memory.

## Rationale

The selected approach balances security, operational simplicity, and architectural consistency.

A single root key creates an unacceptable concentration of trust and introduces a platform-wide single point of compromise. While a dedicated trust service could reduce this concentration, it would also introduce additional infrastructure and operational overhead without fundamentally eliminating the trust problem.

By splitting the root key into independent shares, Keystone Platform reduces blast radius while preserving the centralized governance model established in ADR-001. Compromise of either the Operations Engine or the Vault is insufficient to reconstruct the complete root key, requiring an attacker to successfully compromise multiple trust domains before gaining access to the platform's highest-value cryptographic asset.

This decision further establishes a recurring Keystone Platform principle: high-value trust relationships should be distributed where practical, while ownership remains centralized. Rather than introducing additional platform services or trust authorities, the architecture reduces trust concentration through separation of critical assets across independent ownership boundaries.

This design also aligns with the platform's broader philosophy of separating responsibilities and minimizing trust concentration without introducing unnecessary services. The solution provides a meaningful security improvement while remaining operationally manageable and consistent with the existing KMS architecture.

## Consequences

### Positive

* Eliminates persistent storage of the complete root key
* Reduces blast radius associated with root key compromise
* Requires compromise of multiple trust domains
* Preserves centralized key ownership and governance
* Avoids introducing additional platform services
* Aligns with platform-wide trust separation principles

### Negative

* Increases implementation complexity
* Requires root key reconstruction workflows
* Introduces additional operational dependencies between KMS components
* Recovery procedures become more complex than a single-key model

### Risks

#### Operations Engine Compromise

An attacker may obtain Root Key Share A.

Mitigations:

* Possession of Share A alone is insufficient to reconstruct the root key. Independent trust boundary, policy-based authorization, and audit logging apply to Operations Engine access.

#### Vault Compromise

An attacker may obtain Root Key Share B.

Mitigations:

* Possession of Share B alone is insufficient to reconstruct the root key. Independent trust boundary, encryption at rest, and audit logging apply to Vault access.

#### Simultaneous Trust Domain Compromise

An attacker who successfully compromises both trust domains may reconstruct the root key.

Mitigations:

* Independent trust boundaries
* Policy-based authorization controls
* Comprehensive audit logging
* Future monitoring and intrusion detection controls
* Future high-availability and recovery mechanisms

#### In-Memory Reconstruction Exposure

The complete root key exists briefly during approved operations.

Mitigations:

* Reconstruction only occurs when required
* Root key is never persisted in reconstructed form
* Reconstructed key material is destroyed following operation completion

## Related Decisions

* ADR-001: KMS Owns All Cryptographic Keys
* ADR-003: Platform-Managed Service Identity Keys
* ADR-004: Separate KMS and CSP Responsibilities
* ADR-005: Envelope Encryption for Secrets