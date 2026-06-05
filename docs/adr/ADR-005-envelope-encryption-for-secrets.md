# ADR-005: Envelope Encryption for Secrets

## Status

Accepted

## Date Accepted

2026-06-04

## Context

Secret Vault is responsible for storing sensitive platform secrets. As the platform's cryptographic architecture evolved, a key design question emerged: should secrets be encrypted directly with high-value platform keys, or should an intermediate key hierarchy be introduced?

Direct encryption using long-lived platform keys appears simple, but it creates significant operational challenges. Rotation of a platform key requires re-encryption of all protected secrets, increasing operational risk and coupling secret storage directly to key lifecycle management. Additionally, compromise of a high-value encryption key increases blast radius across all secrets protected by that key.

The architecture therefore required a mechanism that reduced blast radius while enabling efficient key rotation and maintaining centralized governance of cryptographic assets.

## Alternatives Considered

### Option A: Direct Encryption with Platform Keys

Secrets are encrypted directly using long-lived platform encryption keys.

#### Pros

* Simple implementation
* Minimal key hierarchy
* Straightforward storage model

#### Cons

* Large blast radius if a platform key is compromised
* Expensive key rotation operations
* Tight coupling between secret storage and key lifecycle management
* Reduced flexibility for future cryptographic changes

### Option B: Envelope Encryption

Secrets are encrypted using Data Encryption Keys (DEKs), while DEKs are protected using Key Encryption Keys (KEKs).

#### Pros

* Reduced blast radius
* Efficient key rotation
* Centralized governance
* Separation between secret protection and key protection

#### Cons

* Additional key hierarchy
* Increased implementation complexity
* Requires key wrapping and rewrapping workflows

### Option C: Per-Secret Unique Keys

Each secret gets its own unique encryption key managed directly by KMS, with no intermediate DEK/KEK hierarchy.

#### Pros

* Maximum blast radius isolation — compromise of one key affects only one secret
* No wrapping/rewrapping complexity

#### Cons

* KMS must manage one key per secret — potentially thousands of keys
* Key proliferation creates governance and operational overhead
* Rotation becomes more complex at scale

## Decision

Keystone Platform SHALL use envelope encryption as the standard mechanism for protecting secrets.

Secrets SHALL be encrypted using Data Encryption Keys (DEKs).

DEKs SHALL be encrypted and protected using Key Encryption Keys (KEKs) managed by KMS.

KMS SHALL govern the lifecycle of both DEKs and KEKs.

Services SHALL NOT directly manage DEK or KEK lifecycle operations.

## Rationale

Envelope encryption separates protection of secret values from protection of encryption keys.

By encrypting secrets with DEKs and protecting those DEKs with KEKs, the platform reduces the impact of key compromise. Compromise of a single DEK affects only the secrets protected by that DEK rather than all secrets protected by a shared platform key.

Envelope encryption also enables efficient key rotation. KEKs can be rotated independently without requiring immediate re-encryption of all protected secrets. Instead, DEKs can be rewrapped as necessary, significantly reducing the operational impact of rotation events.

This approach aligns with Keystone Platform's broader philosophy of centralized cryptographic governance while minimizing blast radius and operational complexity.

## Consequences

### Positive

* Reduces blast radius of key compromise
* Enables efficient key rotation
* Separates secret protection from key protection
* Aligns with centralized KMS governance
* Provides flexibility for future cryptographic changes

### Negative

* Introduces additional cryptographic assets
* Requires DEK tracking and lifecycle management
* Requires key wrapping and rewrapping workflows
* Increases implementation complexity compared to direct encryption
* KEK rotation triggers DEK rewrapping across all dependent secrets — the platform must maintain dependency mappings to identify and rewrap affected DEKs during rotation events.

### Risks

#### DEK Compromise

A compromised DEK may expose the secrets protected by that DEK.

Mitigations:

* Scoped DEK usage
* Centralized lifecycle management
* Rotation capabilities

#### KEK Compromise

A compromised KEK may expose wrapped DEKs protected by that KEK.

Mitigations:

* Key rotation procedures
* Policy-based access controls limiting KEK operations
* Audit logging on KEK access
* Dependency mapping ensures affected DEKs are identified and rewrapped
* Split Root Key Trust (ADR-002) protects the root key from which KEKs derive

## Related Decisions

* ADR-001: KMS Owns All Cryptographic Keys
* ADR-002: Split Root Key Trust
* ADR-004: Separate KMS and CSP Responsibilities
* ADR-007: Secret Versioning and Retention
* ADR-008: Event Payload Encryption Beyond TLS
