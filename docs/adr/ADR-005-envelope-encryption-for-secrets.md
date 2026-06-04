# ADR-005: Envelope Encryption

## Status

Accepted

## Date Accepted

2026-06-04

## Context

Keystone Platform requires a secure and scalable mechanism for protecting secrets, sensitive data, and service-to-service communications. As the platform's cryptographic architecture evolved, a critical design question emerged: how should encryption keys be applied to data throughout the platform?

A naïve approach would encrypt data directly using long-lived master keys. While simple, this model creates significant operational challenges. Key rotation becomes expensive, compromise of a master key increases blast radius, and cryptographic governance becomes tightly coupled to application data.

The platform also requires support for secret rotation, recovery, auditing, rollback, service identity management, and future cryptographic migrations. These requirements favor a design that separates protection of data from protection of encryption keys.

The architecture therefore required a model that minimizes blast radius, supports scalable rotation, and aligns with Keystone Platform's centralized key ownership philosophy.

## Alternatives Considered

### Option A: Direct Encryption with Master Keys

Data is encrypted directly using long-lived platform keys.

#### Pros

* Simple implementation
* Fewer cryptographic components
* Minimal key hierarchy

#### Cons

* Large blast radius if a master key is compromised
* Expensive key rotation operations
* Tight coupling between encrypted data and master keys
* Difficult recovery and migration workflows

### Option B: Service-Owned Encryption Keys

Each service generates and manages its own encryption keys.

#### Pros

* Strong service-level ownership
* Reduced dependency on centralized key management

#### Cons

* Inconsistent lifecycle management
* Fragmented governance
* Difficult auditing and recovery
* Increased operational burden across services

### Option C: Envelope Encryption

Data is encrypted using Data Encryption Keys (DEKs), while DEKs are protected using Key Encryption Keys (KEKs).

#### Pros

* Reduced blast radius
* Efficient key rotation
* Centralized governance
* Simplified recovery workflows
* Scalable cryptographic architecture
* Aligns with centralized key ownership

#### Cons

* Additional key hierarchy
* Increased implementation complexity
* Requires key wrapping and unwrapping workflows

## Decision

Keystone Platform SHALL use envelope encryption as the standard encryption model across the platform.

Data SHALL be encrypted using Data Encryption Keys (DEKs).

DEKs SHALL be encrypted and protected using Key Encryption Keys (KEKs).

KMS SHALL generate, store, govern, rotate, and retire both DEKs and KEKs.

Services SHALL NOT directly manage DEK or KEK lifecycle operations.

Encrypted data SHALL be stored alongside a reference to the wrapped DEK required for decryption.

Cryptographic operations involving DEKs and KEKs SHALL be performed through platform-controlled services in accordance with established authorization and policy controls.

## Rationale

Envelope encryption provides a clear separation between protecting data and protecting encryption keys. This separation significantly reduces operational complexity when compared to direct encryption models.

Because data is encrypted with DEKs rather than KEKs, rotation of a KEK does not require immediate re-encryption of all protected data. Instead, the wrapped DEKs can be rewrapped using the new KEK. This dramatically reduces the cost and operational impact of rotation events.

The model also limits blast radius. Compromise of a single DEK impacts only the data protected by that key, while KEKs remain governed through centralized controls. This aligns with Keystone Platform's broader philosophy of minimizing trust concentration while maintaining centralized governance.

Envelope encryption additionally supports future platform goals including automated rotation, cryptographic migration, auditability, rollback capabilities, and scalable secret lifecycle management.

## Consequences

### Positive

* Reduces blast radius of key compromise
* Enables efficient key rotation
* Supports centralized governance and auditing
* Simplifies cryptographic recovery workflows
* Aligns with KMS ownership model
* Provides a scalable foundation for secret management
* Supports future cryptographic migrations

### Negative

* Introduces additional key hierarchy complexity
* Requires key wrapping and unwrapping workflows
* Increases implementation complexity compared to direct encryption

### Risks

#### DEK Compromise

A compromised DEK may expose data protected by that specific key.

Mitigations:

* Scoped DEK usage
* Centralized lifecycle management
* Rotation capabilities
* Comprehensive auditing

#### KEK Compromise

A compromised KEK may expose wrapped DEKs protected by that KEK.

Mitigations:

* Split Root Key Trust (ADR-002)
* Centralized governance through KMS
* Key rotation procedures
* Policy-based access controls

#### Rotation Failures

Improper key rotation may result in temporary data access issues.

Mitigations:

* Versioned key management
* Rollback support
* Lazy rewrapping strategy
* Operational recovery procedures

## Related Decisions

* ADR-001: KMS Owns All Cryptographic Keys
* ADR-002: Split Root Key Trust
* ADR-004: Separate KMS and CSP Responsibilities
* ADR-007: Secret Versioning and Retention
* ADR-008: Lazy Rewrapping Strategy
