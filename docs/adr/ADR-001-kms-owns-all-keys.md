# ADR-001: KMS Owns All Cryptographic Keys

## Status

Accepted

## Date

2026-06-04

## Context

One of the earliest architectural questions in Keystone Platform was determining ownership of cryptographic assets. The platform requires multiple categories of keys, including Key Encryption Keys (KEKs), Data Encryption Keys (DEKs), Service Identity Keys, Signing Keys, Transmission Keys, Certificate Authority Keys, and Root Key Shares. These assets are used across multiple services and support fundamentally different platform capabilities such as machine identity, secret management, cryptographic operations, and secure service-to-service communication.

Several ownership models were considered during the design process. A service-owned model initially appeared attractive because it maximizes isolation and allows individual services to manage their own cryptographic assets. Under this approach, a service would generate and maintain the keys required for its operation. While this provides strong local ownership, it introduces significant challenges around recovery, auditing, lifecycle management, policy enforcement, and operational consistency.

A domain-owned model was also considered. In this approach, groups of related services would share responsibility for cryptographic assets within a particular business or platform domain. Although this reduces some duplication compared to fully service-owned keys, it still creates fragmented ownership boundaries and increases the complexity of rotation, auditing, and governance across the platform.

As the platform design matured, it became clear that cryptographic assets represent a foundational security concern rather than an application concern. The platform requires consistent controls around key generation, storage, rotation, recovery, retirement, auditing, and authorization. These requirements strongly favored a centralized ownership model.

## Alternatives Considered

### Option A: Service-Owned Keys

Each service generates, stores, rotates, and manages its own cryptographic assets.

#### Pros

* Strong isolation between services
* Reduced dependency on a central authority
* Clear local ownership

#### Cons

* Inconsistent lifecycle management
* Difficult recovery workflows
* Fragmented auditing
* Increased operational burden
* Higher likelihood of implementation drift between services

### Option B: Domain-Owned Keys

Cryptographic assets are managed by logical platform or business domains rather than individual services.

#### Pros

* Reduced duplication compared to service-owned keys
* Some centralization of governance

#### Cons

* Ownership remains fragmented
* Auditing and policy enforcement become more complex
* Rotation and recovery processes vary by domain
* Difficult to establish a single source of truth

### Option C: KMS-Owned Keys

All cryptographic assets are owned and managed by a centralized Key Management Service (KMS).

#### Pros

* Consistent lifecycle management
* Centralized auditing
* Unified policy enforcement
* Simplified recovery workflows
* Single authoritative source of truth

#### Cons

* KMS becomes a critical dependency
* Increased concentration of sensitive assets
* Greater operational importance of KMS availability

## Decision

Keystone Platform adopts a centralized ownership model in which KMS is the sole authoritative owner of all cryptographic key material. No other service is permitted to independently generate, own, or govern cryptographic keys outside the controls established by KMS.

This decision applies to all key categories within the platform, including KEKs, DEKs, Service Identity Keys, Signing Keys, Transmission Keys, Certificate Authority Keys, and Root Key Shares.

## Rationale

Cryptographic assets are foundational platform resources whose lifecycle requirements extend beyond the needs of any individual service. Centralized ownership allows the platform to enforce consistent standards for generation, storage, rotation, auditing, recovery, retirement, and policy enforcement.

The design philosophy adopted throughout Keystone Platform is that cryptography should be governed centrally while being consumed broadly. Services should focus on business functionality and consume cryptographic capabilities through well-defined platform services rather than becoming independent custodians of security-critical assets.

Centralized ownership also supports future platform goals such as automated rotation, compromise response workflows, dependency tracking, audit reporting, and policy-driven access controls. These capabilities become significantly more difficult when key ownership is distributed across multiple services.

While KMS becomes a higher-value target under this model, subsequent architectural decisions—including split root-key trust, strict policy enforcement, separation of cryptographic execution into CSP, and comprehensive auditing—were specifically introduced to mitigate the risks associated with centralized ownership.

## Consequences

### Positive

* Establishes a single source of truth for cryptographic assets
* Enables consistent lifecycle management across all key types
* Simplifies auditing and compliance reporting
* Supports centralized recovery and rotation workflows
* Reduces implementation drift between services
* Provides a foundation for future automation and governance capabilities

### Negative

* KMS becomes a Tier-0 platform dependency
* Concentrates responsibility within a single platform component
* Increases operational importance of KMS availability
* Requires additional controls to protect centralized key ownership

### Risks

#### KMS Compromise

A compromise of KMS could impact multiple platform capabilities.

Mitigations:

* Split Root Key Trust (ADR-002)
* Policy-based authorization
* Separation of KMS and CSP responsibilities
* Comprehensive audit logging

#### KMS Availability Issues

Platform capabilities dependent on key access may be impacted during outages.

Mitigations:

* Horizontal scaling strategy
* Future high-availability architecture
* Operational recovery procedures

## Related Decisions

* ADR-002: Split Root Key Trust
* ADR-003: Platform-Managed Service Identity Keys
* ADR-004: Separate KMS and CSP Responsibilities
* ADR-005: Envelope Encryption
